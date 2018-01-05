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

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import static com.bol.test.assignment.offer.OfferCondition.AS_NEW;
import static com.bol.test.assignment.offer.OfferCondition.UNKNOWN;


public class AggregatorServiceAssertJTest implements WithAssertions, WithBDDMockito {

    private final int sellerId = 1;
    private final int orderId = 2;
    private final int offerId = 3;
    private final int productId = 4;
    private String title = "Title";

    private OrderService orderService = mock(OrderService.class);

    private OfferService offerService = mock(OfferService.class);

    private ProductService productService = mock(ProductService.class);

    private AggregatorService aggregatorService = new AggregatorService(orderService, offerService, productService);

    @Test
    public void simpleHappyFlow()  {
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
    public void offerAndProductServicesAreSlow() {
        //Given
        given(orderService.getOrder(sellerId)).willReturn(new Order(orderId, offerId, productId));
        given(offerService.getOffer(offerId)).willAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    TimeUnit.MILLISECONDS.sleep(1500L);
                    return new Offer(offerId, AS_NEW);
                }
        );
        given(productService.getProduct(productId)).willAnswer(
                (InvocationOnMock invocationOnMock) -> {
                    TimeUnit.MILLISECONDS.sleep(1500L);
                    return new Product(productId, title);
                }
        );

        //When
        EnrichedOrder enrichedOrder = aggregatorService.enrich(sellerId);

        //Then
        EnrichedOrderAssert.assertThat(enrichedOrder)
                .hasId(orderId)
                .hasOfferCondition(AS_NEW)
                .hasProductId(productId)
                .hasProductTitle(title);
    }

    @Test
    public void offerServiceFailed()  {
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
                .hasProductId(productId)
                .hasOfferId(-1)
                .hasOfferCondition(UNKNOWN);
    }

    @Test
    public void productServiceFailed() {
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
                .hasProductId(-1)
                .hasOfferId(offerId)
                .hasOfferCondition(AS_NEW);
    }

    @Test
    public void productServiceAndOfferServiceFailed() {
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
                .hasProductId(-1)
                .hasOfferId(-1)
                .hasOfferCondition(UNKNOWN);
    }

    @Test
    public void orderServiceFailed() {
        //Given
        given(orderService.getOrder(sellerId)).willThrow(new RuntimeException("Order service failed"));

        //Expect
        assertThatThrownBy(() -> aggregatorService.enrich(sellerId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("the order service does not work");
    }
}
