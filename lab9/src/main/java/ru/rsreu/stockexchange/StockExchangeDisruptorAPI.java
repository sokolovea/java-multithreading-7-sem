package ru.rsreu.stockexchange;

import com.lmax.disruptor.dsl.Disruptor;
import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.data.OrderEvent;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;
import ru.rsreu.stockexchange.enums.OperationStatusEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;
import ru.rsreu.stockexchange.interfaces.StockExchange;

import java.math.BigDecimal;
import java.util.*;

import com.lmax.disruptor.*;

public class StockExchangeDisruptorAPI implements StockExchange {
    private final HashSet<CurrencyPair> availableCurrencyPairs = new HashSet<>();
    private final Disruptor<OrderEvent> disruptor;
    private final RingBuffer<OrderEvent> ringBuffer;

    public StockExchangeDisruptorAPI(Disruptor<OrderEvent> disruptor, HashSet<CurrencyPair> currencyPairs) {
        this.disruptor = disruptor;
        this.ringBuffer = disruptor.getRingBuffer();
        if (currencyPairs != null) {
            this.availableCurrencyPairs.addAll(currencyPairs);
        }
    }

    public  OperationStatusEnum createOrder(long parClientId, OrderTypeEnum parOrderTypeEnum, CurrencyPair parCurrencyPair,
                                           BigDecimal parPrice, BigDecimal parAmount, ExchangeCallback callback) {
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
        Order order = new Order(parClientId, parPrice, parAmount, parOrderTypeEnum, parCurrencyPair, callback);
        long sequence = ringBuffer.next();
        try {
            OrderEvent event = ringBuffer.get(sequence);
            event.setOrder(order);
        } finally {
            ringBuffer.publish(sequence);
        }
        return OperationStatusEnum.Accepted;
    }



    @Override
    public void closeExchange() {
        disruptor.shutdown();
    }
}
