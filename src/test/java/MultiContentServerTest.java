//import org.junit.jupiter.api.*;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.concurrent.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class MultiContentServerTest {
//
//    private static final int PORT = 5679;
//    private static AggregationServer server;
//
//    @BeforeAll
//    static void setup() throws Exception {
//        Path tempFile = Files.createTempFile("weather_multi", ".json");
//        server = new AggregationServer(tempFile);
//        new Thread(() -> {
//            try {
//                server.start(PORT);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//        Thread.sleep(500);
//    }
//
//    @Test
//    void testConcurrentPutLamportOrdering() throws InterruptedException {
//        int numThreads = 5;
//        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
//
//        CountDownLatch latch = new CountDownLatch(numThreads);
//
//        for (int i = 0; i < numThreads; i++) {
//            int id = i;
//            executor.submit(() -> {
//                ContentServer cs = new ContentServer("localhost", PORT);
//                WeatherEntry e = new WeatherEntry();
//                e.setId("CONCURRENT");
//                e.setLamportTime(id); // simulate different Lamport times
//                cs.sendWeatherUpdate(e);
//                latch.countDown();
//            });
//        }
//
//        latch.await();
//        executor.shutdown();
//
//        // Only the entry with highest LamportTime should remain
//        WeatherEntry finalEntry = server.weatherEntries.stream()
//                .filter(e -> e.getId().equals("CONCURRENT"))
//                .findFirst()
//                .orElse(null);
//        assertNotNull(finalEntry);
//        assertEquals(numThreads - 1, finalEntry.getLamportTime());
//    }
//}
