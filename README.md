Implement a thread-safe in-memory rate limiter.
The limiter should restrict how many requests a user can make within a given time window.

public class RateLimiter {
public RateLimiter(int maxRequests, Duration window);
public boolean allowRequest(String userId);
}

Each userId can make at most maxRequests within the given window.
If the limit is exceeded → return false
Otherwise → return true
After the time window passes, old requests should no longer count.

Example:
RateLimiter limiter = new RateLimiter(3, Duration.ofSeconds(10));
limiter.allowRequest("user1"); // true
limiter.allowRequest("user1"); // true
limiter.allowRequest("user1"); // true
limiter.allowRequest("user1"); // false

// After 10 seconds
limiter.allowRequest("user1"); // true
