package com.resourcegame;

import com.resourcegame.ui.MainMenu;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}