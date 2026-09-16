package it.tlom.journey;

import it.tlom.packagecontent.ActivityPackage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JourneyRepository {
    private final List<ActivityPackage.Installation> installations = new ArrayList<>();
    private final Map<String, ActivityPackage.Currency> currencies = new LinkedHashMap<>();
    private final Map<String, ActivityPackage.Mission> missions = new LinkedHashMap<>();
    private final Map<String, ActivityPackage.Campaign> campaigns = new LinkedHashMap<>();
    private final Map<String, ActivityPackage.Goal> goals = new LinkedHashMap<>();
    private final Map<String, OccurrenceState> occurrences = new LinkedHashMap<>();
    private final List<LedgerState> ledger = new ArrayList<>();

    public List<ActivityPackage.Installation> installations() { return List.copyOf(installations); }
    public Map<String, ActivityPackage.Currency> currencies() { return Map.copyOf(currencies); }
    public Map<String, ActivityPackage.Mission> missions() { return Map.copyOf(missions); }
    public Map<String, ActivityPackage.Campaign> campaigns() { return Map.copyOf(campaigns); }
    public Map<String, ActivityPackage.Goal> goals() { return Map.copyOf(goals); }
    public List<OccurrenceState> occurrences() { return List.copyOf(occurrences.values()); }
    public List<LedgerState> ledger() { return List.copyOf(ledger); }
    public OccurrenceState occurrence(String id) { return occurrences.get(id); }
    public boolean installed(String packageId, String version) { return installations.stream().anyMatch(item -> item.packageId().equals(packageId) && item.packageVersion().equals(version)); }

    public void install(ActivityPackage definition, Map<String, Integer> configuration) {
        installations.add(new ActivityPackage.Installation(definition.id() + "::installation::" + definition.version(), definition.id(), definition.version(), Map.copyOf(configuration)));
        definition.currencies().forEach(currency -> currencies.put(runtimeId(definition.id(), "currency", currency.id()), new ActivityPackage.Currency(runtimeId(definition.id(), "currency", currency.id()), currency.name(), currency.symbol())));
        definition.missions().forEach(mission -> {
            String missionId = runtimeId(definition.id(), "mission", mission.id());
            missions.put(missionId, new ActivityPackage.Mission(missionId, mission.name(), mission.goalIds().stream().map(goalId -> runtimeId(definition.id(), "goal", goalId)).toList(), mission.progressStrategy()));
        });
        definition.campaigns().forEach(campaign -> {
            String campaignId = runtimeId(definition.id(), "campaign", campaign.id());
            campaigns.put(campaignId, new ActivityPackage.Campaign(campaignId, campaign.name(), campaign.missionIds().stream().map(missionId -> runtimeId(definition.id(), "mission", missionId)).toList(), campaign.ownership()));
        });
        definition.goals().forEach(goal -> {
            String goalId = runtimeId(definition.id(), "goal", goal.id());
            var rewards = goal.rewards().stream().map(reward -> new ActivityPackage.Reward(runtimeId(definition.id(), "reward", reward.id()), reward.type(), reward.currencyId() == null ? null : runtimeId(definition.id(), "currency", reward.currencyId()), reward.amount())).toList();
            goals.put(goalId, new ActivityPackage.Goal(goalId, goal.name(), goal.description(), goal.goalType(), goal.schedule(), rewards));
            String occurrenceId = goalId + "::occurrence::initial";
            occurrences.put(occurrenceId, new OccurrenceState(occurrenceId, goalId, "PENDING"));
        });
    }

    public void complete(OccurrenceState occurrence) { occurrences.put(occurrence.id(), new OccurrenceState(occurrence.id(), occurrence.goalId(), "COMPLETED")); }
    public boolean hasLedgerSource(String sourceKey) { return ledger.stream().anyMatch(entry -> entry.sourceKey().equals(sourceKey)); }
    public void addLedger(LedgerState entry) { ledger.add(entry); }

    private static String runtimeId(String packageId, String type, String localId) { return packageId + "::" + type + "::" + localId; }
    public record OccurrenceState(String id, String goalId, String status) { }
    public record LedgerState(String id, String currencyId, int amount, String sourceKey) { }
}