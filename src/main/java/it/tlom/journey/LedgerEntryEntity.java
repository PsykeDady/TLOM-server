package it.tlom.journey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_entry")
class LedgerEntryEntity {
    @Id String id;
    @Column(name = "player_id") String playerId;
    @Column(name = "currency_id") String currencyId;
    int amount;
    @Column(name = "source_type") String sourceType;
    @Column(name = "source_id") String sourceId;
    @Column(name = "source_key") String sourceKey;

    protected LedgerEntryEntity() { }

    LedgerEntryEntity(String playerId, JourneyRepository.LedgerState state) {
        id = state.id();
        this.playerId = playerId;
        currencyId = state.currencyId();
        amount = state.amount();
        sourceType = state.sourceType();
        sourceId = state.sourceId();
        sourceKey = state.sourceKey();
    }

    JourneyRepository.LedgerState toModel() { return new JourneyRepository.LedgerState(id, currencyId, amount, sourceType, sourceId, sourceKey); }
}