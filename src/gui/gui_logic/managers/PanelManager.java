package gui.gui_logic.managers;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PanelManager {
    private final JPanel container;
    private final CardLayout cardLayout;
    private final Map<String, JPanel> panels;

    public PanelManager(JPanel container, CardLayout cardLayout) {
        this.container = container;
        this.cardLayout = cardLayout;
        this.panels = new HashMap<>();
    }

    public void addPanel(String name, JPanel panel) {
        if (!panels.containsKey(name)) {
            panels.put(name, panel);
            container.add(panel, name);
        } else {
            // Reemplazar panel existente
            container.remove(panels.get(name));
            panels.put(name, panel);
            container.add(panel, name);
        }
    }

    public void showPanel(String name) {
        if (panels.containsKey(name)) {
            cardLayout.show(container, name);
        } else {
            System.err.println("Panel no encontrado: " + name);
        }
    }

    public boolean hasPanel(String name) {
        return panels.containsKey(name);
    }

    public JPanel getPanel(String name) {
        return panels.get(name);
    }

    public void removePanel(String name) {
        if (panels.containsKey(name)) {
            container.remove(panels.get(name));
            panels.remove(name);
        }
    }
}