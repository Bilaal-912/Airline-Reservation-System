package javaapplication3;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Multi-flight server: keeps a seat map per flight and a list of logs per flight.
 * Thread-safe via read/write locks for each flight.
 */
public class MultiFlightServer implements MultiFlightBuffer {
    // For simplicity each flight uses its own ReentrantReadWriteLock stored here
    private final Map<String, ReentrantReadWriteLock> locks = new HashMap<>();
    private final Map<String, HashMap<Integer, Long>> flightsSeats = new HashMap<>();
    private final Map<String, List<JTextArea>> flightLogs = new HashMap<>();
    private final Map<String, Integer> bookedCounts = new HashMap<>();
    private final List<Flight> flightList = new ArrayList<>();

    // default seat count per flight
    private final int defaultSeatCount = 24;

    public MultiFlightServer() {
        // initialize flights
        addFlight(new Flight("BOM-DXB", "Mumbai", "Dubai"));
        addFlight(new Flight("BOM-GOI", "Mumbai", "Goa"));
        addFlight(new Flight("BOM-DEL", "Mumbai", "Delhi"));
    }

    private void addFlight(Flight f) {
        flightList.add(f);
        String code = f.getCode();
        locks.put(code, new ReentrantReadWriteLock(true));
        HashMap<Integer, Long> seats = new HashMap<>();
        for (int i = 1; i <= defaultSeatCount; i++) seats.put(i, 0L);
        flightsSeats.put(code, seats);
        flightLogs.put(code, Collections.synchronizedList(new ArrayList<>()));
        bookedCounts.put(code, 0);
    }

    @Override
    public AvailableSeatsResult seeAvailableSeats(String flightCode, String requester) throws InterruptedException {
        ReentrantReadWriteLock lock = locks.get(flightCode);
        if (lock == null) {
            return new AvailableSeatsResult("Flight not found: " + flightCode + "\n", new HashMap<>());
        }
        String logMessage;
        HashMap<Integer, Long> copy;
        lock.readLock().lock();
        try {
            String time = "Time: " + LocalTime.now() + "\n";
            StringBuilder sb = new StringBuilder();
            sb.append(time).append(requester).append("\n");
            sb.append("Server update: Available seats for ").append(flightCode).append("\n");
            logMessage = sb.toString();
            copy = new HashMap<>(flightsSeats.get(flightCode)); // defensive copy
        } finally {
            lock.readLock().unlock();
        }
        notifyLogs(flightCode, logMessage);
        return new AvailableSeatsResult(logMessage, copy);
    }

    @Override
    public ReservationResult makeReservation(String flightCode, String requester, int seatNo, long customerId) throws InterruptedException {
        ReentrantReadWriteLock lock = locks.get(flightCode);
        if (lock == null) {
            return new ReservationResult(false, "Flight not found: " + flightCode + "\n");
        }
        String logMessage;
        boolean success = false;
        lock.writeLock().lock();
        try {
            String time = "Time: " + LocalTime.now() + "\n";
            StringBuilder sb = new StringBuilder();
            sb.append("Reservation attempt: ").append(time).append(requester).append("\n");
            int booked = bookedCounts.getOrDefault(flightCode, 0);
            if (booked == seatCount(flightCode)) {
                sb.append("No more seats available on flight ").append(flightCode).append("\n");
            } else if (!flightsSeats.get(flightCode).containsKey(seatNo)) {
                sb.append("Seat ").append(seatNo).append(" does not exist on ").append(flightCode).append("\n");
            } else {
                sb.append(requester).append(" tries to book seat ").append(seatNo).append("\n");
                if (flightsSeats.get(flightCode).get(seatNo) == 0L) {
                    flightsSeats.get(flightCode).put(seatNo, customerId);
                    bookedCounts.put(flightCode, booked + 1);
                    sb.append("Booked seat ").append(seatNo).append(" on ").append(flightCode).append(" successfully.\n");
                    success = true;
                } else {
                    sb.append("Could not book seat ").append(seatNo).append(" — already booked.\n");
                }
            }
            logMessage = sb.toString();
        } finally {
            lock.writeLock().unlock();
        }
        notifyLogs(flightCode, logMessage);
        return new ReservationResult(success, logMessage);
    }

    @Override
    public void registerClientLog(String flightCode, JTextArea s) {
        if (s == null) return;
        List<JTextArea> list = flightLogs.get(flightCode);
        if (list != null) {
            list.add(s);
        }
    }

    @Override
    public int seatCount(String flightCode) {
        HashMap<Integer, Long> map = flightsSeats.get(flightCode);
        return map == null ? 0 : map.size();
    }

    @Override
    public List<Flight> listFlights() {
        return Collections.unmodifiableList(flightList);
    }

    private void notifyLogs(String flightCode, String message) {
        if (message == null) return;
        List<JTextArea> logs = flightLogs.get(flightCode);
        if (logs == null) return;
        synchronized (logs) {
            for (JTextArea t : logs) {
                if (t != null) {
                    SwingUtilities.invokeLater(() -> t.append(message));
                }
            }
        }
    }
}
