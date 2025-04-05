package ru.rsreu.stockexchange;

import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StockExchangeQueueHandler {
    private final PriorityQueue<Order> buyOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    private final PriorityQueue<Order> sellOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    private final BlockingQueue<Order> requestQueue;
    private final Thread thread;
    private static int counter = 0;

    public StockExchangeQueueHandler(BlockingQueue<Order> requestQueue) {
        this.requestQueue = requestQueue;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleOrder();
                } catch (InterruptedException ignored) {
                    System.out.println("Thread was interrupted");
                }
            }
        });
        thread.start();
    }

    private void handleOrder() throws InterruptedException {
        boolean isBreak = false;
        while (true) {
            counter++;
            Order newOrder = requestQueue.take();
            PriorityQueue<Order> market = newOrder.getOrderTypeEnum() == OrderTypeEnum.Buy ? this.sellOrders : this.buyOrders;

            // Матчинг с существующими ордерами
            Iterator<Order> iterator = market.iterator();
            while (iterator.hasNext()) {
                Order eachMarketOrder = iterator.next();
                if (eachMarketOrder.isMatching(newOrder)) {
                    BigDecimal tradeAmount = eachMarketOrder.getAmount().min(newOrder.getAmount());
                    BigDecimal price = eachMarketOrder.getPrice();

                    // Обновление оставшегося количества
                    newOrder.setAmount(newOrder.getAmount().subtract(tradeAmount));
                    eachMarketOrder.setAmount(eachMarketOrder.getAmount().subtract(tradeAmount));

                    // Завершение ордеров
                    if (eachMarketOrder.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                        eachMarketOrder.getExchangeStatusCallback().completeWithFullCompletion(price, tradeAmount);
                        iterator.remove();
                    } else {
                        eachMarketOrder.getExchangeStatusCallback().completeWithPartialCompletion(price, tradeAmount);
                    }

                    if (newOrder.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                        newOrder.getExchangeStatusCallback().completeWithFullCompletion(price, tradeAmount);
                        isBreak = true;
                        break;
                    } else {
                        newOrder.getExchangeStatusCallback().completeWithPartialCompletion(price, tradeAmount);
                    }
                }
            }
            if (!isBreak) {
                PriorityQueue<Order> ordersQueue = newOrder.getOrderTypeEnum() == OrderTypeEnum.Buy ? this.buyOrders : this.sellOrders;
                ordersQueue.add(newOrder);
                newOrder.getExchangeStatusCallback().markAsQueued();
            }
            isBreak = false;
        }
    }

    public void interruptExchange() {
        thread.interrupt();
        System.out.println("counter = " + counter);
    }
}
