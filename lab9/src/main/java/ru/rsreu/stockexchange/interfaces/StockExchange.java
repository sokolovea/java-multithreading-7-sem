package ru.rsreu.stockexchange.interfaces;


import ru.rsreu.stockexchange.enums.OperationStatusEnum;
import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Universal stock exchange interface.
 * Provides methods for trading orders? managing clients and their wallets.
 */
public interface StockExchange {

    /**
     * Places an order in the stock exchange.
     *
     * @param parClientId the id for client creating the order.
     * @param parOrderTypeEnum the type of order (Buy or Sell).
     * @param parCurrencyPair the currency pair for the transaction (base and quote currencies).
     * @param parPrice the limit price for the order.
     * @param parAmount the quantity of the order.
     * @return an operation status indicating the success or failure of the order creation.
     */
    OperationStatusEnum createOrder(long parClientId, OrderTypeEnum parOrderTypeEnum, CurrencyPair parCurrencyPair,
                              BigDecimal parPrice, BigDecimal parAmount, ExchangeCallback callback) throws InterruptedException;


    /**
     * Stops exchange and close all opened orders.
     */
    void closeExchange();
}
