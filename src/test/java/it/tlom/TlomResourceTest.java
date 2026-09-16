package it.tlom;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TlomResourceTest {
    @Test void playerAndLocalizationExposeDevelopmentResources() {
        given().when().get("/api/v1/me").then().statusCode(200).body("id", is("player-psyke")).body("locale", is("it"));
        given().header("If-None-Match", "\"1\"").when().get("/api/v1/localization/it").then().statusCode(304);
        given().when().get("/api/v1/localization/unsupported").then().statusCode(200).body("locale", is("en")).body("labels.'game.today.title'", is("Today"));
    }

    @Test void installationCompletionAndWalletAreAuthoritativeAndIdempotent() {
        given().contentType("application/json").body("{\"values\":{\"walk-reward\":2}}").when().post("/api/v1/packages/healthy-lifestyle/install").then().statusCode(201).body("configuration.walk-reward", is(2));
        given().when().get("/api/v1/me/journey").then().statusCode(200).body("occurrences[0].status", is("PENDING"));
        String occurrenceId = given().when().get("/api/v1/me/journey").then().extract().path("occurrences[0].id");
        given().when().post("/api/v1/goal-occurrences/" + occurrenceId + "/complete").then().statusCode(200).body("wallet.balances[0].balance", is(2)).body("wallet.ledgerEntryCount", is(1));
        given().when().post("/api/v1/goal-occurrences/" + occurrenceId + "/complete").then().statusCode(200).body("wallet.balances[0].balance", is(2)).body("wallet.ledgerEntryCount", is(1));
    }
}