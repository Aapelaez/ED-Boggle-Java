package gui;

import logic.Jugador;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScoreboardPanel extends JPanel {

    public interface ScoreActions {
        void onVolverMenu();
    }

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private final JButton btnVolver = new JButton("Volver");

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ScoreboardPanel(ScoreActions actions) {
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Top 10", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnVolver);
        add(south, BorderLayout.SOUTH);

        btnVolver.addActionListener(e -> {
            AudioManager.playClick();
            actions.onVolverMenu();
        });
    }

    // Mostrar lista real de jugadores
    public void setJugadores(List<Jugador> jugadores) {
        modelo.clear();
        int pos = 1;
        for (Jugador j : jugadores) {
            String fecha = formatFecha(j.getUltimaPartida());
            String linea = String.format("%2d. %s - %d pts  (última: %s)",
                    pos++, j.getNombre(), j.getPuntos(), fecha);
            modelo.addElement(linea);
        }
        if (modelo.isEmpty()) {
            modelo.addElement("No hay jugadores registrados todavía.");
        }
    }

    // Mostrar un único mensaje (por ejemplo, errores o “sin datos”)
    public void setMensaje(String msg) {
        modelo.clear();
        modelo.addElement(msg);
    }

    private String formatFecha(Date d) {
        return d == null ? "-" : sdf.format(d);
    }
}