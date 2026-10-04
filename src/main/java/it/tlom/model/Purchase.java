package it.tlom.model;

public record Purchase(String id, String requestId, String playerId, String storeId,
                       String storeItemId, ActivityPackage.Price price, String purchasedAt) { }