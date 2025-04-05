package ru.rsreu.stockexchange.data;

import java.math.BigDecimal;

public class OrderEvent {
    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

}
