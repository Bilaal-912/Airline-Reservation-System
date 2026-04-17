package javaapplication3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * Client window for a specific flight.
 */
public class Client extends JFrame {
    private final JButton reserveBtn, availableBtn;
    private final JLabel reserveLabel;
    private final JTextField reserveInput;
    private final JTextArea log;
    private final SeatPlanPanel seatPlan;
    private final MultiFlightBuffer server;
    private final String flightCode;
    private final Flight flight;

    public Client(MultiFlightBuffer server, Flight flight) {
        super("Flight: " + flight.getDisplayName() + " (" + flight.getCode() + ")");
        this.server = server;
        this.flight = flight;
        this.flightCode = flight.getCode();

        log = new JTextArea();
        reserveLabel = new JLabel("Enter Seat Number");
        reserveInput = new JTextField();
        reserveBtn = new JButton("Reserve");
        availableBtn = new JButton("Available Seats");

        seatPlan = new SeatPlanPanel(server.seatCount(flightCode));

        // register this client's log for this flight
        server.registerClientLog(flightCode, log);

        buildUI();
        attachListeners();

        // initial fetch
        fetchAvailableSeats();
    }

    private void buildUI() {
        setSize(580, 580);
        setResizable(false);
        setLayout(null);

        seatPlan.setBounds(10, 10, 540, 220);
        add(seatPlan);

        reserveLabel.setBounds(30, 250, 150, 30);
        reserveInput.setBounds(150, 255, 130, 30);
        reserveBtn.setBounds(290, 255, 100, 30);
        availableBtn.setBounds(400, 255, 150, 30);

        add(reserveLabel);
        add(reserveInput);
        add(reserveBtn);
        add(availableBtn);

        log.setFont(new Font("Consolas", Font.PLAIN, 14));
        log.setBackground(Color.white);
        log.setForeground(Color.black);
        log.setEditable(false);
        JScrollPane sp = new JScrollPane(log);
        sp.setBounds(10, 300, 540, 220);
        add(sp);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void attachListeners() {
        reserveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onReserveClicked();
            }
        });
        availableBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchAvailableSeats();
            }
        });
    }

    private void onReserveClicked() {
        int seatNo = getSeatNumberFromInput();
        if (seatNo == 0) return;

        String requester = "client: " + Thread.currentThread().getName() + " (GUI)";
        try {
            ReservationResult res = server.makeReservation(flightCode, requester, seatNo, Thread.currentThread().getId());
            if (res.isSuccess()) {
                SwingUtilities.invokeLater(() -> seatPlan.bookSeat(seatNo));
            }
            SwingUtilities.invokeLater(() -> log.append(res.getMessage()));
        } catch (InterruptedException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Operation interrupted"));
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()));
        } finally {
            reserveInput.setText("");
        }
    }

    private int getSeatNumberFromInput() {
        String text = reserveInput.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter seat number", "Message", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        try {
            int val = Integer.parseInt(text);
            if (val < 1 || val > server.seatCount(flightCode)) {
                JOptionPane.showMessageDialog(this, "Invalid seat number (1 - " + server.seatCount(flightCode) + ")", "Error", JOptionPane.ERROR_MESSAGE);
                return 0;
            }
            return val;
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid seat number", "Error", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    private void fetchAvailableSeats() {
        String requester = "client: " + Thread.currentThread().getName() + " (GUI)";
        try {
            AvailableSeatsResult r = server.seeAvailableSeats(flightCode, requester);
            SwingUtilities.invokeLater(() -> {
                seatPlan.availableSeats(r.getSeats());
                log.append(r.getMessage());
            });
        } catch (InterruptedException ex) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Operation interrupted"));
            Thread.currentThread().interrupt();
        }
    }
}
