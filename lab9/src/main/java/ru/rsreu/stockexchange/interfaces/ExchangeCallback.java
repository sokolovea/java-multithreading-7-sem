package ru.rsreu.stockexchange.interfaces;

import java.math.BigDecimal;

public interface ExchangeCallback {
    void completeWithFullCompletion(BigDecimal price, BigDecimal amount);
    void completeWithPartialCompletion(BigDecimal price, BigDecimal amount);
    void markAsQueued();
    void cancelOrder();

}
