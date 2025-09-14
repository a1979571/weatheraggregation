package assignment2;
public class LamportClock {
    private int time;

    public LamportClock() {
        this.time = 0;
    }

    public synchronized int tick() {
        return ++time;
    }

    public synchronized void onReceive(int remoteTime) {
        time = Math.max(time, remoteTime) + 1;
    }

    public synchronized int getTime() {
        return time;
    }
}