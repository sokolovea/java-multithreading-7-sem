package ru.rsreu.stockexchange;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkerPool;
import ru.rsreu.stockexchange.data.ExchangeCallbackWrapper;
import ru.rsreu.stockexchange.enums.ExchangeStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorOutputCallbacksHandler {
    private final RingBuffer<ExchangeCallbackWrapper> ringBuffer;
    private final WorkerPool<ExchangeCallbackWrapper> workerPool;
    private final ExecutorService executorService;

    public DisruptorOutputCallbacksHandler(Disruptor<ExchangeCallbackWrapper> outputDisruptor) {
        this.ringBuffer = outputDisruptor.getRingBuffer();

        int numThreads = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(numThreads);

        WorkHandler<ExchangeCallbackWrapper>[] handlers = new WorkHandler[numThreads];
        for (int i = 0; i < numThreads; i++) {
            handlers[i] = new EventProcessor();
        }
        this.workerPool = new WorkerPool<>(
                ringBuffer,
                ringBuffer.newBarrier(),
                new CustomExceptionHandler(),
                handlers
        );

        outputDisruptor.start();

        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());

        workerPool.start(executorService);
    }

    public void shutdown() {
        workerPool.drainAndHalt();
        executorService.shutdown();
    }

    private static class EventProcessor implements WorkHandler<ExchangeCallbackWrapper> {
        @Override
        public void onEvent(ExchangeCallbackWrapper event) {
            try {
                ExchangeStatus status = event.getExchangeStatus();
                if (status == ExchangeStatus.SuccessFullExchange) {
                    event.completeWithFullCompletion();
                } else if (status == ExchangeStatus.SuccessPartialExchange) {
                    event.completeWithPartialCompletion();
                } else if (status == ExchangeStatus.Queued) {
                    event.markAsQueued();
                } else if (status == ExchangeStatus.Cancelled) {
                    event.cancelOrder();
                }
            } catch (Exception e) {
                System.err.println("Failed to process event!");
            }
        }
    }

    private static class CustomExceptionHandler implements ExceptionHandler<ExchangeCallbackWrapper> {
        @Override
        public void handleEventException(Throwable ex, long sequence, ExchangeCallbackWrapper event) {
            System.err.println("Exception occurred while processing event: " + event);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            System.err.println("Exception during onStart: " + ex.getMessage());
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            System.err.println("Exception during onShutdown: " + ex.getMessage());
        }
    }

//    public void publishEvent(ExchangeCallbackWrapper event) {
//        long sequence = ringBuffer.next();
//        try {
//            ExchangeCallbackWrapper wrapper = ringBuffer.get(sequence);
//            wrapper.setData(event.getCallback(), event.getExchangeStatus(), event.getPrice(), event.getAmount()); // Копируем данные в ячейку
//        } finally {
//            ringBuffer.publish(sequence);
//        }
//    }
}
