package com.bnp.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Delivers a saved notification across its outbound channels (Email / SMS / WhatsApp)
 * asynchronously. The in-app copy is already persisted by {@link NotificationService};
 * this runs off the request thread so callers never block on delivery.
 *
 * <p>The channel sends here are simulated (Phase 4 wires real providers); the point is the
 * non-blocking {@code @Async} + {@link CompletableFuture} fan-out and composition.</p>
 */
@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final Executor executor;

    public NotificationDispatcher(@Qualifier("notificationExecutor") Executor executor) {
        this.executor = executor;
    }

    /**
     * Fan out to all channels in parallel and complete when every channel is done.
     * Runs on the notification thread pool; the returned future lets callers (or tests)
     * await delivery, but normal callers fire-and-forget.
     */
    @Async("notificationExecutor")
    public CompletableFuture<Void> dispatch(Notification n) {
        CompletableFuture<Void> email = CompletableFuture.runAsync(() -> send("EMAIL", n), executor);
        CompletableFuture<Void> sms = CompletableFuture.runAsync(() -> send("SMS", n), executor);
        CompletableFuture<Void> whatsapp = CompletableFuture.runAsync(() -> send("WHATSAPP", n), executor);

        return CompletableFuture.allOf(email, sms, whatsapp)
                .whenComplete((v, ex) -> {
                    if (ex != null) {
                        log.warn("Notification #{} dispatch to user {} failed: {}", n.getId(), n.getUserId(), ex.toString());
                    } else {
                        log.info("Notification #{} delivered to user {} on all channels", n.getId(), n.getUserId());
                    }
                });
    }

    /** Simulates a single channel send (replace with a real provider in Phase 4). */
    private void send(String channel, Notification n) {
        try {
            Thread.sleep(120); // stand-in for network latency to the provider
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        log.info("[{}] -> user {} : {}", channel, n.getUserId(), n.getTitle());
    }
}
