import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class InventoryTest {
    private static int checks;
    static void check(boolean condition) {
        checks++; if (!condition) throw new AssertionError("Check " + checks + " failed");
    }
    static void fails(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); } catch (Throwable failure) { check(type.isInstance(failure)); return; }
        throw new AssertionError("Expected " + type.getSimpleName());
    }
    public static void main(String[] args) throws Exception {
        Inventory i = new Inventory(Map.of("A", 10, "B", 3));
        Inventory.Reservation r = i.reserve("r1", "A", 2);
        check(i.available("A") == 8);
        check(i.reserve("r1", "A", 2) == r);
        check(i.available("A") == 8);
        fails(IllegalStateException.class, () -> i.reserve("r1", "B", 2));
        fails(IllegalStateException.class, () -> i.reserve("r2", "A", 9));
        check(i.available("A") == 8);
        fails(IllegalArgumentException.class, () -> i.reserve("r3", "A", 0));
        fails(IllegalArgumentException.class, () -> i.reserve("", "A", 1));
        fails(IllegalArgumentException.class, () -> i.reserve("r4", "MISSING", 1));
        fails(IllegalArgumentException.class, () -> new Inventory(Map.of("A", -1)));
        // A failed request did not consume its idempotency key.
        i.reserve("r2", "A", 8);
        check(i.available("A") == 0);
        Inventory concurrent = new Inventory(Map.of("A", 40));
        ExecutorService pool = Executors.newFixedThreadPool(12);
        AtomicInteger successes = new AtomicInteger();
        for (int n = 0; n < 200; n++) {
            final String id = "c" + n;
            pool.submit(() -> { try { concurrent.reserve(id, "A", 1); successes.incrementAndGet(); }
                catch (IllegalStateException expected) { } });
        }
        pool.shutdown(); check(pool.awaitTermination(10, TimeUnit.SECONDS));
        check(successes.get() == 40); check(concurrent.available("A") == 0);
        Inventory retries = new Inventory(Map.of("A", 10));
        ExecutorService retryPool = Executors.newFixedThreadPool(8);
        for (int n=0; n<100; n++) retryPool.submit(() -> retries.reserve("same-id", "A", 1));
        retryPool.shutdown(); check(retryPool.awaitTermination(10, TimeUnit.SECONDS));
        check(retries.available("A") == 9);
        check(retries.metrics().contains("inventory_replayed_total 99"));
        check(concurrent.metrics().contains("inventory_conflicts_total 160"));
        System.out.println("PASS: " + checks + " checks (including concurrent stock and retries)");
    }
}
