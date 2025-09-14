//import org.junit.Test;
//import org.junit.jupiter.api.*;
//import java.io.*;
//import java.net.Socket;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.concurrent.*;
//
//import static org.junit.Assert.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class AggregationServerTest {
//
//    private AggregationServer server;
//    private Path tempFile;
//    private ExecutorService executor;
//
//    @BeforeAll
//    public void setupServer() throws Exception {
//        tempFile = Files.createTempFile("weatherStore", ".json");
//        server = new AggregationServer(tempFile, 5000);
//
//        executor = Executors.newSingleThreadExecutor();
//        executor.submit(server::start);
//
//        // Give server time to start
//        Thread.sleep(500);
//    }
//
//    @AfterAll
//    public void cleanup() throws Exception {
//        executor.shutdownNow();
//        Files.deleteIfExists(tempFile);
//    }
//
//    private String sendRequest(String request, String body) throws IOException {
//        try (Socket socket = new Socket("localhost", 5000);
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
//
//            out.println(request);
//            if (body != null && !body.isEmpty()) {
//                out.println(body);
//            }
//            out.flush();
//
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = in.readLine()) != null) {
//                response.append(line);
//            }
//            return response.toString().trim();
//        }
//    }
//
//    @Test
//    public void testPutAndGetWeatherEntry() throws IOException {
//        String putJson = "{\"id\":\"loc1\",\"airTemp\":25.0,\"apparentTemp\":26.0,\"cloud\":50," +
//                "\"dewpt\":18.0,\"press\":1010.0,\"relHum\":60.0,\"windDir\":180.0," +
//                "\"windSpdKmh\":15.0,\"windSpdKt\":8.0,\"lamportTime\":0,\"lastUpdatedEpoch\":0}";
//
//        String putResponse = sendRequest("PUT", putJson);
//        assertTrue(putResponse.contains("200") || putResponse.contains("201"));
//
//        String getResponse = sendRequest("GET", null);
//        assertTrue(getResponse.contains("loc1"));
//        assertTrue(getResponse.contains("25.0"));
//    }
//
//    @Test
//    public void testExpiryOfStaleEntries() throws Exception {
//        WeatherEntry entry = new WeatherEntry("loc2", 20.0, 21.0, 30, 15.0, 1005.0, 55.0, 90.0, 10.0, 5.0);
//        server.aggregateEntry(entry);
//
//        // Wait 1 second and expire entries older than 0 ms
//        Thread.sleep(1000);
//        server.removeExpiredEntries(0);
//
//        List<WeatherEntry> entries = server.getWeatherEntries();
//        assertTrue(entries.stream().noneMatch(e -> e.getId().equals("loc2")));
//    }
//
//    @Test
//    public void testLamportOrderingOnConcurrentPuts() throws InterruptedException {
//        WeatherEntry e1 = new WeatherEntry("loc3", 10.0, 10.0, 20, 5.0, 1000.0, 50.0, 0.0, 5.0, 2.0);
//        WeatherEntry e2 = new WeatherEntry("loc3", 15.0, 15.0, 25, 7.0, 1002.0, 55.0, 0.0, 7.0, 3.0);
//
//        ExecutorService pool = Executors.newFixedThreadPool(2);
//        pool.submit(() -> server.aggregateEntry(e1));
//        pool.submit(() -> server.aggregateEntry(e2));
//
//        pool.shutdown();
//        pool.awaitTermination(2, TimeUnit.SECONDS);
//
//        List<WeatherEntry> entries = server.getWeatherEntries();
//        WeatherEntry result = entries.stream().filter(e -> e.getId().equals("loc3")).findFirst().orElse(null);
//        assertNotNull(result);
//
//        // The entry with the highest Lamport time should be the final one
//        assertEquals(15.0, result.getAirTemp(), 0.001);
//    }
//
//    @Test
//    public void testErrorHandlingBadRequest() throws IOException {
//        String response = sendRequest("INVALID_REQUEST", null);
//        assertTrue(response.contains("400"));
//    }
//}
