import assignment2.*;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class ConcurrencyTest {

    private AggregationServer server;
    private Path tempFeed;

    @Before
    public void setUp() throws Exception {
        tempFeed = Files.createTempFile("feed", ".json");
        server = new AggregationServer(tempFeed);
    }

    @Test
    public void testConcurrentPuts() throws InterruptedException {
        int threads = 5;
        CountDownLatch latch = new CountDownLatch(threads);

        Runnable task = () -> {
            WeatherEntry e = new WeatherEntry();
            e.setId("CONC");
            e.setName(Thread.currentThread().getName());
            server.aggregateEntry(e);
            latch.countDown();
        };

        for (int i = 0; i < threads; i++) {
            new Thread(task, "Thread-" + i).start();
        }

        latch.await();

        assertEquals(1, server.weatherEntries.size());
        assertEquals("CONC", server.weatherEntries.get(0).getId());
        assertTrue(server.weatherEntries.get(0).getLamportTime() > 0);
    }
}
