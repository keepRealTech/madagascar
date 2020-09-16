package com.keepreal.madagascar.vanga.model;

/**
 * Represents the interface of orders.
 */
public interface Order {

    String getId();
    String getUserId();
    String getPropertyId();
    String getTradeNumber();
    Integer getState();

}
