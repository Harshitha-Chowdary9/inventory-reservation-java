import java.util.HashMap;
import java.util.Map;

/** In-memory, single-process inventory. Quantities are whole units. */
public final class Inventory {
    public static final class Reservation {
        public final String sku;
        public final int quantity;
        Reservation(String sku, int quantity) { this.sku = sku; this.quantity = quantity; }
    }
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, Reservation> reservations = new HashMap<>();
    private long requests, conflicts, successful, replayed;

    public Inventory(Map<String, Integer> initial) {
        initial.forEach((sku, quantity) -> {
            if (sku == null || sku.isBlank() || quantity == null || quantity < 0)
                throw new IllegalArgumentException("Invalid initial stock");
            stock.put(sku, quantity);
        });
    }

    // One lock covers availability checking, stock update and idempotency recording.
    public synchronized Reservation reserve(String requestId, String sku, int quantity) {
        requests++;
        if (requestId == null || requestId.isBlank() || sku == null || sku.isBlank() || quantity <= 0)
            throw new IllegalArgumentException("Nonblank request ID/SKU and positive quantity required");
        Reservation previous = reservations.get(requestId);
        if (previous != null) {
            if (!previous.sku.equals(sku) || previous.quantity != quantity) {
                conflicts++;
                throw new IllegalStateException("Idempotency key reused for a different request");
            }
            replayed++;
            return previous;
        }
        Integer available = stock.get(sku);
        if (available == null) throw new IllegalArgumentException("Unknown SKU");
        if (available < quantity) {
            conflicts++;
            throw new IllegalStateException("Insufficient stock");
        }
        Reservation reservation = new Reservation(sku, quantity);
        stock.put(sku, available - quantity);
        reservations.put(requestId, reservation);
        successful++;
        return reservation;
    }

    public synchronized int available(String sku) {
        if (!stock.containsKey(sku)) throw new IllegalArgumentException("Unknown SKU");
        return stock.get(sku);
    }

    public synchronized String metrics() {
        return "inventory_requests_total " + requests + "\n"
            + "inventory_conflicts_total " + conflicts + "\n"
            + "inventory_reserved_total " + successful + "\n"
            + "inventory_replayed_total " + replayed + "\n";
    }
}
