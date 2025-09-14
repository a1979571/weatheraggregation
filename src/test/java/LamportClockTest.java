import assignment2.LamportClock;
import org.junit.Test;
import static org.junit.Assert.*;

public class LamportClockTest {

    @Test
    public void testTickIncrements() {
        LamportClock clock = new LamportClock();
        int t1 = clock.tick();
        int t2 = clock.tick();
        assertEquals(1, t1);
        assertEquals(2, t2);
    }

    @Test
    public void testOnReceiveUpdatesTime() {
        LamportClock clock = new LamportClock();
        clock.tick(); // time = 1
        clock.onReceive(5); // remoteTime = 5
        assertEquals(6, clock.getTime());
    }
}
