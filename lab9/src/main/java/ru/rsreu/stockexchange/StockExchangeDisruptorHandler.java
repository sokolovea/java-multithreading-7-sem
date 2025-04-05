package ru.rsreu.stockexchange;

import com.lmax.disruptor.dsl.Disruptor;
import ru.rsreu.stockexchange.data.ExchangeCallbackWrapper;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.data.OrderEvent;
import ru.rsreu.stockexchange.enums.ExchangeStatus;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class StockExchangeDisruptorHandler {
    private final PriorityQueue<Order> buyOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    private final PriorityQueue<Order> sellOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice, Comparator.reverseOrder()));
    private final Disruptor<OrderEvent> inputDisruptor;
    private final Disruptor<ExchangeCallbackWrapper> outputDisruptor;

    public StockExchangeDisruptorHandler(Disruptor<OrderEvent> inputDisruptor, Disruptor<ExchangeCallbackWrapper> outputDisruptor) {
        this.inputDisruptor = inputDisruptor;
        this.outputDisruptor = outputDisruptor;

        inputDisruptor.handleEventsWith(this::processOrderEvent);
        inputDisruptor.start();
    }

    /**
     * Обработчик событий входного буфера Disruptor
     */
    private void processOrderEvent(OrderEvent event, long sequence, boolean endOfBatch) {
        Order newOrder = event.getOrder();
        PriorityQueue<Order> market = newOrder.getOrderTypeEnum() == OrderTypeEnum.Buy ? this.sellOrders : this.buyOrders;

        Iterator<Order> iterator = market.iterator();
        while (iterator.hasNext()) {
            Order eachMarketOrder = iterator.next();
            if (eachMarketOrder.isMatching(newOrder)) {
                BigDecimal tradeAmount = eachMarketOrder.getAmount().min(newOrder.getAmount());
                BigDecimal price = eachMarketOrder.getPrice();

                newOrder.setAmount(newOrder.getAmount().subtract(tradeAmount));
                eachMarketOrder.setAmount(eachMarketOrder.getAmount().subtract(tradeAmount));

                // Создаем события для выходного буфера
                publishCallback(eachMarketOrder, price, tradeAmount,
                        eachMarketOrder.getAmount().compareTo(BigDecimal.ZERO) == 0
                                ? ExchangeStatus.SuccessFullExchange
                                : ExchangeStatus.SuccessPartialExchange);

                if (eachMarketOrder.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                    iterator.remove();
                }

                publishCallback(newOrder, price, tradeAmount,
                        newOrder.getAmount().compareTo(BigDecimal.ZERO) == 0
                                ? ExchangeStatus.SuccessFullExchange
                                : ExchangeStatus.SuccessPartialExchange);

                if (newOrder.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            }
        }

        // Добавление нового ордера в очередь
        if (newOrder.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            PriorityQueue<Order> ordersQueue = newOrder.getOrderTypeEnum() == OrderTypeEnum.Buy ? this.buyOrders : this.sellOrders;
            ordersQueue.add(newOrder);

            publishCallback(newOrder, null, null, ExchangeStatus.Queued);
        }
    }

    private void publishCallback(Order order, BigDecimal price, BigDecimal amount, ExchangeStatus exchangeStatus) {
        outputDisruptor.publishEvent((event, sequence) -> {
            event.setData(order.getExchangeStatusCallback(), exchangeStatus,  price, amount);
        });
    }

    public void interruptExchange() {
        inputDisruptor.shutdown();
        outputDisruptor.shutdown();
    }

}

