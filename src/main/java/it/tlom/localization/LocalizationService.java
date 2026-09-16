package it.tlom.localization;

import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LocalizationService {
    private static final Map<String, String> ENGLISH = Map.of(
            "game.today.title", "Today",
            "goal.action.complete", "Complete",
            "goal.action.skip", "Skip",
            "goal.action.remind", "Remind later",
            "package.action.install", "Install");
    private static final Map<String, String> ITALIAN = Map.of(
            "game.today.title", "Oggi",
            "goal.action.complete", "Completa",
            "goal.action.skip", "Salta",
            "goal.action.remind", "Ricordamelo piu tardi");

    public LocalizationResource forLocale(String locale) {
        Map<String, String> localized = "it".equals(locale) ? ITALIAN : Map.of();
        Map<String, String> labels = new java.util.HashMap<>(ENGLISH);
        labels.putAll(localized);
        return new LocalizationResource("it".equals(locale) ? "it" : "en", "en", "1", Map.copyOf(labels));
    }

    public record LocalizationResource(String locale, String fallbackLocale, String revision, Map<String, String> labels) { }
}