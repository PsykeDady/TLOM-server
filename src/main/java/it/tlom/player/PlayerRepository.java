package it.tlom.player;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlayerRepository {
    @PersistenceContext EntityManager entityManager;

    public Player find(String id) {
        var entity = entityManager.find(PlayerEntity.class, id);
        return entity == null ? null : entity.toModel();
    }

    @Transactional
    public Player findOrCreate(Player player) {
        var existing = entityManager.find(PlayerEntity.class, player.id());
        if (existing == null) entityManager.persist(new PlayerEntity(player));
        return existing == null ? player : existing.toModel();
    }

    public void lock(String id) {
        entityManager.find(PlayerEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
    }
}