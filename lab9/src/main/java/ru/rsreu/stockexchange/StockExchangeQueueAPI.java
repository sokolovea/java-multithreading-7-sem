package ru.rsreu.stockexchange;

import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;
import ru.rsreu.stockexchange.enums.OperationStatusEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;
import ru.rsreu.stockexchange.interfaces.StockExchange;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StockExchangeQueueAPI implements StockExchange {
    private final HashSet<CurrencyPair> availableCurrencyPairs = new HashSet<>();
    private final BlockingQueue<Order> requestQueue;

    public StockExchangeQueueAPI(BlockingQueue<Order> requestQueue, HashSet<CurrencyPair> currencyPairs) {
        this.requestQueue = requestQueue;

        if (currencyPairs != null) {
            availableCurrencyPairs.addAll(currencyPairs);
        }
    }


    public OperationStatusEnum createOrder(long parClientId, OrderTypeEnum parOrderTypeEnum, CurrencyPair parCurrencyPair,
                                     BigDecimal parPrice, BigDecimal parAmount, ExchangeCallback callback) throws InterruptedException {
            if (parPrice == null || parAmount == null || parCurrencyPair == null || parOrderTypeEnum == null) {
                return OperationStatusEnum.Rejected;
            }
            if (parPrice.compareTo(BigDecimal.ZERO) <= 0 || parAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return OperationStatusEnum.Rejected;
            }
            if (parCurrencyPair.getBaseCurrency() == parCurrencyPair.getQuoteCurrency() ||
                    !this.availableCurrencyPairs.contains(parCurrencyPair)) {
                return OperationStatusEnum.Rejected;
            }
            try {
                Order newOrder = new Order(parClientId, parPrice, parAmount, parOrderTypeEnum, parCurrencyPair, callback);
                requestQueue.put(newOrder);
            } finally {
                return OperationStatusEnum.Accepted;
            }
    }

    @Override
    public void closeExchange() {
        for (Order order : this.requestQueue) {
            order.getExchangeStatusCallback().cancelOrder();
        }
        this.requestQueue.clear();
    }
}
