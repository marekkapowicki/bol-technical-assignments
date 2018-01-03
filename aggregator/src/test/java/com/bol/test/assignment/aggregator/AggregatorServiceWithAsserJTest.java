package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.Offer;
import com.bol.test.assignment.offer.OfferService;
import com.bol.test.assignment.order.Order;
import com.bol.test.assignment.order.OrderService;
import com.bol.test.assignment.product.Product;
import com.bol.test.assignment.product.ProductService;
import info.solidsoft.mockito.java8.api.WithBDDMockito;
import org.assertj.core.api.WithAssertions;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;

import java.util.concurrent.ExecutionException;

import static com.bol.test.assignment.offer.OfferCondition.AS_NEW;
import static com.bol.test.assignment.offer.OfferCondition.UNKNOWN;


public class AggregatorServiceWithAsserJTest implements WithAssertions, WithBDDMockito {
    private OrderService orderService = mock(OrderService.class);

    private OfferService offerService = mock(OfferService.class);

    private ProductService productService = mock(ProductService.class);

    private AggregatorService aggregatorService = new AggregatorService(orderService, offerService, productService);


    private final int sellerId = 1;
    private final int orderId = 2;
    private final int offerId = 3;
    private final int productId = 4;
    private String title = "Title";


    @Test
    public void simpleHappyFlow() throws ExecutionException, InterruptedException {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willReturn(new Offer(offerId, AS_NEW));
        given(productService.getProduct(productId)).willReturn(new Product(productId, title));

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        assertThat(enrichedOrder.getId()).isEqualTo(orderId);

    }

    @Test(timeout = 2000)
    public void offerAndProductServicesAreSlow() throws InterruptedException, ExecutionException {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    Thread.sleep(1500);
                    return new Offer(offerId, AS_NEW);
                }
        );
        given(productService.getProduct(productId)).willAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    Thread.sleep(1500);
                    return new Product(productId, title);
                }
        );

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        EnrichedOrderAssert.assertThat(enrichedOrder)
                .hasId(orderId)
                .hasOfferCondition(AS_NEW)
                .hasProductTitle(title);
    }

    @Test
    public void offerServiceFailed() throws ExecutionException, InterruptedException {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willThrow(new RuntimeException("Offer Service failed"));
        given(productService.getProduct(productId)).willReturn(new Product(productId, title));

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        EnrichedOrderAssert.assertThat(enrichedOrder)
                .hasId(orderId)
                .hasProductTitle(title)
                .hasOfferId(-1)
                .hasOfferCondition(UNKNOWN);
    }

    @Test
    public void productServiceFailed() throws ExecutionException, InterruptedException {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willReturn(new Offer(offerId, AS_NEW));
        given(productService.getProduct(productId)).willThrow(new RuntimeException("Product Service failed"));

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        EnrichedOrderAssert.assertThat(enrichedOrder)
                .hasId(orderId)
                .hasNullProductTitle()
                .hasOfferId(offerId)
                .hasOfferCondition(AS_NEW);
    }

    @Test
    public void productServiceAndOfferServiceFailed() throws ExecutionException, InterruptedException {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willThrow(new RuntimeException("Offer Service failed"));
        given(productService.getProduct(productId)).willThrow(new RuntimeException("Product Service failed"));

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        EnrichedOrderAssert.assertThat(enrichedOrder)
                .hasId(orderId)
                .hasNullProductTitle()
                .hasOfferId(-1)
                .hasOfferCondition(UNKNOWN);
    }

    @Test
    public void orderServiceFailed() {
        //Given
        given(orderService.getOrder(sellerId)).willThrow(new RuntimeException("Order service failed"));

        //Expect
        assertThatThrownBy(() -> aggregatorService.enrich(sellerId))
                .isInstanceOf(RuntimeException.class);
    }
}
