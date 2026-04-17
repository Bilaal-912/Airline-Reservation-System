package javaapplication3;

public class ReservationResult {
    private final boolean success;
    private final String message;

    public ReservationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
