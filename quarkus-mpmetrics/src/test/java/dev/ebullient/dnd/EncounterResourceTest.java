package dev.ebullient.dnd;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.ebullient.dnd.combat.client.RoundResult;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;

@QuarkusTest
public class EncounterResourceTest {
    static final TypeRef<List<RoundResult>> typeRef = new TypeRef<List<RoundResult>>() {
    };

    @Test
    public void testAnyEndpoint() {
        given()
                .when().get("/combat/any")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().as(typeRef);
    }

    @Test
    public void testFaceoffEndpoint() {
        given()
                .when().get("/combat/faceoff")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().as(typeRef);
    }

    @Test
    public void testMeleeEndpoint() {
        given()
                .when().get("/combat/melee")
                .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().body().as(typeRef);
    }

    @Test
    void testPrometheusScrapeEndpoint() {
        given()
                .when().get("/q/metrics")
                .then()
                .statusCode(200)
                .body(containsString("# HELP"));
    }
}
