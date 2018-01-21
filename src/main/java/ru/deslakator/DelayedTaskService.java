package ru.deslakator;
/*
 * Created by DeSlakator on 21.01.2018.
 */

import com.sun.istack.internal.NotNull;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Данная сервис обеспечивает поставновку Callable по LocalDateTime в
 * ExecutorService (по умолчанию Один поток на исполнение), который
 * передается через конструктор.
 *
 * Если LocalDateTime совпадают, то задачи выполняются в порядке прихода.
 */
public class DelayedTaskService implements Runnable {

    private final DelayQueue<DelayTask> queue = new DelayQueue<>();
    private final ExecutorService execService;

    /**
     * По умолчанию создается однопоточный Executor для выполнения приходящих задач
     */
    public DelayedTaskService() {
        this(Executors.newSingleThreadExecutor());
    }

    /**
     * Можно задать Executor который будет выполнять задачи
     *
     * @param execService ExecutorService выполняющий задачи
     */
    public DelayedTaskService(ExecutorService execService) {
        this.execService = execService;
    }

    /**
     * Постановка задачи
     *
     * @param time     - @NotNull LocalDateTime время выполнения
     * @param callable - @NotNull Задача
     */
    public void scheduled(@NotNull LocalDateTime time, @NotNull Callable callable) {
        DelayTask task = new DelayTask(time, callable);
        queue.offer(task);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                DelayTask task = queue.take();
                execService.submit(task.callable);
            }
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * Вспомогательный класс который отвечает за логику сортировки задач
     */
    static class DelayTask implements Delayed {

        private final static AtomicLong sequenceGen = new AtomicLong(0);

        private final long sequenceId = sequenceGen.getAndIncrement();
        @NotNull
        final LocalDateTime time;
        @NotNull
        final Callable callable;

        DelayTask(@NotNull LocalDateTime time, @NotNull Callable callable) {
            if (time == null || callable == null) {
                throw new IllegalArgumentException("time and callable must be NotNull");
            }
            this.time = time;
            this.callable = callable;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(Duration.between(LocalDateTime.now(), time).toMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (o.equals(this)) {
                return 0;
            }
            if (o instanceof DelayTask) {
                DelayTask other = (DelayTask) o;
                int timeCompare = time.compareTo(other.time);
                if (timeCompare != 0) {
                    return timeCompare;
                }
                return Long.compare(sequenceId, other.sequenceId);
            } else {
                throw new IllegalArgumentException();
            }
        }

        public LocalDateTime getTime() {
            return time;
        }

        public Callable getCallable() {
            return callable;
        }
    }
}
