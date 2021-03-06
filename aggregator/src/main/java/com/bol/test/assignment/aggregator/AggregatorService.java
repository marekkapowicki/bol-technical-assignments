package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bol.test.assignment.offer.OfferCondition.UNKNOWN;
import static java.util.concurrent.CompletableFuture.supplyAsync;

class AggregatorService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final OrderService orderService;
    private final OfferService offerService;
    private final ProductService productService;

    AggregatorService(OrderService orderService, OfferService offerService, ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    EnrichedOrder enrich(int sellerId) {
        return retrieveOrder(sellerId)
                .thenComposeAsync(order ->
                        retrieveOffer(order)
                                .thenCombineAsync(retrieveProduct(order),
                                        (offer, product) -> combine(order, offer, product))).join();
    }

    private CompletableFuture<Order> retrieveOrder(int sellerId) {
        return supplyAsync(()
                -> orderService.getOrder(sellerId), executorService)
                .exceptionally(throwable -> {
                    throw new IllegalStateException("the order service does not work");
                });
    }


    private CompletableFuture<Offer> retrieveOffer(Order order) {
        return supplyAsync(() -> offerService.getOffer(order.getOfferId()), executorService)
                        .exceptionally(throwable -> new Offer(-1, UNKNOWN));
    }

    private CompletableFuture<Product> retrieveProduct(Order order) {
        return supplyAsync(() -> productService.getProduct(order.getProductId()), executorService)
                        .exceptionally(throwable -> new Product(-1, null));
    }

    private EnrichedOrder combine(Order order, Offer offer, Product product) {
        return new EnrichedOrder(order.getId(), offer.getId(), offer.getCondition(), product.getId(), product.getTitle());
    }
}
