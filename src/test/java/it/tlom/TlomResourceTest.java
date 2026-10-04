package it.tlom;

import it.tlom.journey.JourneyRepository;
import it.tlom.player.PlayerService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TlomResourceTest {
    @Inject JourneyRepository journeyRepository;
    @Inject PlayerService playerService;
    @Inject EntityManager entityManager;

    @Test void playerAndLocalizationExposeDevelopmentResources() {
        given().when().get("/api/v1/me").then().statusCode(200).body("id", is("player-psyke")).body("locale", is("it"));
        given().header("If-None-Match", "\"1\"").when().get("/api/v1/localization/it").then().statusCode(304);
        given().when().get("/api/v1/localization/unsupported").then().statusCode(200).body("locale", is("en")).body("labels.'game.today.title'", is("Today"));
    }

    @Test void installationCompletionAndWalletAreAuthoritativeAndIdempotent() throws Exception {
        given().contentType("application/json").body("{\"values\":{\"walk-reward\":2}}").when().post("/api/v1/packages/healthy-lifestyle/install").then().statusCode(201).body("configuration.walk-reward", is(1));
        given().when().get("/api/v1/me/stores").then().statusCode(200).body("stores.id", hasItem("healthy-lifestyle::store::healthy-store")).body("stores.itemIds[0][0]", is("healthy-lifestyle::store-item::pizza")).body("storeItems[0].price.currencyId", is("healthy-lifestyle::currency::healthy-coin"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-insufficient\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(409).body("code", is("INSUFFICIENT_FUNDS"));
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("size()", is(0));
        given().when().get("/api/v1/me/journey").then().statusCode(200).body("occurrences[0].status", is("PENDING"));
        String occurrenceId = given().when().get("/api/v1/me/journey").then().extract().path("occurrences[0].id");
        given().when().post("/api/v1/goal-occurrences/" + occurrenceId + "/complete").then().statusCode(200).body("wallet.balances[0].balance", is(1)).body("wallet.ledgerEntryCount", is(1));
        given().when().post("/api/v1/goal-occurrences/" + occurrenceId + "/complete").then().statusCode(200).body("wallet.balances[0].balance", is(1)).body("wallet.ledgerEntryCount", is(1));
        String purchaseRequest = "{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-1\"}";
        given().contentType("application/json").body(purchaseRequest).when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(200).body("purchase.price.currencyId", is("healthy-lifestyle::currency::healthy-coin")).body("purchase.price.amount", is(1)).body("wallet.balances[0].balance", is(0)).body("wallet.ledgerEntryCount", is(2));
        given().contentType("application/json").body(purchaseRequest).when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(200).body("purchase.requestId", is("pizza-1")).body("wallet.balances[0].balance", is(0)).body("wallet.ledgerEntryCount", is(2));
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("size()", is(1)).body("[0].price.amount", is(1));
        entityManager.clear();
        given().when().get("/api/v1/me/journey").then().statusCode(200).body("occurrences[0].status", is("COMPLETED"));
        given().when().get("/api/v1/me/wallet").then().statusCode(200).body("balances[0].balance", is(0)).body("ledgerEntryCount", is(2));
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("size()", is(1)).body("[0].requestId", is("pizza-1"));

        journeyRepository.addLedger(playerService.currentPlayer().id(), new JourneyRepository.LedgerState("test-hc-credit", "healthy-lifestyle::currency::healthy-coin", 1, "TEST", "test-hc-credit", "TEST:test-hc-credit"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-2\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(200).body("wallet.balances[0].balance", is(0));
        given().contentType("application/json").body("{}").when().post("/api/v1/packages/study-focus/install").then().statusCode(201);
        journeyRepository.addLedger(playerService.currentPlayer().id(), new JourneyRepository.LedgerState("test-st-credit", "study-focus::currency::study-token", 100, "TEST", "test-st-credit", "TEST:test-st-credit"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-wrong-currency\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(409).body("code", is("INSUFFICIENT_FUNDS"));
        given().contentType("application/json").body("{\"storeItemId\":\"study-focus::store-item::gaming-break\",\"requestId\":\"wrong-store\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(400).body("code", is("STORE_ITEM_NOT_IN_STORE"));
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("size()", is(2));

        journeyRepository.addLedger(playerService.currentPlayer().id(), new JourneyRepository.LedgerState("rollback-fixture", "healthy-lifestyle::currency::healthy-coin", 1, "TEST", "rollback-fixture", "PURCHASE:rollback-request"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"rollback-request\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(500);
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("requestId", not(hasItem("rollback-request")));

        var executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> firstPurchase = () -> given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"concurrent-pizza-a\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").statusCode();
            Callable<Integer> secondPurchase = () -> given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"concurrent-pizza-b\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").statusCode();
            var statuses = executor.invokeAll(java.util.List.of(firstPurchase, secondPurchase)).stream().map(future -> {
                try { return future.get(); } catch (Exception exception) { throw new RuntimeException(exception); }
            }).sorted().toList();
            org.junit.jupiter.api.Assertions.assertEquals(java.util.List.of(200, 409), statuses);
        } finally {
            executor.shutdownNow();
        }
        given().when().get("/api/v1/me/wallet").then().statusCode(200).body("balances.find { it.currencyId == 'healthy-lifestyle::currency::healthy-coin' }.balance", is(0));
    }
}