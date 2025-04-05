package ru.rsreu.stockexchange;

import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.OperationStatusEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;
import ru.rsreu.stockexchange.interfaces.StockExchange;

import java.math.BigDecimal;
import java.util.*;

public class StockExchangeSimple implements StockExchange {
    private final HashSet<CurrencyPair> availableCurrencyPairs = new HashSet<>();
    private final PriorityQueue<Order> buyOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    private final PriorityQueue<Order> sellOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice, Comparator.reverseOrder()));

    private final Object globalLock = new Object();

    public StockExchangeSimple(HashSet<CurrencyPair> availableCurrencyPairs) {
        if (availableCurrencyPairs != null) {
            this.availableCurrencyPairs.addAll(availableCurrencyPairs);
        }
    }

//    public long createClient() {
//        synchronized (globalLock) {
//            Client client = new Client();
//            clients.put(client.getId(), client);
//            return client.getId();
//        }
//    }

//    public OperationStatusEnum changeClientWallet(long clientId, CurrencyTypeEnum currencyTypeEnum, BigDecimal amount) {
//        synchronized (globalLock) {
//            if (currencyTypeEnum == null || amount == null) {
//                return OperationStatusEnum.Failed;
//            }
//            try {
//                BigDecimal copyAmount = new BigDecimal(amount.toString());
//                BigDecimal oldAmount = clients.get(clientId).getWallet().getOrDefault(currencyTypeEnum, BigDecimal.ZERO);
//                BigDecimal newAmount = oldAmount.add(copyAmount);
//                clients.get(clientId).getWallet().put(currencyTypeEnum, newAmount);
//                return OperationStatusEnum.OK;
//            } catch (Exception ignored) {
//                return OperationStatusEnum.Failed;
//            }
//        }
//    }

    public OperationStatusEnum createOrder(long parClientId, OrderTypeEnum parOrderTypeEnum, CurrencyPair parCurrencyPair,
                                           BigDecimal parPrice, BigDecimal parAmount, ExchangeCallback callback) {
        synchronized (globalLock) {
            // Валидация входных данных
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

            // Создание нового ордера
            Order newOrder = new Order(parClientId, parPrice, parAmount, parOrderTypeEnum, parCurrencyPair, callback);
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
                        break;
                    } else {
                        newOrder.getExchangeStatusCallback().completeWithPartialCompletion(price, tradeAmount);
                    }
                    return OperationStatusEnum.Accepted;
                }
            }

            // Добавление нового ордера в очередь
            PriorityQueue<Order> ordersQueue = newOrder.getOrderTypeEnum() == OrderTypeEnum.Buy ? this.buyOrders : this.sellOrders;
            ordersQueue.add(newOrder);
            newOrder.getExchangeStatusCallback().markAsQueued();
            return OperationStatusEnum.Accepted;
        }
    }


//    public List<Order> getOpenOrders() {
//        synchronized (globalLock) {
//            List<Order> resultList = new LinkedList<>(buyOrders);
//            resultList.addAll(sellOrders);
//            return resultList;
//        }
//    }

//    public Map<CurrencyTypeEnum, BigDecimal> getClientWalletInfo(long clientId) {
//        synchronized (globalLock) {
//            if (clients.get(clientId) == null) {
//                return Collections.emptyMap();
//            }
//            if (clients.containsKey(clientId)) {
//                return clients.get(clientId).getWallet();
//            }
//            return Collections.emptyMap();
//        }
//    }

//    public void printInfo() {
//        synchronized (globalLock) {
//            Map<CurrencyTypeEnum, BigDecimal> totalBalances = getCurrentBalance();
//            System.out.printf("Open orders count = %d: %d to buy, %d to sell.\n",
//                    sellOrders.size() + buyOrders.size(), sellOrders.size(), buyOrders.size());
//            for (CurrencyTypeEnum currency : totalBalances.keySet()) {
//                System.out.println("Currency: " + currency + " - total balance: " + totalBalances.get(currency));
//            }
//        }
//    }
//
//    public Map<CurrencyTypeEnum, BigDecimal> getCurrentBalance() {
//        synchronized (globalLock) {
//            ConcurrentHashMap<CurrencyTypeEnum, BigDecimal> totalBalances = new ConcurrentHashMap<>();
//
//            for (Client client : clients.values()) {
//                for (CurrencyTypeEnum currencyTypeEnum : client.getWallet().keySet()) {
//                    BigDecimal clientBalance = client.getWallet().get(currencyTypeEnum);
//                    totalBalances.merge(currencyTypeEnum, clientBalance, BigDecimal::add);
//                }
//            }
//            return totalBalances;
//        }
//    }

    @Override
    public void closeExchange() {
        synchronized (globalLock) {
            for (Order order : this.buyOrders) {
                order.getExchangeStatusCallback().cancelOrder();
            }
            for (Order order : this.sellOrders) {
                order.getExchangeStatusCallback().cancelOrder();
            }
        }
    }
}
