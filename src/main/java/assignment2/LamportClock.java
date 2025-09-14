package assignment2;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * LamportClock
 *
 * Implements a simple Lamport logical clock.
 * Provides methods to increment the clock on local events
 * and update it when receiving timestamps from other processes.
 */
public class LamportClock {

    private final AtomicInteger clock;

    /** Initialize Lamport clock to 0 */
    public LamportClock() {
        this.clock = new AtomicInteger(0);
    }

    /**
     * Increment the clock for a local event and return the new value.
     *
     * @return updated clock value
     */
    public int tick() {
        return clock.incrementAndGet();
    }

    /**
     * Update the clock based on a received timestamp.
     * Ensures the Lamport clock is always increasing.
     *
     * @param receivedTime timestamp received from another process
     * @return updated clock value
     */
    public int update(int receivedTime) {
        int current;
        int updated;
        do {
            current = clock.get();
            updated = Math.max(current, receivedTime) + 1;
        } while (!clock.compareAndSet(current, updated));
        return updated;
    }

    /** Get the current value of the Lamport clock */
    public int getTime() {
        return clock.get();
    }

    /** Reset the clock to 0 (optional, useful for testing) */
    public void reset() {
        clock.set(0);
    }

    @Override
    public String toString() {
        return "LamportClock{" + "time=" + clock.get() + '}';
    }
}
