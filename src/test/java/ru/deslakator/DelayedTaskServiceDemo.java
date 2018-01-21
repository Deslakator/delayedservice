package ru.deslakator;
/*
 * Created by DeSlakator on 21.01.2018.
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 4 потока будут одновременно ставить задачи в наш сервис
 *
 * Задачи из первого и третьего потока должны будут выполнятся через
 * 4 секунды от момента создания
 *
 * Задачи из второго и четвертого потока должны будут выполнятся через
 * 2 секунды от момента создания
 *
 * Каждый поток ставит по 3 задач
 *
 * На выходе мы должны увидеть что для одинаковых StartTime, в консоли NumSetToQueue
 * увеличивается
 */
public class DelayedTaskServiceDemo {

    private final static int qntTaskForThread = 3;

    public static void main(String[] args) throws InterruptedException {
        new DelayedTaskServiceDemo().demo();
    }

    private final ExecutorService delayedTaskExecutor = Executors.newSingleThreadExecutor();
    private final DelayedTaskService delayedTaskService = new DelayedTaskService(delayedTaskExecutor);

    private final AtomicInteger countSetToScheduler = new AtomicInteger(0);

    public void demo() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(delayedTaskService);

        executor.submit(new TaskCreater(1, 4));
        executor.submit(new TaskCreater(2, 2));
        executor.submit(new TaskCreater(3, 4));
        executor.submit(new TaskCreater(4, 2));
        TimeUnit.SECONDS.sleep(5);
        delayedTaskExecutor.shutdownNow();
        executor.shutdownNow();
    }


    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss:nnnnnnnnn");

    private String p(LocalDateTime time) {
        return time.format(formatter);
    }
    private class TaskCreater implements Runnable {

        private int numThread;
        private int qntSecondIncrement;
        public TaskCreater(int numThread, int qntSecondIncrement) {
            this.numThread = numThread;
            this.qntSecondIncrement = qntSecondIncrement;
        }

        @Override
        public void run() {
            LocalDateTime time = LocalDateTime.now().plusSeconds(qntSecondIncrement);
            for (int i = 0; i < qntTaskForThread; i++) {
                setTask(time, numThread);
            }

        }

    }

    // данный метод синхронизирован что бы точно знать в какой последовательности мы ставили задачи в наш сервис
    private synchronized void setTask(LocalDateTime time, int threadNum) {
        int numSetToScheduled = countSetToScheduler.getAndIncrement();
        delayedTaskService.scheduled(time, () -> {
            System.out.println("Thread: " + threadNum + " StartTime: " + p(time) + " RealStart: " + p(LocalDateTime.now()) + " NumSetToQueue: " + numSetToScheduled);
            return null;
        });
    }
}
