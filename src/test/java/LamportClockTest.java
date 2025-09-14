import static org.junit.Assert.*;

import assignment2.LamportClock;
import org.junit.Before;
import org.junit.Test;

public class LamportClockTest {
    private LamportClock clock;

    @Before
    public void setUp() {
        clock = new LamportClock();
    }

    @Test
    public void testInitialTime() {
        assertEquals(0, clock.getTime());
    }

    @Test
    public void testTickIncrements() {
        int t1 = clock.tick();
        int t2 = clock.tick();
        assertTrue(t2 > t1);
    }

    @Test
    public void testOnReceiveUpdatesCorrectly() {
        clock.tick();
        clock.onReceive(5);
        assertEquals(6, clock.getTime());
    }

    @Test
    public void testOnReceiveWithLowerRemote() {
        clock.tick();
        clock.onReceive(0);
        assertEquals(2, clock.getTime());
    }
}