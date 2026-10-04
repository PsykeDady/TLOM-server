package it.tlom.journey;

import it.tlom.packagecontent.ActivityPackage;
import it.tlom.packagecontent.PackageRepository;
import it.tlom.player.PlayerService;
import it.tlom.shared.ApiException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class JourneyService {
    @Inject JourneyRepository repository;
    @Inject PackageRepository packageRepository;
    @Inject PlayerService playerService;

    @Transactional
    public ActivityPackage.Installation install(ActivityPackage definition, Map<String, Integer> configuration) {
        String playerId = playerService.currentPlayer().id();
        if (!repository.installed(playerId, definition.id(), definition.version())) repository.install(playerId, definition, configuration);
        return repository.installations(playerId).stream().filter(item -> item.packageId().equals(definition.id()) && item.packageVersion().equals(definition.version())).findFirst().orElseThrow();
    }

    public boolean installed(String packageId, String version) {
        return repository.installed(playerService.currentPlayer().id(), packageId, version);
    }

    public JourneyResponse journey() {
        var state = runtimeState();
        return new JourneyResponse(state.installations(), List.copyOf(state.currencies().values()), List.copyOf(state.campaigns().values()),
                List.copyOf(state.missions().values()), List.copyOf(state.goals().values()), repository.occurrences(playerService.currentPlayer().id()));
    }

    @Transactional
    public CompletionResponse complete(String occurrenceId) {
        String playerId = playerService.currentPlayer().id();
        var occurrence = repository.occurrence(playerId, occurrenceId, true);
        if (occurrence == null) throw new ApiException(Response.Status.NOT_FOUND, "GOAL_OCCURRENCE_NOT_FOUND");
        if ("PENDING".equals(occurrence.status())) {
            var goal = runtimeState().goals().get(occurrence.goalId());
            repository.complete(occurrence.id());
            goal.rewards().stream().filter(reward -> "CURRENCY".equals(reward.type())).forEach(reward -> {
                String sourceKey = "GOAL_OCCURRENCE:" + occurrence.id() + ":" + reward.id();
                if (!repository.hasLedgerSource(playerId, sourceKey)) repository.addLedger(playerId, new JourneyRepository.LedgerState(sourceKey, reward.currencyId(), reward.amount(), "GOAL_OCCURRENCE", occurrence.id(), sourceKey));
            });
        }
        return new CompletionResponse(journey(), wallet());
    }

    public WalletResponse wallet() {
        String playerId = playerService.currentPlayer().id();
        var state = runtimeState();
        List<WalletBalance> balances = state.currencies().values().stream()
                .map(currency -> new WalletBalance(currency.id(), currency.name(), currency.symbol(), repository.walletBalance(playerId, currency.id()))).toList();
        return new WalletResponse(balances, repository.ledgerCount(playerId));
    }

    public int walletBalance(String currencyId) { return repository.walletBalance(playerService.currentPlayer().id(), currencyId); }

    public StoreCatalog stores() {
        var state = runtimeState();
        return new StoreCatalog(List.copyOf(state.stores().values()), List.copyOf(state.storeItems().values()));
    }

    public ActivityPackage.Store storeById(String storeId) {
        var store = runtimeState().stores().get(storeId);
        if (store == null) throw new ApiException(Response.Status.NOT_FOUND, "STORE_NOT_FOUND");
        return store;
    }

    public ActivityPackage.StoreItem storeItemById(String storeItemId) {
        var item = runtimeState().storeItems().get(storeItemId);
        if (item == null) throw new ApiException(Response.Status.NOT_FOUND, "STORE_ITEM_NOT_FOUND");
        return item;
    }

    private RuntimeState runtimeState() {
        String playerId = playerService.currentPlayer().id();
        var currencies = new LinkedHashMap<String, ActivityPackage.Currency>();
        var missions = new LinkedHashMap<String, ActivityPackage.Mission>();
        var campaigns = new LinkedHashMap<String, ActivityPackage.Campaign>();
        var goals = new LinkedHashMap<String, ActivityPackage.Goal>();
        var stores = new LinkedHashMap<String, ActivityPackage.Store>();
        var storeItems = new LinkedHashMap<String, ActivityPackage.StoreItem>();
        var installations = repository.installations(playerId);
        for (var installation : installations) {
            var definition = packageRepository.findById(installation.packageId());
            if (definition == null) throw new IllegalStateException("Installed package definition is unavailable: " + installation.packageId());
            var resolved = resolveDefinition(definition, installation.configuration());
            resolved.currencies().forEach(currency -> currencies.put(runtimeId(resolved.id(), "currency", currency.id()), new ActivityPackage.Currency(runtimeId(resolved.id(), "currency", currency.id()), currency.name(), currency.symbol())));
            resolved.missions().forEach(mission -> missions.put(runtimeId(resolved.id(), "mission", mission.id()), new ActivityPackage.Mission(runtimeId(resolved.id(), "mission", mission.id()), mission.name(), mission.goalIds().stream().map(goalId -> runtimeId(resolved.id(), "goal", goalId)).toList(), mission.progressStrategy())));
            resolved.campaigns().forEach(campaign -> campaigns.put(runtimeId(resolved.id(), "campaign", campaign.id()), new ActivityPackage.Campaign(runtimeId(resolved.id(), "campaign", campaign.id()), campaign.name(), campaign.missionIds().stream().map(missionId -> runtimeId(resolved.id(), "mission", missionId)).toList(), campaign.ownership())));
            resolved.storeItems().forEach(item -> storeItems.put(runtimeId(resolved.id(), "store-item", item.id()), new ActivityPackage.StoreItem(runtimeId(resolved.id(), "store-item", item.id()), item.name(), item.description(), new ActivityPackage.Price(runtimeId(resolved.id(), "currency", item.price().currencyId()), item.price().amount()))));
            resolved.stores().forEach(store -> stores.put(runtimeId(resolved.id(), "store", store.id()), new ActivityPackage.Store(runtimeId(resolved.id(), "store", store.id()), store.name(), store.itemIds().stream().map(itemId -> runtimeId(resolved.id(), "store-item", itemId)).toList())));
            resolved.goals().forEach(goal -> goals.put(runtimeId(resolved.id(), "goal", goal.id()), new ActivityPackage.Goal(runtimeId(resolved.id(), "goal", goal.id()), goal.name(), goal.description(), goal.goalType(), goal.schedule(), goal.rewards().stream().map(reward -> new ActivityPackage.Reward(runtimeId(resolved.id(), "reward", reward.id()), reward.type(), reward.currencyId() == null ? null : runtimeId(resolved.id(), "currency", reward.currencyId()), reward.amount())).toList())));
        }
        return new RuntimeState(installations, currencies, campaigns, missions, goals, stores, storeItems);
    }

    private static ActivityPackage resolveDefinition(ActivityPackage definition, Map<String, Integer> values) {
        var goals = definition.goals().stream().map(goal -> new ActivityPackage.Goal(goal.id(), goal.name(), goal.description(), goal.goalType(), goal.schedule(), goal.rewards().stream().map(reward -> definition.bindings().stream().filter(binding -> "GOAL_REWARD_AMOUNT".equals(binding.targetType()) && binding.targetId().equals(goal.id()) && binding.rewardId().equals(reward.id())).findFirst().map(binding -> new ActivityPackage.Reward(reward.id(), reward.type(), reward.currencyId(), values.get(binding.parameterId()))).orElse(reward)).toList())).toList();
        return new ActivityPackage(definition.id(), definition.version(), definition.name(), definition.description(), definition.currencies(), goals, definition.missions(), definition.campaigns(), definition.stores(), definition.storeItems(), definition.parameters(), definition.bindings());
    }

    private static String runtimeId(String packageId, String type, String localId) { return packageId + "::" + type + "::" + localId; }

    private record RuntimeState(List<ActivityPackage.Installation> installations, Map<String, ActivityPackage.Currency> currencies, Map<String, ActivityPackage.Campaign> campaigns, Map<String, ActivityPackage.Mission> missions, Map<String, ActivityPackage.Goal> goals, Map<String, ActivityPackage.Store> stores, Map<String, ActivityPackage.StoreItem> storeItems) { }
    public record JourneyResponse(List<ActivityPackage.Installation> installations, List<ActivityPackage.Currency> currencies, List<ActivityPackage.Campaign> campaigns, List<ActivityPackage.Mission> missions, List<ActivityPackage.Goal> goals, List<JourneyRepository.OccurrenceState> occurrences) { }
    public record WalletBalance(String currencyId, String name, String symbol, int balance) { }
    public record WalletResponse(List<WalletBalance> balances, int ledgerEntryCount) { }
    public record CompletionResponse(JourneyResponse journey, WalletResponse wallet) { }
    public record StoreCatalog(List<ActivityPackage.Store> stores, List<ActivityPackage.StoreItem> storeItems) { }
}