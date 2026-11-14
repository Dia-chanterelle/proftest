package com.proftest.server.network;

import com.proftest.server.dao.TestAttemptDao;
import com.proftest.server.model.TestAttempt;
import com.proftest.server.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Планировщик, который каждую минуту проверяет незавершённые попытки
 * и переводит просроченные в статус EXPIRED.
 * Паттерн Singleton — единственный экземпляр на сервер.
 */
public class AttemptExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AttemptExpiryScheduler.class);
    private static volatile AttemptExpiryScheduler instance;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "attempt-expiry-scheduler");
                t.setDaemon(true);
                return t;
            });

    private AttemptExpiryScheduler() {}

    public static AttemptExpiryScheduler getInstance() {
        if (instance == null) {
            synchronized (AttemptExpiryScheduler.class) {
                if (instance == null) {
                    instance = new AttemptExpiryScheduler();
                }
            }
        }
        return instance;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::expireStaleAttempts, 60, 60, TimeUnit.SECONDS);
        logger.info("AttemptExpiryScheduler started — checking every 60 seconds");
    }

    public void stop() {
        scheduler.shutdownNow();
        logger.info("AttemptExpiryScheduler stopped");
    }

    private void expireStaleAttempts() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getInstance().getSessionFactory().openSession();
            tx = session.beginTransaction();

            List<TestAttempt> inProgress = session.createQuery(
                    "SELECT ta FROM TestAttempt ta " +
                    "JOIN FETCH ta.assignment a JOIN FETCH a.test t " +
                    "WHERE ta.status = :status",
                    TestAttempt.class)
                    .setParameter("status", TestAttempt.Status.IN_PROGRESS)
                    .list();

            LocalDateTime now = LocalDateTime.now();
            int expired = 0;

            for (TestAttempt ta : inProgress) {
                int limitMinutes = ta.getAssignment().getTest().getTimeLimitMinutes();
                LocalDateTime deadline = ta.getStartTime().plusMinutes(limitMinutes);
                if (deadline.isBefore(now)) {
                    ta.setStatus(TestAttempt.Status.EXPIRED);
                    ta.setEndTime(deadline);
                    session.merge(ta);
                    expired++;
                    logger.info("Attempt {} expired (user={}, test={})",
                            ta.getAttemptId(),
                            ta.getUser().getLogin(),
                            ta.getAssignment().getTest().getTitle());
                }
            }

            tx.commit();
            if (expired > 0) {
                logger.info("Expired {} stale attempt(s)", expired);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            logger.error("Error during attempt expiry check: {}", e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
