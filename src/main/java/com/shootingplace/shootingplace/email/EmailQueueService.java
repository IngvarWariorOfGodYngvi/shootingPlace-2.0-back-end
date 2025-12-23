package com.shootingplace.shootingplace.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class EmailQueueService {
    private final Queue<EmailQueue> queue = new ConcurrentLinkedQueue<>();
    private final Logger LOG = LogManager.getLogger(getClass());

    public void enqueue(EmailQueue item) {
        queue.add(item);
        LOG.info("Dodano do kolejki wysyłania: {}", item.getRequest().getTo());
    }

    EmailQueue dequeue() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

}
