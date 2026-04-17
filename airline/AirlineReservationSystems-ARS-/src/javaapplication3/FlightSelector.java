package javaapplication3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Window shown after login: lets user pick which flight to open.
 */
public class FlightSelector extends JFrame {
    private final MultiFlightBuffer server;
    private final List<Flight> flights;

    public FlightSelector(MultiFlightBuffer server) {
        super("Select Flight");
        this.server = server;
        this.flights = server.listFlights();

        buildUI();
    }

    private void buildUI() {
        setSize(400, 220);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);

        JLabel lbl = new JLabel("Select Flight:");
        lbl.setBounds(20, 10, 200, 30);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(lbl);

        int btnY = 50;
        for (Flight f : flights) {
            JButton b = new JButton(f.getDisplayName());
            b.setBounds(20, btnY, 340, 40);
            btnY += 45;
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // open a Client window for this flight
                    SwingUtilities.invokeLater(() -> new Client(server, f));
                }
            });
            add(b);
        }

        setVisible(true);
    }
}
