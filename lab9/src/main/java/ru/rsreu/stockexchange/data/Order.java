package ru.rsreu.stockexchange.data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.concurrent.CompletableFuture;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.rsreu.stockexchange.enums.ExchangeStatus;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;

@Getter
@Setter
@EqualsAndHashCode
public class Order implements Comparable<Order> {
    private static long idCounter = 0;
    private long orderId;
    private long clientId;
    private OrderTypeEnum orderTypeEnum;
    private CurrencyPair currencyPair;
    private BigDecimal price;
    private BigDecimal amount;
    private Timestamp timestamp;
    private final ExchangeCallback exchangeStatusCallback;

    public Order(Long clientId, BigDecimal price, BigDecimal amount, OrderTypeEnum orderTypeEnum,
                 CurrencyPair currencyPair, ExchangeCallback exchangeStatusCallback) {
        this.orderId = idCounter++;
        this.clientId = clientId;
        this.price = price;
        this.amount = amount;
        this.orderTypeEnum = orderTypeEnum;
        this.currencyPair = currencyPair;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.exchangeStatusCallback = exchangeStatusCallback;
    }

    public boolean isMatching(Order eachOrder) {
        if (eachOrder.getClientId() == this.clientId || orderTypeEnum == eachOrder.getOrderTypeEnum()) {
            return false;
        }

        if (!this.currencyPair.equals(eachOrder.getCurrencyPair())) {
            return false;
        }

        if (this.orderTypeEnum == OrderTypeEnum.Buy) {
            return this.price.compareTo(eachOrder.getPrice()) >= 0;
        } else {
            return this.price.compareTo(eachOrder.getPrice()) <= 0;
        }
    }

    @Override
    public String toString() {
        return String.format("Order{clientId=%d, orderType=%s, currencyPair=%s, price=%s, amount=%s, timestamp=%s}",
                clientId, orderTypeEnum, currencyPair, price.toPlainString(), amount.toPlainString(), timestamp);
    }

    @Override
    public int compareTo(Order other) {
        return this.price.compareTo(other.getPrice());
    }
}
