package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.OfferCondition;

class EnrichedOrder {
    private final int id;
    private final int offerId;
    private final OfferCondition offerCondition;
    private final int productId;
    private final String productTitle;

    public EnrichedOrder(int id, int offerId, OfferCondition offerCondition, int productId, String productTitle) {
        this.id = id;
        this.offerId = offerId;
        this.offerCondition = offerCondition;
        this.productId = productId;
        this.productTitle = productTitle;
    }

    int getId() {
        return id;
    }

    int getOfferId() {
        return offerId;
    }

    OfferCondition getOfferCondition() {
        return offerCondition;
    }

    int getProductId() {
        return productId;
    }

    String getProductTitle() {
        return productTitle;
    }
}
