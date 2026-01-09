package sound;

import javax.sound.sampled.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class BackgroundMusic {
    private static Clip currentClip;
    private static boolean enabled = true;
    private static float volume = 0.7f;

    // Tipos de música
    public enum MusicType {
        AMBIENTE("game_files/musica_ambiente.wav", true),
        TRANSICION("game_files/jingle_transicion.wav", false),
        SUSPENSE("game_files/musica_suspense.wav", true);

        private final String path;
        private final boolean loop;

        MusicType(String path, boolean loop) {
            this.path = path;
            this.loop = loop;
        }

        public String getPath() { return path; }
        public boolean shouldLoop() { return loop; }
    }

    private static MusicType currentMusicType = MusicType.AMBIENTE;

    private BackgroundMusic() {}

    // Cargar la configuracion de audio guardada
    public static void cargarConfiguracionAudio() {
        try {
            // Determinar directorio de datos
            String userHome = System.getProperty("user.home");
            Path appDataDir = Paths.get(userHome, ".boggle");
            Path prefsPath = appDataDir.resolve("preferencias.properties");

            if (!Files.exists(prefsPath)) {
                return; // Usar valores por defecto
            }

            java.util.Properties props = new java.util.Properties();
            props.load(Files.newInputStream(prefsPath));

            // Cargar y aplicar configuración de audio inmediatamente
            String audio = props.getProperty("audio_habilitado", "true");
            AudioManager.setEnabled(Boolean.parseBoolean(audio));

            String musica = props.getProperty("musica_habilitada", "true");
            BackgroundMusic.setEnabled(Boolean.parseBoolean(musica));

            String volMusica = props.getProperty("volumen_musica", "80");
            String volSonidos = props.getProperty("volumen_sonidos", "80");

            float volMusicaFloat = Integer.parseInt(volMusica) / 100.0f;
            float volSonidosFloat = Integer.parseInt(volSonidos) / 100.0f;

            BackgroundMusic.setVolume(volMusicaFloat);
            AudioManager.setVolumen(volSonidosFloat);

        } catch (Exception e) {
            System.err.println("Error cargando configuración de audio: " + e.getMessage());
        }
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!on) {
            stop();
        } else {
            // Solo reproducir si no hay música actual o está detenida
            if (currentClip == null || !currentClip.isRunning()) {
                play(currentMusicType);
            }
        }
    }

    public static void setVolume(float newVolume) {
        volume = Math.max(0.0f, Math.min(1.0f, newVolume));
        if (currentClip != null && currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public static void play(MusicType type) {
        if (!enabled) return;

        try {
            // Si ya está reproduciendo el mismo tipo, no hacer nada
            if (currentClip != null && currentClip.isRunning() && currentMusicType == type) {
                return;
            }

            // Detener música actual si existe
            if (currentClip != null) {
                currentClip.stop();
                currentClip.close();
                currentClip = null;
            }

            currentMusicType = type;

            // Cargar el archivo de música
            File f = new File(type.getPath());
            AudioInputStream ais = null;

            if (f.exists()) {
                ais = AudioSystem.getAudioInputStream(f);
            } else {
                URL res = BackgroundMusic.class.getClassLoader().getResource(type.getPath());
                if (res != null) {
                    ais = AudioSystem.getAudioInputStream(res);
                }
            }

            if (ais == null) {
                System.err.println("[Música] No se encontró el archivo: " + type.getPath());
                return;
            }

            currentClip = AudioSystem.getClip();
            currentClip.open(ais);

            // Configurar volumen
            if (currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }

            // Reproducir en bucle o una sola vez
            if (type.shouldLoop()) {
                currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP && !currentClip.isRunning()) {
                        // Cuando termina una transición, volver a la música ambiente
                        if (type == MusicType.TRANSICION) {
                            play(MusicType.AMBIENTE);
                        }
                    }
                });
                currentClip.start();
            }

        } catch (Exception e) {
            System.err.println("[Música] Error reproduciendo " + type + ": " + e.getMessage());
            currentClip = null; // Asegurar que no quede un clip en estado inválido
        }
    }

    public static void stop() {
        if (currentClip != null) {
            try {
                currentClip.stop();
                currentClip.close();
            } catch (Exception e) {
                System.err.println("[Música] Error al detener música: " + e.getMessage());
            } finally {
                currentClip = null;
            }
        }
    }

    // Método para verificar si la música está reproduciéndose
    public static boolean isPlaying() {
        return currentClip != null && currentClip.isRunning();
    }

    // Método para reiniciar la música si se detuvo inesperadamente
    public static void ensurePlaying() {
        if (enabled && (currentClip == null || !currentClip.isRunning())) {
            System.out.println("[Música] Reanudando música que se detuvo inesperadamente");
            play(currentMusicType);
        }
    }

    public static void playTransicion() {
        play(MusicType.TRANSICION);
    }

    public static void playSuspense() {
        play(MusicType.SUSPENSE);
    }

    public static void playAmbiente() {
        play(MusicType.AMBIENTE);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static float getVolume() {
        return volume;
    }

    public static MusicType getCurrentMusicType() {
        return currentMusicType;
    }
}