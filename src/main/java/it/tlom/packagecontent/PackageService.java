package it.tlom.packagecontent;

import it.tlom.journey.JourneyRepository;
import it.tlom.journey.JourneyService;
import it.tlom.shared.ApiException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class PackageService {
    private static final String GOAL_REWARD_AMOUNT = "GOAL_REWARD_AMOUNT";
    @Inject PackageRepository repository;
    @Inject JourneyRepository journeyRepository;
    @Inject JourneyService journeyService;

    void bootstrapHealthyLifestyle(@Observes io.quarkus.runtime.StartupEvent event) {
        install("healthy-lifestyle", Map.of());
    }

    public List<ActivityPackage> catalog() { return repository.findAll(); }
    public ActivityPackage packageById(String packageId) {
        var definition = repository.findById(packageId);
        if (definition == null) throw new ApiException(Response.Status.NOT_FOUND, "PACKAGE_NOT_FOUND");
        return definition;
    }

    public ActivityPackage.Installation install(String packageId, Map<String, Object> requestedValues) {
        var definition = packageById(packageId);
        var existing = journeyRepository.installations().stream().filter(item -> item.packageId().equals(definition.id()) && item.packageVersion().equals(definition.version())).findFirst();
        if (existing.isPresent()) return existing.get();
        var values = resolveConfiguration(definition, requestedValues == null ? Map.of() : requestedValues);
        journeyService.install(resolveDefinition(definition, values), values);
        return journeyRepository.installations().stream().filter(item -> item.packageId().equals(definition.id()) && item.packageVersion().equals(definition.version())).findFirst().orElseThrow();
    }

    private Map<String, Integer> resolveConfiguration(ActivityPackage definition, Map<String, Object> requestedValues) {
        var parameters = definition.parameters().stream().collect(java.util.stream.Collectors.toMap(ActivityPackage.Parameter::id, parameter -> parameter));
        if (requestedValues.keySet().stream().anyMatch(id -> !parameters.containsKey(id))) throw new ApiException(Response.Status.BAD_REQUEST, "UNKNOWN_PACKAGE_PARAMETER");
        var resolved = new HashMap<String, Integer>();
        for (var parameter : definition.parameters()) {
            Object value = requestedValues.getOrDefault(parameter.id(), parameter.defaultValue());
            if (!(value instanceof Integer integer) || integer < parameter.min() || integer > parameter.max()) throw new ApiException(Response.Status.BAD_REQUEST, "INVALID_PACKAGE_CONFIGURATION");
            resolved.put(parameter.id(), integer);
        }
        return Map.copyOf(resolved);
    }

    private ActivityPackage resolveDefinition(ActivityPackage definition, Map<String, Integer> values) {
        var resolvedGoals = definition.goals().stream().map(goal -> new ActivityPackage.Goal(goal.id(), goal.name(), goal.description(), goal.goalType(), goal.schedule(), goal.rewards().stream().map(reward -> {
            var binding = definition.bindings().stream().filter(item -> item.targetType().equals(GOAL_REWARD_AMOUNT) && item.targetId().equals(goal.id()) && item.rewardId().equals(reward.id())).findFirst();
            return binding.map(item -> new ActivityPackage.Reward(reward.id(), reward.type(), reward.currencyId(), values.get(item.parameterId()))).orElse(reward);
        }).toList())).toList();
        return new ActivityPackage(definition.id(), definition.version(), definition.name(), definition.description(), definition.currencies(), resolvedGoals, definition.missions(), definition.campaigns(), definition.parameters(), definition.bindings());
    }
}