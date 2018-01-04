package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferCondition;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;

import java.util.concurrent.CompletableFuture;

class AggregatorService {
    private final OrderService orderService;
    private final OfferService offerService;
    private final ProductService productService;

    AggregatorService(OrderService orderService, OfferService offerService, ProductService productService) {
        this.orderService = orderService;
        this.offerService = offerService;
        this.productService = productService;
    }

    EnrichedOrder enrich(int sellerId) {

        return retrieveOrder(sellerId).
                thenCompose( order ->
                        retrieveOffer(order)
                            .thenCombine(
                                    retrieveProduct(order),
                                    (offer, product) -> combine(order, offer, product)))
                .join();
    }

    private CompletableFuture<Order> retrieveOrder(int sellerId) {
        return CompletableFuture
                .supplyAsync(() -> orderService.getOrder(sellerId))
                .exceptionally(throwable -> {throw new IllegalStateException("mayday");});
    }


    private CompletableFuture<Offer> retrieveOffer(Order order) {
        return CompletableFuture
                .supplyAsync(() -> offerService.getOffer(order.getOfferId()))
                .exceptionally(throwable -> new Offer(-1, OfferCondition.UNKNOWN));
    }

    private CompletableFuture<Product> retrieveProduct(Order order) {
        return CompletableFuture
                .supplyAsync(() -> productService.getProduct(order.getProductId()))
                .exceptionally(throwable -> new Product(-1, null));
    }

    private EnrichedOrder combine(Order order, Offer offer, Product product) {
        return new EnrichedOrder(order.getId(), offer.getId(), offer.getCondition(), product.getId(), product.getTitle());
    }
}
