package javaapplication3;

import javax.swing.SwingUtilities;

/**
 * Entry point.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
