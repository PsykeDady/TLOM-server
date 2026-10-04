package it.tlom.journey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

@Entity
@Table(name = "goal_occurrence")
class GoalOccurrenceEntity {
    @Id String id;
    @Column(name = "player_id") String playerId;
    @Column(name = "goal_id") String goalId;
    String status;

    protected GoalOccurrenceEntity() { }

    GoalOccurrenceEntity(String id, String playerId, String goalId, String status) {
        this.id = id;
        this.playerId = playerId;
        this.goalId = goalId;
        this.status = status;
    }

    JourneyRepository.OccurrenceState toModel() { return new JourneyRepository.OccurrenceState(id, goalId, status); }
}