package javaapplication3;

import java.util.List;
import javax.swing.JTextArea;

public interface MultiFlightBuffer {
    AvailableSeatsResult seeAvailableSeats(String flightCode, String requester) throws InterruptedException;
    ReservationResult makeReservation(String flightCode, String requester, int seatNo, long customerId) throws InterruptedException;
    void registerClientLog(String flightCode, JTextArea s);
    int seatCount(String flightCode);
    List<Flight> listFlights();
}
