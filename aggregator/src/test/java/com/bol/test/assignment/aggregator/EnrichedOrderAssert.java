package com.bol.test.assignment.aggregator;

import com.bol.test.assignment.offer.OfferCondition;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

class EnrichedOrderAssert extends AbstractAssert<EnrichedOrderAssert, EnrichedOrder> {


    private EnrichedOrderAssert(EnrichedOrder enrichedOrder) {
        super(enrichedOrder, EnrichedOrderAssert.class);
    }

    static EnrichedOrderAssert assertThat(EnrichedOrder actual) {
        return new EnrichedOrderAssert(actual);
    }

    public EnrichedOrderAssert hasId(int id) {
        Assertions.assertThat(actual.getId()).isEqualTo(id);
        return this;
    }

    public EnrichedOrderAssert hasOfferId(int offerId) {
        Assertions.assertThat(actual.getOfferId()).isEqualTo(offerId);
        return this;
    }

    public EnrichedOrderAssert hasOfferCondition(OfferCondition offerCondition) {
        Assertions.assertThat(actual.getOfferCondition()).isEqualTo(offerCondition);
        return this;
    }

    public EnrichedOrderAssert hasProductId(int productId) {
        Assertions.assertThat(actual.getProductId()).isEqualTo(productId);
        return this;
    }

    public EnrichedOrderAssert hasProductTitle(String productTitle) {
        Assertions.assertThat(actual.getProductTitle()).isEqualTo(productTitle);
        return this;
    }

    public EnrichedOrderAssert hasNullProductTitle() {
        Assertions.assertThat(actual.getProductTitle()).isNull();
        return this;
    }
}
