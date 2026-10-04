package it.tlom.purchase;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class PurchaseRepository {
    @PersistenceContext EntityManager entityManager;

    public Purchase findByRequestId(String playerId, String requestId) {
        return entityManager.createQuery("from PurchaseEntity where playerId = :playerId and requestId = :requestId", PurchaseEntity.class)
                .setParameter("playerId", playerId).setParameter("requestId", requestId).getResultStream().findFirst().map(PurchaseEntity::toModel).orElse(null);
    }

    public List<Purchase> findAll(String playerId) {
        return entityManager.createQuery("from PurchaseEntity where playerId = :playerId order by purchasedAt", PurchaseEntity.class)
                .setParameter("playerId", playerId).getResultList().stream().map(PurchaseEntity::toModel).toList();
    }

    public void add(Purchase purchase) { entityManager.persist(new PurchaseEntity(purchase)); }
}