package ru.rsreu;

import ru.rsreu.stockexchange.StockExchangeFabric;
import ru.rsreu.stockexchange.data.Client;
import ru.rsreu.stockexchange.data.CurrencyPair;
import ru.rsreu.stockexchange.data.Order;
import ru.rsreu.stockexchange.enums.CurrencyTypeEnum;
import ru.rsreu.stockexchange.enums.OperationStatusEnum;
import ru.rsreu.stockexchange.enums.OrderTypeEnum;
import ru.rsreu.stockexchange.interfaces.ExchangeCallback;
import ru.rsreu.stockexchange.interfaces.StockExchange;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static ru.rsreu.stockexchange.util.RandomExchangeObjectsGenerator.generateRandomOrder;

/**
 * 1. Биржа
 */
public class Runner {
    private static final List<Client> clients = createClients(2);
    public static final int THREAD_POOL_SIZE = 1500;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        long usersCount = 0;
        long ordersCount = 0;

        try {
            System.out.println("Input count of users:");
            usersCount = Long.parseLong(scanner.nextLine());
            System.out.println("Input count of orders:");
            ordersCount = Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input!");
            System.exit(1);
        }

        Random random = new Random();
        random.setSeed(1);

        List<Client> users = new ArrayList<>();
        HashSet<CurrencyPair> availableCurrencyPairs = new HashSet<>();
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Ruble, CurrencyTypeEnum.Yuan));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Ruble, CurrencyTypeEnum.Frank));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Dollar, CurrencyTypeEnum.Euro));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Dollar, CurrencyTypeEnum.Frank));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Euro, CurrencyTypeEnum.Dollar));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble));

//        StockExchange stockExchange = StockExchangeFabric.getStockExchangeQueue(availableCurrencyPairs);
        StockExchange stockExchange = StockExchangeFabric.getStockExchangeDisruptor(availableCurrencyPairs);

        for (long i = 0; i < usersCount; i++) {
            Client client = new Client();
            client.getWallet().put(CurrencyTypeEnum.Yuan, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Ruble, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Euro, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Dollar, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Frank, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.BritishPound, new BigDecimal(100));
            users.add(client);
        }

        System.out.println("Initial balances:");
        Map<CurrencyTypeEnum, BigDecimal> initialBalances = calculateTotalBalances(users);
        initialBalances.forEach((currency, balance) -> System.out.println(currency + ": " + balance));

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            CountDownLatch countDownLatch = new CountDownLatch((int) ordersCount);
            for (long i = 0; i < ordersCount; i++) {
                Client randomClient = users.get(random.nextInt(users.size()));
                Order order = generateRandomOrder(randomClient.getId());
                executorService.submit(() -> {
                    OperationStatusEnum orderResponse = null;
                    try {
                        orderResponse = stockExchange.createOrder(
                                order.getClientId(),
                                order.getOrderTypeEnum(),
                                order.getCurrencyPair(),
                                order.getPrice(),
                                order.getAmount(),
                                new ExchangeCallback() {
                                    @Override
                                    public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                                        processOrder(randomClient, order, price, amount);
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                                        processOrder(randomClient, order, price, amount);
                                    }

                                    @Override
                                    public void markAsQueued() {
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    public void cancelOrder() {
                                        countDownLatch.countDown();
                                    }
                                }
                        );
                    } catch (InterruptedException e) {
                        countDownLatch.countDown();
                        return;
                    }
                    if (orderResponse == OperationStatusEnum.Rejected) {
                        countDownLatch.countDown();
                    }
                });

            }
            countDownLatch.await();
            executorService.shutdown();
            StockExchangeFabric.stopQueueHandler();
            stockExchange.closeExchange();

            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Executor interrupted!");
            }
            StockExchangeFabric.stopQueueHandler();
            System.out.println("---------");
            System.out.println("Final balances:");
            Map<CurrencyTypeEnum, BigDecimal> finalBalances = calculateTotalBalances(users);
            finalBalances.forEach((currency, balance) -> System.out.println(currency + ": " + balance));
        } catch (InterruptedException e) {
            System.out.println("Main is interrupted!");
        }
    }

    /**
     * Processes an order by updating the client's wallet balances based on the order type (buy/sell).
     *
     * @param client the client placing the order
     * @param order  the order being processed
     * @param price  the price of the other side of the order (the trade price)
     */
    public static void processOrder(Client client, Order order, BigDecimal price, BigDecimal tradeAmount) {
        synchronized (client) {
            if (order.getOrderTypeEnum() == OrderTypeEnum.Buy) {
                BigDecimal totalCost = price.multiply(tradeAmount).setScale(5, RoundingMode.HALF_UP);
                client.getWallet().compute(order.getCurrencyPair().getQuoteCurrency(),
                        (currency, balance) -> balance.subtract(totalCost));
                client.getWallet().compute(order.getCurrencyPair().getBaseCurrency(),
                        (currency, balance) -> balance.add(tradeAmount));
            } else if (order.getOrderTypeEnum() == OrderTypeEnum.Sell) {
                BigDecimal totalRevenue = price.multiply(tradeAmount).setScale(5, RoundingMode.HALF_UP);
                client.getWallet().compute(order.getCurrencyPair().getBaseCurrency(),
                        (currency, balance) -> balance.subtract(tradeAmount));
                client.getWallet().compute(order.getCurrencyPair().getQuoteCurrency(),
                        (currency, balance) -> balance.add(totalRevenue));
            }
        }
    }


    /**
     * Calculates the total balances of all clients for each currency.
     *
     * @param clients the list of clients
     * @return a map of currency type to total balance
     */
    public static Map<CurrencyTypeEnum, BigDecimal> calculateTotalBalances(List<Client> clients) {
        return clients.stream()
                .flatMap(client -> client.getWallet().entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        BigDecimal::add
                ));
    }


    public static List<Client> createClients(int count) {
        List<Client> clients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Client client = new Client();
            client.getWallet().put(CurrencyTypeEnum.Yuan, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Ruble, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Euro, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Dollar, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.BritishPound, new BigDecimal(100));
            client.getWallet().put(CurrencyTypeEnum.Frank, new BigDecimal(100));
            clients.add(client);
        }
        return clients;
    }
}
