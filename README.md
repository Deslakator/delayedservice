# delayedservice

Сервис который решает следующую задачу:

```sh
На вход поступают пары (LocalDateTime, Callable). Нужно реализовать систему, которая будет 
выполнять Callable для каждого пришедшего события в указанном LocalDateTime, либо как можно
скорее в случае если система перегружена и не успевает все выполнять (имеет беклог). 
Задачи должны выполняться в порядке согласно значению LocalDateTime либо в порядке прихода 
события для равных LocalDateTime. События могут приходить в произвольном порядке и 
добавление новых пар (LocalDateTime, Callable) может вызываться из разных потоков.
```

Сервис построен на DelayQueue. Гарантируется постановка задач по времени указанном в LocalDateTime, если время
одинаково то в порядке прихода. 

Задачи ставятся в ExecutorService который можно передать через констркутор. По умолчанию использован SingleThreadExecutor 
для гарантии выполнения задач последовательно.
