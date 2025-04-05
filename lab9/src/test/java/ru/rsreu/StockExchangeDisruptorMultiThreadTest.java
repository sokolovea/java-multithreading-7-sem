package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.rsreu.Runner.calculateTotalBalances;
import static ru.rsreu.Runner.processOrder;
import static ru.rsreu.stockexchange.util.RandomExchangeObjectsGenerator.generateRandomOrder;

public class StockExchangeDisruptorMultiThreadTest {

    private StockExchange stockExchange;
    private List<Client> clients;

    @BeforeEach
    public void setup() {
        HashSet<CurrencyPair> availableCurrencyPairs = new HashSet<>();
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Ruble, CurrencyTypeEnum.Yuan));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Ruble, CurrencyTypeEnum.Frank));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Dollar, CurrencyTypeEnum.Euro));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Dollar, CurrencyTypeEnum.Frank));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Euro, CurrencyTypeEnum.Dollar));
        availableCurrencyPairs.add(new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble));

        stockExchange = StockExchangeFabric.getStockExchangeDisruptor(availableCurrencyPairs);
        clients = createClients(100);
    }

    private List<Client> createClients(int count) {
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

    @Test
    public void testPartialOrderExecution() throws InterruptedException {
        Client seller = clients.get(0);
        Client buyer = clients.get(1);

        // Создаем ордера
        Order sellOrder = new Order(seller.getId(), BigDecimal.valueOf(1.5), BigDecimal.valueOf(50), OrderTypeEnum.Sell,
                new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble), null);
        Order buyOrder = new Order(buyer.getId(), BigDecimal.valueOf(1.5), BigDecimal.valueOf(25), OrderTypeEnum.Buy,
                new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble), null);

        CountDownLatch latch = new CountDownLatch(2);

        // Обрабатываем sellOrder
        stockExchange.createOrder(
                sellOrder.getClientId(),
                sellOrder.getOrderTypeEnum(),
                sellOrder.getCurrencyPair(),
                sellOrder.getPrice(),
                sellOrder.getAmount(),
                new ExchangeCallback() {
                    @Override
                    public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(seller, sellOrder, price, amount);
                        latch.countDown();
                    }

                    @Override
                    public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(seller, sellOrder, price, amount);
                        latch.countDown();
                    }

                    @Override
                    public void markAsQueued() {
                        latch.countDown();
                    }

                    @Override
                    public void cancelOrder() {
                        latch.countDown();
                    }
                }
        );

        // Обрабатываем buyOrder
        stockExchange.createOrder(
                buyOrder.getClientId(),
                buyOrder.getOrderTypeEnum(),
                buyOrder.getCurrencyPair(),
                buyOrder.getPrice(),
                buyOrder.getAmount(),
                new ExchangeCallback() {
                    @Override
                    public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(buyer, buyOrder, price, amount);
                        latch.countDown();
                    }

                    @Override
                    public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(buyer, buyOrder, price, amount);
                    }

                    @Override
                    public void markAsQueued() {
                        latch.countDown();
                    }

                    @Override
                    public void cancelOrder() {
                        latch.countDown();
                    }
                }
        );

        latch.await();

        // Проверяем итоговые балансы
        Assertions.assertEquals(new BigDecimal("137.5").setScale(10, RoundingMode.HALF_UP),
                seller.getWallet().get(CurrencyTypeEnum.Ruble).setScale(10, RoundingMode.HALF_UP));
    }


    @Test
    public void testFullOrderExecution() throws InterruptedException {
        Client seller = clients.get(0);
        Client buyer = clients.get(1);

        // Создаем ордера
        Order sellOrder = new Order(seller.getId(), BigDecimal.valueOf(1.5), BigDecimal.valueOf(50), OrderTypeEnum.Sell,
                new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble), null);
        Order buyOrder = new Order(buyer.getId(), BigDecimal.valueOf(1.5), BigDecimal.valueOf(50), OrderTypeEnum.Buy,
                new CurrencyPair(CurrencyTypeEnum.Yuan, CurrencyTypeEnum.Ruble), null);

        CountDownLatch latch = new CountDownLatch(2);

        // Обрабатываем sellOrder
        stockExchange.createOrder(
                sellOrder.getClientId(),
                sellOrder.getOrderTypeEnum(),
                sellOrder.getCurrencyPair(),
                sellOrder.getPrice(),
                sellOrder.getAmount(),
                new ExchangeCallback() {
                    @Override
                    public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(seller, sellOrder, price, amount);
                        latch.countDown();
                    }

                    @Override
                    public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(seller, sellOrder, price, amount);
                    }

                    @Override
                    public void markAsQueued() {
                    }

                    @Override
                    public void cancelOrder() {
                        latch.countDown();
                    }
                }
        );

        // Обрабатываем buyOrder
        stockExchange.createOrder(
                buyOrder.getClientId(),
                buyOrder.getOrderTypeEnum(),
                buyOrder.getCurrencyPair(),
                buyOrder.getPrice(),
                buyOrder.getAmount(),
                new ExchangeCallback() {
                    @Override
                    public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(buyer, buyOrder, price, amount);
                        latch.countDown();
                    }

                    @Override
                    public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                        processOrder(buyer, buyOrder, price, amount);
                    }

                    @Override
                    public void markAsQueued() {
                        latch.countDown();
                    }

                    @Override
                    public void cancelOrder() {
                        latch.countDown();
                    }
                }
        );

        latch.await();

        // Проверяем итоговые балансы
        Assertions.assertEquals(new BigDecimal("50").setScale(10, RoundingMode.HALF_UP), seller.getWallet().get(CurrencyTypeEnum.Yuan).setScale(10, RoundingMode.HALF_UP));
        Assertions.assertEquals(new BigDecimal("175").setScale(10, RoundingMode.HALF_UP), seller.getWallet().get(CurrencyTypeEnum.Ruble).setScale(10, RoundingMode.HALF_UP));
        Assertions.assertEquals(new BigDecimal("150").setScale(10, RoundingMode.HALF_UP), buyer.getWallet().get(CurrencyTypeEnum.Yuan).setScale(10, RoundingMode.HALF_UP));
        Assertions.assertEquals(new BigDecimal("25").setScale(10, RoundingMode.HALF_UP), buyer.getWallet().get(CurrencyTypeEnum.Ruble).setScale(10, RoundingMode.HALF_UP));
    }



    @Test
    public void stressTestWithManyOrdersAndClients() throws InterruptedException {
        Random random = new Random();
        long ordersCount = 500000;

        System.out.println("Initial balances:");
        Map<CurrencyTypeEnum, BigDecimal> initialBalances = calculateTotalBalances(clients);
        initialBalances.forEach((currency, balance) -> System.out.println(currency + ": " + balance));

        ExecutorService executorService = Executors.newFixedThreadPool(Runner.THREAD_POOL_SIZE);
        CountDownLatch latch = new CountDownLatch((int) ordersCount);

        for (long i = 0; i < ordersCount; i++) {
            Client randomClient = clients.get(random.nextInt(clients.size()));
            Order order = generateRandomOrder(randomClient.getId());

            executorService.submit(() -> {
                try {
                    OperationStatusEnum orderResult = stockExchange.createOrder(
                            order.getClientId(),
                            order.getOrderTypeEnum(),
                            order.getCurrencyPair(),
                            order.getPrice(),
                            order.getAmount(),
                            new ExchangeCallback() {
                                @Override
                                public void completeWithFullCompletion(BigDecimal price, BigDecimal amount) {
                                    processOrder(randomClient, order, price, amount);
                                    latch.countDown();
                                }

                                @Override
                                public void completeWithPartialCompletion(BigDecimal price, BigDecimal amount) {
                                    processOrder(randomClient, order, price, amount);
                                }

                                @Override
                                public void markAsQueued() {
                                    latch.countDown();
                                }

                                @Override
                                public void cancelOrder() {
                                    latch.countDown();
                                }
                            }
                    );
                    if (orderResult == OperationStatusEnum.Rejected) {
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    Assertions.fail(e);
                }
            });
        }
        latch.await();
        executorService.shutdown();
        StockExchangeFabric.stopQueueHandler();
        stockExchange.closeExchange();

        System.out.println("---");
        System.out.println("Final balances:");
        Map<CurrencyTypeEnum, BigDecimal> finalBalances = calculateTotalBalances(clients);
        finalBalances.forEach((currency, balance) -> System.out.println(currency + ": " + balance));

        initialBalances.forEach((currency, initialBalance) -> {
            BigDecimal finalBalance = finalBalances.getOrDefault(currency, BigDecimal.ZERO);
            Assertions.assertEquals(
                    initialBalance.setScale(10, RoundingMode.HALF_UP),
                    finalBalance.setScale(10, RoundingMode.HALF_UP),
                    "Balances for " + currency + " do not match after the stress test!"
            );
        });
    }

}

