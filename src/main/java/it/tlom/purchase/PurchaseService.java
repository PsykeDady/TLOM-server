package it.tlom.purchase;

import it.tlom.journey.JourneyRepository;
import it.tlom.journey.JourneyService;
import it.tlom.player.PlayerService;
import it.tlom.store.StoreService;
import it.tlom.shared.ApiException;
import java.time.Instant;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PurchaseService {
    @Inject StoreService storeService;
    @Inject JourneyService journeyService;
    @Inject JourneyRepository journeyRepository;
    @Inject PurchaseRepository purchaseRepository;
    @Inject PlayerService playerService;

    @Transactional
    public PurchaseResult purchase(String storeId, String storeItemId, String requestId) {
        if (storeItemId == null || storeItemId.isBlank() || requestId == null || requestId.isBlank()) {
            throw new ApiException(Response.Status.BAD_REQUEST, "INVALID_PURCHASE_REQUEST");
        }
        playerService.lockCurrentPlayer();
        String playerId = playerService.currentPlayer().id();
        var previousPurchase = purchaseRepository.findByRequestId(playerId, requestId);
        if (previousPurchase != null) return new PurchaseResult(previousPurchase, journeyService.wallet());

        var store = storeService.storeById(storeId);
        var item = storeService.storeItemById(storeItemId);
        if (!store.itemIds().contains(item.id())) {
            throw new ApiException(Response.Status.BAD_REQUEST, "STORE_ITEM_NOT_IN_STORE");
        }
        var price = item.price();
        if (price.currencyId() == null || price.currencyId().isBlank() || price.amount() <= 0) {
            throw new ApiException(Response.Status.BAD_REQUEST, "INVALID_PURCHASE_REQUEST");
        }
        if (journeyService.walletBalance(price.currencyId()) < price.amount()) {
            throw new ApiException(Response.Status.CONFLICT, "INSUFFICIENT_FUNDS");
        }

        var purchase = new Purchase(requestId, requestId, playerId, store.id(), item.id(), price, Instant.now().toString());
        purchaseRepository.add(purchase);
        String sourceKey = "PURCHASE:" + purchase.id();
        journeyRepository.addLedger(playerId, new JourneyRepository.LedgerState(sourceKey, price.currencyId(), -price.amount(), "PURCHASE", purchase.id(), sourceKey));
        return new PurchaseResult(purchase, journeyService.wallet());
    }

    public List<Purchase> purchases() { return purchaseRepository.findAll(playerService.currentPlayer().id()); }

    public record PurchaseResult(Purchase purchase, JourneyService.WalletResponse wallet) { }
}