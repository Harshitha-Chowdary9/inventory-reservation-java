import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Simulation {
    public static void main(String[] args) throws Exception {
        Inventory inventory = new Inventory(Map.of("DEMO-SHOE", 25));
        ExecutorService workers = Executors.newFixedThreadPool(8);
        AtomicInteger accepted = new AtomicInteger();
        for (int n = 0; n < 100; n++) {
            final String id = "order-" + n;
            workers.submit(() -> {
                try { inventory.reserve(id, "DEMO-SHOE", 1); accepted.incrementAndGet(); }
                catch (IllegalStateException expected) { /* stock exhausted */ }
            });
        }
        workers.shutdown();
        if (!workers.awaitTermination(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout");
        System.out.println("Fictional simulation: accepted=" + accepted + ", available=" + inventory.available("DEMO-SHOE"));
        System.out.print(inventory.metrics());
    }
}
