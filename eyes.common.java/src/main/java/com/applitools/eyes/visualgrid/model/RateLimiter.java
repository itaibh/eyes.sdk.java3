package com.applitools.eyes.visualgrid.model;

import com.applitools.eyes.IPutFuture;
import com.applitools.eyes.Logger;
import com.applitools.utils.GeneralUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class RateLimiter {
    private final Logger logger;
    private List<IPutFuture> awaitingTasks = Collections.synchronizedList(new ArrayList<IPutFuture>());
    private List<IPutFuture> runningTasks = Collections.synchronizedList(new ArrayList<IPutFuture>());

    private int maxConcurrentTasks;
    private final Object lock = new Object();

    public void handle(IPutFuture putFuture) {
        synchronized (lock) {
            awaitingTasks.add(putFuture);
            if (!pollingThread.isAlive()) {
                pollingThread.start();
            }
        }
    }

    public boolean isSlotAvailable() {
        return runningTasks.size() < maxConcurrentTasks;
    }

    class RateLimiterRunnable implements Runnable {
        @Override
        public void run() {
            logger.verbose("enter");
            do {

                while (!isSlotAvailable()){
                    GeneralUtils.sleep(250);
                }

                executeTask();

                removeCompletedTasks();

            } while (!awaitingTasks.isEmpty() || !runningTasks.isEmpty());
            logger.verbose("exit");
        }

        private void executeTask() {
            logger.verbose("enter");
            if (awaitingTasks.isEmpty()) return;
            IPutFuture putFuture;
            synchronized (lock) {
                putFuture = awaitingTasks.get(0);
                logger.verbose("executing task " + putFuture);
                runningTasks.add(putFuture);
                awaitingTasks.remove(putFuture);
            }
            try {
                putFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            logger.verbose("exit");
        }

        private void removeCompletedTasks() {
            logger.verbose("enter");
            Iterator<IPutFuture> iterator = runningTasks.iterator();
            while (iterator.hasNext()) {
                IPutFuture future = iterator.next();
                if (future.isDone()) {
                    logger.verbose("removing done task " + future);
                    iterator.remove();
                }
            }
            logger.verbose("exit");
        }
    }

    private Thread pollingThread = new Thread(new RateLimiterRunnable(), "PutThrottler");

    public RateLimiter(Logger logger, int maxConcurrentTasks) {
        this.logger = logger;
        this.maxConcurrentTasks = maxConcurrentTasks;
    }

    public void run() {
        logger.verbose("enter");
        pollingThread.run();
        logger.verbose("exit");
    }
}
