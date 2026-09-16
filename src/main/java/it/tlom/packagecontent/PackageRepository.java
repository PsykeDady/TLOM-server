package it.tlom.packagecontent;

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PackageRepository {
    private final List<ActivityPackage> catalog = List.of(
            new ActivityPackage("healthy-lifestyle", "1", "Healthy Lifestyle", "Build a steady daily rhythm.",
                    List.of(new ActivityPackage.Currency("healthy-coin", "Healthy Coin", "HC")),
                    List.of(new ActivityPackage.Goal("daily-walk", "Daily walk", "Take a walk and keep the rhythm.", "ROUTINES", "0 9 * * *",
                            List.of(new ActivityPackage.Reward("walk-hc", "CURRENCY", "healthy-coin", 1)))),
                    List.of(new ActivityPackage.Mission("daily-health", "Daily health", List.of("daily-walk"), "ACTIVITY_COMPLETION")),
                    List.of(new ActivityPackage.Campaign("healthy-life", "Healthy Lifestyle", List.of("daily-health"), "PLAYER")),
                    List.of(new ActivityPackage.Parameter("walk-reward", "INTEGER", "Healthy Coins per walk", 1, 1, 10)),
                    List.of(new ActivityPackage.Binding("walk-reward", "GOAL_REWARD_AMOUNT", "daily-walk", "walk-hc"))),
            new ActivityPackage("study-focus", "1", "Study Focus", "Make time for deliberate learning.",
                    List.of(new ActivityPackage.Currency("study-token", "Study Token", "ST")),
                    List.of(new ActivityPackage.Goal("study-session", "Focused study session", "Read, practice, or learn without distractions.", "ONESHOTS", null,
                            List.of(new ActivityPackage.Reward("study-st", "CURRENCY", "study-token", 1)))),
                    List.of(new ActivityPackage.Mission("study-session", "Study session", List.of("study-session"), "ACTIVITY_COMPLETION")),
                    List.of(new ActivityPackage.Campaign("study-campaign", "Study Focus", List.of("study-session"), "PLAYER")), List.of(), List.of()));

    public List<ActivityPackage> findAll() { return catalog; }
    public ActivityPackage findById(String id) { return catalog.stream().filter(item -> item.id().equals(id)).findFirst().orElse(null); }
}