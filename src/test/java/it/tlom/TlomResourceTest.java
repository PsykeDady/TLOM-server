package it.tlom;

import it.tlom.repository.JourneyRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TlomResourceTest {
    @Inject JourneyRepository journeyRepository;

    @Test void playerAndLocalizationExposeDevelopmentResources() {
        given().when().get("/api/v1/me").then().statusCode(200).body("id", is("player-psyke")).body("locale", is("it"));
        given().header("If-None-Match", "\"1\"").when().get("/api/v1/localization/it").then().statusCode(304);
        given().when().get("/api/v1/localization/unsupported").then().statusCode(200).body("locale", is("en")).body("labels.'game.today.title'", is("Today"));
    }

    @Test void installationCompletionAndWalletAreAuthoritativeAndIdempotent() {
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

        journeyRepository.addLedger(new JourneyRepository.LedgerState("test-hc-credit", "healthy-lifestyle::currency::healthy-coin", 1, "TEST", "test-hc-credit", "TEST:test-hc-credit"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-2\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(200).body("wallet.balances[0].balance", is(0));
        given().contentType("application/json").body("{}").when().post("/api/v1/packages/study-focus/install").then().statusCode(201);
        journeyRepository.addLedger(new JourneyRepository.LedgerState("test-st-credit", "study-focus::currency::study-token", 100, "TEST", "test-st-credit", "TEST:test-st-credit"));
        given().contentType("application/json").body("{\"storeItemId\":\"healthy-lifestyle::store-item::pizza\",\"requestId\":\"pizza-wrong-currency\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(409).body("code", is("INSUFFICIENT_FUNDS"));
        given().contentType("application/json").body("{\"storeItemId\":\"study-focus::store-item::gaming-break\",\"requestId\":\"wrong-store\"}").when().post("/api/v1/stores/healthy-lifestyle::store::healthy-store/purchases").then().statusCode(400).body("code", is("STORE_ITEM_NOT_IN_STORE"));
        given().when().get("/api/v1/me/purchases").then().statusCode(200).body("size()", is(2));
    }
}