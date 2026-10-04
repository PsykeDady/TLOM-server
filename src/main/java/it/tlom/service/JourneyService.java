package it.tlom.service;

import it.tlom.model.ActivityPackage;
import it.tlom.repository.JourneyRepository;
import it.tlom.shared.ApiException;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class JourneyService {
    @Inject JourneyRepository repository;

    public void install(ActivityPackage definition, Map<String, Integer> configuration) {
        if (!repository.installed(definition.id(), definition.version())) repository.install(definition, configuration);
    }

    public JourneyResponse journey() {
        return new JourneyResponse(
            repository.installations(),
            repository.currencies().values().stream().toList(),
            repository.campaigns().values().stream().toList(),
            repository.missions().values().stream().toList(),
            repository.goals().values().stream().toList(),
            repository.occurrences());
    }

    public CompletionResponse complete(String occurrenceId) {
        var occurrence = repository.occurrence(occurrenceId);
        if (occurrence == null) throw new ApiException(Response.Status.NOT_FOUND, "GOAL_OCCURRENCE_NOT_FOUND");
        if ("PENDING".equals(occurrence.status())) {
            repository.complete(occurrence);
            var goal = repository.goals().get(occurrence.goalId());
            goal.rewards().stream().filter(reward -> "CURRENCY".equals(reward.type())).forEach(reward -> {
                String sourceKey = "GOAL_OCCURRENCE:" + occurrence.id() + ":" + reward.id();
                if (!repository.hasLedgerSource(sourceKey)) repository.addLedger(new JourneyRepository.LedgerState(sourceKey, reward.currencyId(), reward.amount(), sourceKey));
            });
        }
        return new CompletionResponse(journey(), wallet());
    }

    public WalletResponse wallet() {
        List<WalletBalance> balances = repository.currencies().values().stream().map(currency -> new WalletBalance(currency.id(), currency.name(), currency.symbol(), repository.ledger().stream().filter(entry -> entry.currencyId().equals(currency.id())).mapToInt(JourneyRepository.LedgerState::amount).sum())).toList();
        return new WalletResponse(balances, repository.ledger().size());
    }

    public record JourneyResponse(List<ActivityPackage.Installation> installations,
                                  List<ActivityPackage.Currency> currencies,
                                  List<ActivityPackage.Campaign> campaigns,
                                  List<ActivityPackage.Mission> missions,
                                  List<ActivityPackage.Goal> goals,
                                  List<JourneyRepository.OccurrenceState> occurrences) { }
    public record WalletBalance(String currencyId, String name, String symbol, int balance) { }
    public record WalletResponse(List<WalletBalance> balances, int ledgerEntryCount) { }
    public record CompletionResponse(JourneyResponse journey, WalletResponse wallet) { }
}