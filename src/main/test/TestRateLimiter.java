import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TestRateLimiter {
    @Test
    public void testAllowRequestShouldReturnFalse() {
        RateLimiter limiter = new RateLimiter(3, Duration.ofSeconds(10));
        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertFalse(limiter.allowRequest("user1"));

    }

    @Test
    public void testAllowRequestShouldAllowUserAfterDuration() {
        RateLimiter limiter = new RateLimiter(3, Duration.ofSeconds(10));
        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertTrue(limiter.allowRequest("user1"));
        assertFalse(limiter.allowRequest("user1"));

        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertTrue(limiter.allowRequest("user1"));
    }

    @Test
    public void testConcurrentRequestsOneUser() throws InterruptedException {
        int maxRequests = 5;
        RateLimiter limiter = new RateLimiter(maxRequests, Duration.ofSeconds(10));

        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        for(int i = 0; i < numberOfThreads; i++){
            executorService.submit(() -> {
                try {
                    boolean result = limiter.allowRequest("user1");
                    if (result) success.incrementAndGet();
                    else fail.incrementAndGet();
                    latch.countDown();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(maxRequests, success.get());
        assertEquals(numberOfThreads - maxRequests, fail.get());
    }

    @Test
    public void testConcurrentRequestsMultiUser() throws InterruptedException {
        int maxRequests = 5;
        RateLimiter limiter = new RateLimiter(maxRequests, Duration.ofSeconds(10));

        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        for(int i = 0; i < numberOfThreads; i++){
            final int userIndex = i;
            executorService.submit(() -> {
                try {
                    boolean result = limiter.allowRequest("user" + userIndex);
                    if (result) success.incrementAndGet();
                    else fail.incrementAndGet();
                    latch.countDown();

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(numberOfThreads, success.get());
    }

    @Test
    public void testUserIndependency() throws InterruptedException {
        int limiterMaxRequest = 5;

        RateLimiter limiter = new RateLimiter(limiterMaxRequest, Duration.ofSeconds(10));

        AtomicInteger user1Success = new AtomicInteger(0);
        AtomicInteger user2Success = new AtomicInteger(0);

        int cycles = 7;
        for (int i = 0; i < cycles; i++){
            if (limiter.allowRequest("user1")) user1Success.getAndIncrement();
            if (limiter.allowRequest("user2")) user2Success.getAndIncrement();
        }

        assertEquals(limiterMaxRequest, user1Success.get());
        assertEquals(limiterMaxRequest, user2Success.get());
    }
}
