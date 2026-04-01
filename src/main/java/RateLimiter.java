import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final int maxRequests;
    private final Duration window;
    private ConcurrentHashMap<String, RequestWindow> requests = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public boolean allowRequest(String userId) {
        RequestWindow requestWindow = requests.computeIfAbsent(userId, id -> new RequestWindow());

        boolean isLocked = false;
        try {
            isLocked = requestWindow.getLock().tryLock(5, TimeUnit.SECONDS);
            if (!isLocked) throw new RuntimeException("Could not acquire a lock");

            Queue<Instant> queue = requestWindow.getQueue();

            Instant now  = Instant.now();
            Instant deadline = now.minusSeconds(window.getSeconds());

            while (!queue.isEmpty()) {
                if(queue.peek().isAfter(deadline)) break;
                queue.poll();
            }
            if (queue.size() == maxRequests) return false;

            queue.add(Instant.now());
            return true;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (isLocked) requestWindow.getLock().unlock();
        }
    }

}
