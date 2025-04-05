package ru.rsreu.stockexchange.util;

import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

public class RandomExchangeObjectsGenerator {
    private static final Random RANDOM = new Random();
    private static final MathContext CONTEXT = new MathContext(3, RoundingMode.HALF_UP);

    public static BigDecimal getRandomAmount() {
        double amount = RANDOM.nextDouble() * 1000;
        return new BigDecimal(amount, CONTEXT);
    }

    public static Order generateRandomOrder(Long userId) {

        BigDecimal randomPrice = new BigDecimal(Double.toString(0.5 + (2.0 - 0.5) * RANDOM.nextDouble()), CONTEXT);
        BigDecimal randomAmount = new BigDecimal(Double.toString(100 * RANDOM.nextDouble()), CONTEXT);

        OrderTypeEnum orderTypeEnum = OrderTypeEnum.values()[RANDOM.nextInt(OrderTypeEnum.values().length)];

        CurrencyTypeEnum baseCurrency;
        CurrencyTypeEnum quoteCurrency;

        do {
            baseCurrency = CurrencyTypeEnum.values()[RANDOM.nextInt(CurrencyTypeEnum.values().length)];
            quoteCurrency = CurrencyTypeEnum.values()[RANDOM.nextInt(CurrencyTypeEnum.values().length)];
        } while (baseCurrency == quoteCurrency);

        CurrencyPair currencyPair = new CurrencyPair(baseCurrency, quoteCurrency);
        return new Order(userId, randomPrice, randomAmount, orderTypeEnum, currencyPair, null);
    }
}
