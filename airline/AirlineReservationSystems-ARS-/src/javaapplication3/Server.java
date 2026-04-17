package javaapplication3;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Thread-safe server implementing FlightBuffer.
 * It keeps an internal seats map (1..24) where value=0L means free, otherwise contains customerId.
 * It notifies any registered JTextArea logs (safely on EDT).
 */
class Server implements FlightBuffer {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    private final HashMap<Integer, Long> seats;
    private final List<JTextArea> clientLogs = Collections.synchronizedList(new ArrayList<>());
    private int bookedCount = 0;

    public Server() {
        this.seats = new HashMap<>();
        for (int i = 1; i <= 24; i++) {
            this.seats.put(i, 0L);
        }
    }

    @Override
    public AvailableSeatsResult seeAvailableSeats(String name) throws InterruptedException {
        // read lock while building message and copying seats
        String newLogStr;
        lock.readLock().lock();
        HashMap<Integer, Long> copy;
        try {
            String time = "Time: " + LocalTime.now() + "\n";
            StringBuilder newLog = new StringBuilder();
            newLog.append(time).append(name).append('\n');
            newLog.append("Server update: Available Seats\n");
            newLogStr = newLog.toString();
            copy = new HashMap<>(this.seats); // defensive copy
        } finally {
            lock.readLock().unlock();
        }

        // update all registered GUI logs on EDT
        notifyLogs(newLogStr);

        return new AvailableSeatsResult(newLogStr, copy);
    }

    @Override
    public ReservationResult makeReservation(String name, int seatNo, long customerId) throws InterruptedException {
        // write lock for reservation changes
        String newLogStr;
        boolean success = false;
        lock.writeLock().lock();
        try {
            String time = "Time: " + LocalTime.now() + "\n";
            StringBuilder newLog = new StringBuilder();
            newLog.append("Receive Reservation: ").append(time).append(name).append('\n');

            if (bookedCount == this.seatCount()) {
                newLog.append("No more seats available on the flight currently!\n");
            } else if (!this.seats.containsKey(seatNo)) {
                newLog.append("Seat number ").append(seatNo).append(" does not exist.\n");
            } else {
                newLog.append(name).append(" tries to book seat ").append(seatNo).append('\n');
                if (this.seats.get(seatNo) == 0L) {
                    this.seats.put(seatNo, customerId);
                    bookedCount++;
                    newLog.append("Booked seat number ").append(seatNo).append(" successfully.\n");
                    success = true;
                } else {
                    newLog.append("Could not book seat ").append(seatNo).append(" — already booked.\n");
                    success = false;
                }
            }
            newLogStr = newLog.toString();
        } finally {
            lock.writeLock().unlock();
        }

        // update GUI logs safely
        notifyLogs(newLogStr);

        return new ReservationResult(success, newLogStr);
    }

    @Override
    public void registerClientLog(JTextArea s) {
        if (s == null) return;
        clientLogs.add(s);
    }

    @Override
    public int seatCount() {
        return this.seats.size();
    }

    private void notifyLogs(String message) {
        if (message == null) return;
        synchronized (clientLogs) {
            for (JTextArea t : clientLogs) {
                if (t != null) {
                    SwingUtilities.invokeLater(() -> t.append(message));
                }
            }
        }
    }
}
