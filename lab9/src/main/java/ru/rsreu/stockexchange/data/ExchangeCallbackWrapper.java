package ru.rsreu.stockexchange.data;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.rsreu.stockexchange.enums.ExchangeStatus;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class ExchangeCallbackWrapper {
    private ExchangeStatus exchangeStatus;
    private ExchangeCallback callback;
    private BigDecimal price;
    private BigDecimal amount;

    public ExchangeCallbackWrapper() {

    }

    public ExchangeCallbackWrapper(ExchangeCallback callback, ExchangeStatus exchangeStatus, BigDecimal price, BigDecimal amount) {
        this.price = price;
        this.amount = amount;
        this.callback = callback;
        this.exchangeStatus = exchangeStatus;
    }

    public void setData(ExchangeCallback callback, ExchangeStatus exchangeStatus, BigDecimal price, BigDecimal amount) {
        this.price = price;
        this.amount = amount;
        this.callback = callback;
        this.exchangeStatus = exchangeStatus;
    }

    public void completeWithFullCompletion() {
        if (callback != null) {
            callback.completeWithFullCompletion(price, amount);
        }
    }
    public void completeWithPartialCompletion() {
        if (callback != null) {
            callback.completeWithPartialCompletion(price, amount);
        }
    };
    public void markAsQueued() {
        if (callback != null) {
            callback.markAsQueued();
        }
    }
    public void cancelOrder() {
        if (callback != null) {
            callback.cancelOrder();
        }
    }
}
