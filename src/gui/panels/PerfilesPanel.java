package gui.panels;

import sound.AudioManager;
import gui.frames.MainFrame;
import gui.gui_logic.controllers.PerfilesController;
import logic.Jugador;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;

public class PerfilesPanel extends JPanel {

    public interface PerfilesActions {
        void onVolver();
    }

    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnVolver = new JButton("Volver");
    private final JList<JugadorItem> listaJugadores = new JList<>();
    private final DefaultListModel<JugadorItem> modeloJugadores = new DefaultListModel<>();

    private final File datosFile;
    private final PerfilesActions actions;
    private final MainFrame parentFrame;
    private final PerfilesController controller;

    private final ImageIcon iconoPapelera = crearIconoPapelera();

    // Clase interna para representar cada jugador en la lista
    public class JugadorItem {
        private Jugador jugador;
        private boolean mostrarPapelera = false;

        public JugadorItem(Jugador jugador) {
            this.jugador = jugador;
        }

        public Jugador getJugador() {
            return jugador;
        }

        public boolean isMostrarPapelera() {
            return mostrarPapelera;
        }

        public void setMostrarPapelera(boolean mostrar) {
            this.mostrarPapelera = mostrar;
        }

        @Override
        public String toString() {
            return jugador.getNombre();
        }
    }

    // Renderer personalizado para mostrar icono de papelera
    private class JugadorItemRenderer extends JPanel implements ListCellRenderer<JugadorItem> {
        private JLabel labelNombre;
        private JLabel labelPapelera;

        public JugadorItemRenderer() {
            setLayout(new BorderLayout(10, 0));
            setOpaque(true);

            labelNombre = new JLabel();
            labelNombre.setFont(new Font("Arial", Font.PLAIN, 16));
            labelNombre.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            labelPapelera = new JLabel(iconoPapelera);
            labelPapelera.setPreferredSize(new Dimension(30, 30));
            labelPapelera.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            labelPapelera.setCursor(new Cursor(Cursor.HAND_CURSOR));

            labelPapelera.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    labelPapelera.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    labelPapelera.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                }
            });

            add(labelNombre, BorderLayout.CENTER);
            add(labelPapelera, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends JugadorItem> list, JugadorItem item,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            labelNombre.setText(item.getJugador().getNombre());
            labelPapelera.setVisible(item.isMostrarPapelera());

            if (isSelected) {
                setBackground(new Color(200, 230, 255));
                labelNombre.setForeground(Color.BLACK);
                setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            } else {
                setBackground(Color.WHITE);
                labelNombre.setForeground(Color.BLACK);

                if (item.isMostrarPapelera()) {
                    Border bordeExterno = BorderFactory.createLineBorder(new Color(220, 220, 220), 1);
                    Border bordeInterno = BorderFactory.createLineBorder(new Color(240, 240, 240), 2);
                    Border margen = BorderFactory.createEmptyBorder(1, 1, 1, 1);

                    setBorder(BorderFactory.createCompoundBorder(
                            bordeExterno,
                            BorderFactory.createCompoundBorder(bordeInterno, margen)
                    ));

                    setBackground(new Color(250, 250, 250));
                } else {
                    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                }
            }

            return this;
        }
    }

    public PerfilesPanel(File datosFile, PerfilesActions actions, MainFrame parent) {
        this.datosFile = datosFile;
        this.actions = actions;
        this.parentFrame = parent;
        this.controller = new PerfilesController(this, datosFile, parent);

        initUI();
        controller.cargarJugadores();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel titulo = new JLabel("PERFILES", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.BLACK);
        add(titulo, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Color.WHITE);

        listaJugadores.setModel(modeloJugadores);
        listaJugadores.setCellRenderer(new JugadorItemRenderer());
        listaJugadores.setFixedCellHeight(50);
        listaJugadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        listaJugadores.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = listaJugadores.locationToIndex(e.getPoint());
                if (index >= 0) {
                    for (int i = 0; i < modeloJugadores.size(); i++) {
                        JugadorItem item = modeloJugadores.getElementAt(i);
                        item.setMostrarPapelera(false);
                    }

                    JugadorItem item = modeloJugadores.getElementAt(index);
                    item.setMostrarPapelera(true);
                    listaJugadores.repaint();
                }
            }
        });

        listaJugadores.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = listaJugadores.locationToIndex(e.getPoint());
                if (index >= 0 && index < modeloJugadores.size()) {
                    Rectangle cellBounds = listaJugadores.getCellBounds(index, index);
                    int cellWidth = cellBounds.width;
                    int clickX = e.getX() - cellBounds.x;

                    if (clickX > cellWidth - 40) {
                        JugadorItem item = modeloJugadores.getElementAt(index);
                        controller.eliminarJugadorConfirmacion(item.getJugador());
                        return;
                    }

                    if (e.getClickCount() == 2) {
                        JugadorItem item = modeloJugadores.getElementAt(index);
                        controller.seleccionarJugador(item.getJugador());
                    }
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                for (int i = 0; i < modeloJugadores.size(); i++) {
                    JugadorItem item = modeloJugadores.getElementAt(i);
                    item.setMostrarPapelera(false);
                }
                listaJugadores.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(listaJugadores);
        scrollPane.setPreferredSize(new Dimension(450, 350));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(16);
        verticalScrollBar.setPreferredSize(new Dimension(12, 0));

        JPanel scrollContainer = new JPanel(new GridBagLayout());
        scrollContainer.setBackground(Color.WHITE);
        scrollContainer.add(scrollPane);
        center.add(scrollContainer, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        botonesPanel.setBackground(Color.WHITE);

        configurarBoton(btnAgregar, 140, 45);
        configurarBoton(btnEditar, 140, 45);

        botonesPanel.add(btnAgregar);
        botonesPanel.add(btnEditar);

        center.add(botonesPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(Color.WHITE);
        configurarBoton(btnVolver, 150, 40);
        south.add(btnVolver);
        add(south, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> controller.agregarJugador());
        btnEditar.addActionListener(e -> controller.editarJugador(listaJugadores.getSelectedIndex()));
        btnVolver.addActionListener(e -> {
            AudioManager.playClick();
            actions.onVolver();
        });
    }

    private ImageIcon crearIconoPapelera() {
        Image img = new ImageIcon(getClass().getResource("/game_files/papelera.png")).getImage();
        if (img == null) {
            img = createTrashIcon(20, 20);
        }
        return new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
    }

    private Image createTrashIcon(int width, int height) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);

        g2d.setColor(Color.RED);
        g2d.fillRect(5, 3, 10, 12);
        g2d.fillRect(3, 2, 14, 2);
        g2d.fillRect(8, 15, 4, 2);

        g2d.setColor(Color.WHITE);
        g2d.drawLine(7, 5, 7, 12);
        g2d.drawLine(10, 5, 10, 12);
        g2d.drawLine(13, 5, 13, 12);

        g2d.dispose();
        return image;
    }

    private void configurarBoton(JButton boton, int ancho, int alto) {
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setMinimumSize(new Dimension(ancho, alto));
        boton.setMaximumSize(new Dimension(ancho, alto));
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);

        boton.setBackground(new Color(70, 130, 180));
        boton.setForeground(Color.BLACK);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(70, 130, 180));
            }
        });
    }

    // Getters para el controlador
    public DefaultListModel<JugadorItem> getModeloJugadores() {
        return modeloJugadores;
    }

    public PerfilesActions getActions() {
        return actions;
    }

    public void setListaJugadoresEnabled(boolean enabled) {
        listaJugadores.setEnabled(enabled);
    }

    public void actualizarLista() {
        controller.cargarJugadores();
    }
}