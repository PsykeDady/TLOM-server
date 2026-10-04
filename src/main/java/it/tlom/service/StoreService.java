package it.tlom.service;

import it.tlom.model.ActivityPackage;
import it.tlom.repository.JourneyRepository;
import it.tlom.shared.ApiException;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class StoreService {
    @Inject JourneyRepository journeyRepository;

    public StoreCatalog stores() {
        return new StoreCatalog(journeyRepository.stores().values().stream().toList(), journeyRepository.storeItems().values().stream().toList());
    }

    public ActivityPackage.Store storeById(String storeId) {
        var store = journeyRepository.stores().get(storeId);
        if (store == null) throw new ApiException(Response.Status.NOT_FOUND, "STORE_NOT_FOUND");
        return store;
    }

    public ActivityPackage.StoreItem storeItemById(String storeItemId) {
        var item = journeyRepository.storeItems().get(storeItemId);
        if (item == null) throw new ApiException(Response.Status.NOT_FOUND, "STORE_ITEM_NOT_FOUND");
        return item;
    }

    public record StoreCatalog(List<ActivityPackage.Store> stores, List<ActivityPackage.StoreItem> storeItems) { }
}