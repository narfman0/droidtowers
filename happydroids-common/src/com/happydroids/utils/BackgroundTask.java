/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class BackgroundTask {
  private static final String TAG = BackgroundTask.class.getSimpleName();
  protected static ExecutorService threadPool;

  protected Thread thread;
  private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

  static {
    threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "BackgroundTaskThread");
        if (uncaughtExceptionHandler != null) {
          thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setDaemon(true);
        return thread;
      }
    });
  }

  public BackgroundTask() {

  }

  public synchronized void beforeExecute() {
  }

  public abstract void execute();

  public synchronized void afterExecute() {
  }

  public final void run() {
    threadPool.submit(new Runnable() {
      public void run() {
        beforeExecute();
        execute();
        afterExecute();
      }
    });
  }

  public static void dispose() {
    if (threadPool != null) {
      threadPool.shutdown();
      Logger.getLogger(TAG).info("Shutting down background tasks...");
      try {
        threadPool.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {
      }
    }
  }

  public static void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
    BackgroundTask.uncaughtExceptionHandler = uncaughtExceptionHandler;
  }
}