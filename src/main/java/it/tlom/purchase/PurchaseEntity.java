package it.tlom.purchase;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase")
class PurchaseEntity {
    @Id String id;
    @Column(name = "player_id") String playerId;
    @Column(name = "request_id") String requestId;
    @Column(name = "store_id") String storeId;
    @Column(name = "store_item_id") String storeItemId;
    @Column(name = "price_currency_id") String priceCurrencyId;
    @Column(name = "price_amount") int priceAmount;
    @Column(name = "purchased_at") String purchasedAt;

    protected PurchaseEntity() { }

    PurchaseEntity(Purchase purchase) {
        id = purchase.id();
        playerId = purchase.playerId();
        requestId = purchase.requestId();
        storeId = purchase.storeId();
        storeItemId = purchase.storeItemId();
        priceCurrencyId = purchase.price().currencyId();
        priceAmount = purchase.price().amount();
        purchasedAt = purchase.purchasedAt();
    }

    Purchase toModel() {
        return new Purchase(id, requestId, playerId, storeId, storeItemId,
                new it.tlom.packagecontent.ActivityPackage.Price(priceCurrencyId, priceAmount), purchasedAt);
    }
}