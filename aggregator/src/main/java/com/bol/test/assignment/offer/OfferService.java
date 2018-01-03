package com.bol.test.assignment.offer;

public class OfferService {
    public Offer getOffer(int id) {
        return new Offer(id, OfferCondition.AS_NEW);
    }
}
