package ru.rsreu.stockexchange;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.ExchangeCallbackWrapper;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.data.OrderEvent;
import ru.rsreu.stockexchange.interfaces.StockExchange;

import java.util.concurrent.*;

import java.util.HashSet;

public class StockExchangeFabric {

    private static StockExchangeQueueHandler queueHandler;
    private static StockExchangeDisruptorHandler disruptorHandler;

    public static StockExchange getStockExchangeQueue(HashSet<CurrencyPair> currencyPairs) {
        BlockingQueue<Order> requestQueue = new LinkedBlockingQueue<>();
        queueHandler = new StockExchangeQueueHandler(requestQueue);
        return new StockExchangeQueueAPI(requestQueue, currencyPairs);
    }

    public static StockExchange getStockExchangeSimple(HashSet<CurrencyPair> currencyPairs) {
        return new StockExchangeSimple(currencyPairs);
    }

    public static StockExchange getStockExchangeDisruptor(HashSet<CurrencyPair> currencyPairs) {
        Disruptor<OrderEvent> inputDisruptor = new Disruptor<>(
                OrderEvent::new,
                4096,
                Executors.defaultThreadFactory(),
                ProducerType.MULTI,
                new BusySpinWaitStrategy()
        );
        Disruptor<ExchangeCallbackWrapper> outputDisruptor = new Disruptor<>(
                ExchangeCallbackWrapper::new,
                4096,
                Executors.defaultThreadFactory(),
                ProducerType.SINGLE,
                new BlockingWaitStrategy()
        );
        disruptorHandler = new StockExchangeDisruptorHandler(inputDisruptor, outputDisruptor);
        DisruptorOutputCallbacksHandler disruptorOutputCallbacksHandler = new DisruptorOutputCallbacksHandler(outputDisruptor);
        return new StockExchangeDisruptorAPI(inputDisruptor, currencyPairs);
    }


    public static void stopQueueHandler() {
        if (queueHandler != null) {
            queueHandler.interruptExchange();
        }
        if (disruptorHandler != null) {
            disruptorHandler.interruptExchange();
        }
    }
}
