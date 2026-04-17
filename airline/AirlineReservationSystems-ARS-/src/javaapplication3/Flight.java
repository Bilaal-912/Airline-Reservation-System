package javaapplication3;

/**
 * Simple Flight descriptor.
 */
public class Flight {
    private final String code;      // e.g. "BOM-DXB"
    private final String origin;
    private final String destination;
    private final String displayName; // e.g. "Mumbai → Dubai"

    public Flight(String code, String origin, String destination) {
        this.code = code;
        this.origin = origin;
        this.destination = destination;
        this.displayName = origin + " → " + destination;
    }

    public String getCode() { return code; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDisplayName() { return displayName; }
    @Override
    public String toString() { return displayName + " (" + code + ")"; }
}
