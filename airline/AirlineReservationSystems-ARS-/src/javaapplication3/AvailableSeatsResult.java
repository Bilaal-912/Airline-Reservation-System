package javaapplication3;

import java.util.HashMap;

public class AvailableSeatsResult {
    private final String message;
    private final HashMap<Integer, Long> seats;

    public AvailableSeatsResult(String message, HashMap<Integer, Long> seats) {
        this.message = message;
        this.seats = seats;
    }

    public String getMessage() { return message; }
    public HashMap<Integer, Long> getSeats() { return seats; }
}
