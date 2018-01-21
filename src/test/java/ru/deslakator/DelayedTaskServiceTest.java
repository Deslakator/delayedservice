package ru.deslakator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/*
 * Created by DeSlakator on 21.01.2018.
 */
public class DelayedTaskServiceTest {

    private ExecutorService processorExec;
    private ExecutorService mockExecServ;
    private DelayedTaskService delayedTaskService;

    @Before
    public void setUp() throws Exception {
        processorExec = Executors.newSingleThreadExecutor();
        mockExecServ = mock(ExecutorService.class);
        delayedTaskService = new DelayedTaskService(mockExecServ);
    }

    @Test
    public void oneCallabele_submited() throws InterruptedException {
        Callable stubCallale = () -> null;
        delayedTaskService.scheduled(LocalDateTime.now(), stubCallale);
        processorExec.submit(delayedTaskService);
        TimeUnit.MILLISECONDS.sleep(100);
        verify(mockExecServ, times(1)).submit(stubCallale);
    }

    @Test
    public void twoCallable_reverseSumbit_byTime() throws InterruptedException {
        Callable firstCallable = () -> null;
        Callable secondCallable = () -> null;
        delayedTaskService.scheduled(LocalDateTime.now().plusSeconds(1), firstCallable);
        delayedTaskService.scheduled(LocalDateTime.now(), secondCallable);
        processorExec.submit(delayedTaskService);
        TimeUnit.MILLISECONDS.sleep(100);
        InOrder inOrder = inOrder(mockExecServ, mockExecServ);
        inOrder.verify(mockExecServ).submit(secondCallable);
        TimeUnit.MILLISECONDS.sleep(1000);
        inOrder.verify(mockExecServ).submit(firstCallable);
    }

    @Test
    public void twoCallable_reverseSumbit_byOrderSet() throws InterruptedException {
        Callable firstCallable = () -> null;
        Callable secondCallable = () -> null;
        delayedTaskService.scheduled(LocalDateTime.now(), firstCallable);
        delayedTaskService.scheduled(LocalDateTime.now(), secondCallable);
        processorExec.submit(delayedTaskService);
        TimeUnit.MILLISECONDS.sleep(100);
        InOrder inOrder = inOrder(mockExecServ, mockExecServ);
        inOrder.verify(mockExecServ, times(1)).submit(firstCallable);
        inOrder.verify(mockExecServ, times(1)).submit(secondCallable);
    }

    @Test
    public void oneCallabele_submited_delayOneSecond() throws InterruptedException {
        Callable stubCallale = () -> null;
        delayedTaskService.scheduled(LocalDateTime.now().plusSeconds(1), stubCallale);
        processorExec.submit(delayedTaskService);
        TimeUnit.MILLISECONDS.sleep(950);
        verify(mockExecServ, never()).submit(stubCallale);
        TimeUnit.MILLISECONDS.sleep(200);
        verify(mockExecServ, times(1)).submit(stubCallale);
    }

}