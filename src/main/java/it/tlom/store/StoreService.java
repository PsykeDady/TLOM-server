package it.tlom.store;

import it.tlom.packagecontent.ActivityPackage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreService {
    @Inject it.tlom.journey.JourneyService journeyService;

    public StoreCatalog stores() {
        var catalog = journeyService.stores();
        return new StoreCatalog(catalog.stores(), catalog.storeItems());
    }

    public ActivityPackage.Store storeById(String storeId) {
        return journeyService.storeById(storeId);
    }

    public ActivityPackage.StoreItem storeItemById(String storeItemId) {
        return journeyService.storeItemById(storeItemId);
    }

    public record StoreCatalog(java.util.List<ActivityPackage.Store> stores, java.util.List<ActivityPackage.StoreItem> storeItems) { }
}