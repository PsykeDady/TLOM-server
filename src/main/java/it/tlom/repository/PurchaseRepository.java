package it.tlom.repository;

import it.tlom.model.Purchase;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PurchaseRepository {
    private final Map<String, Purchase> purchasesByRequestId = new LinkedHashMap<>();

    public Purchase findByRequestId(String requestId) { return purchasesByRequestId.get(requestId); }
    public List<Purchase> findAll() { return List.copyOf(new ArrayList<>(purchasesByRequestId.values())); }
    public void add(Purchase purchase) { purchasesByRequestId.put(purchase.requestId(), purchase); }
}