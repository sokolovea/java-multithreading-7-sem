package ru.rsreu.stockexchange.data;

import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    private static long idCounter = 0;
    private final long id;
    private final ConcurrentHashMap<CurrencyTypeEnum, BigDecimal> wallet = new ConcurrentHashMap<>();

    public Client() {
        id = idCounter++;
    }

    public long getId() {
        return id;
    }

    // Синхронизированный метод для безопасного получения кошелька
    public synchronized Map<CurrencyTypeEnum, BigDecimal> getWallet() {
        return wallet;
    }
}
