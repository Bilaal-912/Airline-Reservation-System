package javaapplication3;

import java.util.Map;

public interface FlightBuffer {
    AvailableSeatsResult seeAvailableSeats(String name) throws InterruptedException;
    ReservationResult makeReservation(String name, int seatNo, long customerId) throws InterruptedException;
    void registerClientLog(javax.swing.JTextArea s);
    int seatCount();
}
