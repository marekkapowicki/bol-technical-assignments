package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class AggregatorService {
    private final OrderService orderService;
    private final OfferService offerService;
    private final ProductService productService;

    AggregatorService(OrderService orderService, OfferService offerService, ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    EnrichedOrder enrich(int sellerId) throws ExecutionException, InterruptedException {

        CompletableFuture<Order> orderPromise = CompletableFuture
                .supplyAsync(() -> orderService.getOrder(sellerId));
        return orderPromise.
                thenCompose( order ->
                        retrieveOffer(order)
                            .thenCombine(retrieveProduct(order), (offer, product) -> combine(order, offer, product)))
                .join();
    }

    private CompletableFuture<Offer> retrieveOffer(Order order) {
        return CompletableFuture
                .supplyAsync( () -> offerService.getOffer(order.getOfferId()));
    }

    private CompletableFuture<Product> retrieveProduct(Order order) {
        return CompletableFuture
                .supplyAsync( () -> productService.getProduct(order.getProductId()));
    }

    private EnrichedOrder combine(Order order, Offer offer, Product product) {
        return new EnrichedOrder(order.getId(), offer.getId(), offer.getCondition(), product.getId(), product.getTitle());
    }
}
