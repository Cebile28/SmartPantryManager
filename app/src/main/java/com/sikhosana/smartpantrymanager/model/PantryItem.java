package com.sikhosana.smartpantrymanager.model;

/**
 * One ingredient the user currently has at home.
 *
 * Represents a single row of the pantry table. Quantity is stored as a double
 * rather than an int because real pantry amounts are fractional - half a litre
 * of milk, 1.5 kg of flour - and the strict-matching rule has to compare these
 * against recipe requirements numerically.
 */
public class PantryItem {

    /** Used for a new item that has not been saved to the database yet. */
    public static final long NO_ID = -1;

    /** Stored in expiryDate when the user chose not to enter one. */
    public static final long NO_EXPIRY = 0L;

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private long expiryDate;      // epoch milliseconds, or NO_EXPIRY
    private long dateAdded;       // epoch milliseconds

    public PantryItem() {
        this.id = NO_ID;
        this.expiryDate = NO_EXPIRY;
        this.dateAdded = System.currentTimeMillis();
    }

    public PantryItem(String name, double quantity, String unit) {
        this();
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public PantryItem(long id, String name, double quantity, String unit,
                      long expiryDate, long dateAdded) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.dateAdded = dateAdded;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(long expiryDate) { this.expiryDate = expiryDate; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    /** True when the user supplied an expiry date for this item. */
    public boolean hasExpiryDate() {
        return expiryDate != NO_EXPIRY;
    }

    /** True when this item has an expiry date that has already passed. */
    public boolean isExpired() {
        return hasExpiryDate() && expiryDate < System.currentTimeMillis();
    }

    /**
     * Whole days until this item expires. Negative when it has already expired.
     * Returns Long.MAX_VALUE when no expiry date was set, so that items without
     * a date always sort last in an "expiring soon" list.
     */
    public long daysUntilExpiry() {
        if (!hasExpiryDate()) {
            return Long.MAX_VALUE;
        }
        long millisRemaining = expiryDate - System.currentTimeMillis();
        return millisRemaining / (1000L * 60 * 60 * 24);
    }

    /** Formatted for display in the pantry list, e.g. "1.5 kg" or "3 pcs". */
    public String getFormattedQuantity() {
        String amount = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)     // 3 rather than 3.0
                : String.valueOf(quantity);
        return unit == null || unit.isEmpty() ? amount : amount + " " + unit;
    }

    @Override
    public String toString() {
        return name + " (" + getFormattedQuantity() + ")";
    }
}