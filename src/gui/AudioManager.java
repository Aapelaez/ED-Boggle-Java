package gui;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class AudioManager {
    private static boolean enabled = true;
    private static String clickPath = "game_files/click.wav";
    private static Clip clickClip;
    private static boolean preloaded = false;

    private AudioManager() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static void setClickPath(String path) {
        clickPath = path;
        preloaded = false; // fuerza recarga con la nueva ruta
        closeClip();
    }

    public static void preloadClick() {
        if (preloaded) return;
        closeClip();

        try {
            // 1) Intenta cargar desde archivo
            File f = new File(clickPath);
            AudioInputStream ais = null;

            if (f.exists()) {
                System.out.println("[Audio] Cargando desde archivo: " + f.getAbsolutePath() + " (" + f.length() + " bytes)");
                ais = AudioSystem.getAudioInputStream(f);
            } else {
                // 2) Alternativa: desde recursos del classpath (si se empaqueta dentro del jar)
                URL res = AudioManager.class.getClassLoader().getResource(clickPath);
                if (res != null) {
                    System.out.println("[Audio] Cargando desde recurso: " + res);
                    ais = AudioSystem.getAudioInputStream(res);
                }
            }

            if (ais == null) {
                System.err.println("[Audio] No se encontró el fichero de audio en: " + clickPath);
                preloaded = true; // evita reintentos constantes
                return;
            }

            // Intentar convertir a PCM_SIGNED 16-bit si el formato original no es PCM
            AudioFormat base = ais.getFormat();
            AudioFormat decoded = base;
            if (base.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                decoded = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        base.getSampleRate(),
                        16,
                        base.getChannels(),
                        base.getChannels() * 2,
                        base.getSampleRate(),
                        false
                );
                if (AudioSystem.isConversionSupported(decoded, base)) {
                    ais = AudioSystem.getAudioInputStream(decoded, ais);
                } else {
                    System.err.println("[Audio] Conversión a PCM_SIGNED no soportada para este archivo.");
                }
            }

            clickClip = AudioSystem.getClip();
            clickClip.open(ais); // carga en memoria
            preloaded = true;
            System.out.println("[Audio] Clip precargado. Formato: " + clickClip.getFormat());

        } catch (UnsupportedAudioFileException e) {
            System.err.println("[Audio] Formato de audio no soportado: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("[Audio] Dispositivo de audio no disponible: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[Audio] Error E/S cargando audio: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Audio] Error inesperado: " + e.getMessage());
        }
    }

    public static void playClick() {
        if (!enabled) return;

        if (!preloaded) {
            preloadClick();
        }

        if (clickClip != null) {
            try {
                if (clickClip.isRunning()) {
                    clickClip.stop();
                }
                clickClip.setFramePosition(0);
                clickClip.start();
                return;
            } catch (Exception e) {
                System.err.println("[Audio] Falló reproducción de clip precargado: " + e.getMessage());
            }
        }

        // Fallback mínimo para no dejar el clic “mudo”
        Toolkit.getDefaultToolkit().beep();
    }

    private static void closeClip() {
        try {
            if (clickClip != null) {
                clickClip.stop();
                clickClip.flush();
                clickClip.close();
            }
        } catch (Exception ignored) {
        } finally {
            clickClip = null;
        }
    }
}