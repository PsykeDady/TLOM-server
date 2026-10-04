package it.tlom.journey;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.tlom.packagecontent.ActivityPackage;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JourneyRepository {
    private static final TypeReference<Map<String, Integer>> CONFIGURATION_TYPE = new TypeReference<>() { };

    @PersistenceContext EntityManager entityManager;
    @Inject ObjectMapper objectMapper;

    public List<ActivityPackage.Installation> installations(String playerId) {
        return entityManager.createQuery("from PackageInstallationEntity where playerId = :playerId order by id", PackageInstallationEntity.class)
                .setParameter("playerId", playerId).getResultList().stream()
                .map(entity -> new ActivityPackage.Installation(entity.id, entity.packageId, entity.packageVersion, configuration(entity.configurationJson))).toList();
    }

    public boolean installed(String playerId, String packageId, String version) {
        return entityManager.createQuery("select count(entity) from PackageInstallationEntity entity where entity.playerId = :playerId and entity.packageId = :packageId and entity.packageVersion = :version", Long.class)
                .setParameter("playerId", playerId).setParameter("packageId", packageId).setParameter("version", version).getSingleResult() > 0;
    }

    public void install(String playerId, ActivityPackage definition, Map<String, Integer> configuration) {
        String installationId = definition.id() + "::installation::" + definition.version();
        entityManager.persist(new PackageInstallationEntity(installationId, playerId, definition.id(), definition.version(), configurationJson(configuration)));
        definition.goals().forEach(goal -> {
            String goalId = runtimeId(definition.id(), "goal", goal.id());
            entityManager.persist(new GoalOccurrenceEntity(goalId + "::occurrence::initial", playerId, goalId, "PENDING"));
        });
    }

    public List<OccurrenceState> occurrences(String playerId) {
        return entityManager.createQuery("from GoalOccurrenceEntity where playerId = :playerId order by id", GoalOccurrenceEntity.class)
                .setParameter("playerId", playerId).getResultList().stream().map(GoalOccurrenceEntity::toModel).toList();
    }

    public OccurrenceState occurrence(String playerId, String occurrenceId, boolean lock) {
        var entity = entityManager.find(GoalOccurrenceEntity.class, occurrenceId, lock ? LockModeType.PESSIMISTIC_WRITE : LockModeType.NONE);
        return entity == null || !entity.playerId.equals(playerId) ? null : entity.toModel();
    }

    public void complete(String occurrenceId) { entityManager.find(GoalOccurrenceEntity.class, occurrenceId).status = "COMPLETED"; }

    public boolean hasLedgerSource(String playerId, String sourceKey) {
        return entityManager.createQuery("select count(entity) from LedgerEntryEntity entity where entity.playerId = :playerId and entity.sourceKey = :sourceKey", Long.class)
                .setParameter("playerId", playerId).setParameter("sourceKey", sourceKey).getSingleResult() > 0;
    }

    @Transactional
    public void addLedger(String playerId, LedgerState state) { entityManager.persist(new LedgerEntryEntity(playerId, state)); }

    public List<LedgerState> ledger(String playerId) {
        return entityManager.createQuery("from LedgerEntryEntity where playerId = :playerId order by id", LedgerEntryEntity.class)
                .setParameter("playerId", playerId).getResultList().stream().map(LedgerEntryEntity::toModel).toList();
    }

    public int walletBalance(String playerId, String currencyId) {
        Long balance = entityManager.createQuery("select coalesce(sum(entity.amount), 0) from LedgerEntryEntity entity where entity.playerId = :playerId and entity.currencyId = :currencyId", Long.class)
                .setParameter("playerId", playerId).setParameter("currencyId", currencyId).getSingleResult();
        return balance.intValue();
    }

    public int ledgerCount(String playerId) {
        return entityManager.createQuery("select count(entity) from LedgerEntryEntity entity where entity.playerId = :playerId", Long.class)
                .setParameter("playerId", playerId).getSingleResult().intValue();
    }

    private Map<String, Integer> configuration(String json) {
        try { return Map.copyOf(objectMapper.readValue(json, CONFIGURATION_TYPE)); }
        catch (Exception exception) { throw new IllegalStateException("Invalid persisted package configuration", exception); }
    }

    private String configurationJson(Map<String, Integer> configuration) {
        try { return objectMapper.writeValueAsString(configuration); }
        catch (Exception exception) { throw new IllegalStateException("Cannot persist package configuration", exception); }
    }

    private static String runtimeId(String packageId, String type, String localId) { return packageId + "::" + type + "::" + localId; }

    public record OccurrenceState(String id, String goalId, String status) { }
    public record LedgerState(String id, String currencyId, int amount, String sourceType, String sourceId, String sourceKey) { }
}