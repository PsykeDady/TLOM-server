package it.tlom.packagecontent;

import java.util.List;
import java.util.Map;

public record ActivityPackage(String id, String version, String name, String description,
                              List<Currency> currencies, List<Goal> goals,
                              List<Mission> missions, List<Campaign> campaigns,
                              List<Store> stores, List<StoreItem> storeItems,
                              List<Parameter> parameters, List<Binding> bindings) {
    public record Currency(String id, String name, String symbol) { }
    public record Reward(String id, String type, String currencyId, int amount) { }
    public record Goal(String id, String name, String description, String goalType, String schedule, List<Reward> rewards) { }
    public record Mission(String id, String name, List<String> goalIds, String progressStrategy) { }
    public record Campaign(String id, String name, List<String> missionIds, String ownership) { }
    public record Store(String id, String name, List<String> itemIds) { }
    public record StoreItem(String id, String name, String description, Price price) { }
    public record Price(String currencyId, int amount) { }
    public record Parameter(String id, String type, String label, int defaultValue, int min, int max) { }
    public record Binding(String parameterId, String targetType, String targetId, String rewardId) { }
    public record Installation(String id, String packageId, String packageVersion, Map<String, Integer> configuration) { }
}