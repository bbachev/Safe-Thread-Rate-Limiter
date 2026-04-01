import lombok.Getter;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class RequestWindow {
    private Deque<Instant> queue = new ArrayDeque<>();
    private ReentrantLock lock = new ReentrantLock();
}
