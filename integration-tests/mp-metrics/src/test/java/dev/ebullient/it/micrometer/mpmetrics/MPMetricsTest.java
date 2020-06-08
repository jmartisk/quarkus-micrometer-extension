package dev.ebullient.it.micrometer.mpmetrics;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MPMetricsTest {

    @Test
    @Order(1)
    void callPrimeGen_1() {
        given()
                .when().get("/prime/31")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    void callPrimeGen_2() {
        given()
                .when().get("/prime/33")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    void callPrimeGen_3() {
        given()
                .when().get("/prime/887")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(4)
    void validateMetricsOutput_1() {
        given()
                .when().get("/prometheus")
                .then()
                .log().body()
                .statusCode(200)

                // Prometheus body has ALL THE THINGS in no particular order

                .body(containsString("dev_ebullient_it_micrometer_mpmetrics_PrimeResource_performedChecks=3"))
                .body(containsString("dev_ebullient_it_micrometer_mpmetrics_PrimeResource_highestPrimeNumberSoFar=887"));
    }

    @Test
    @Order(5)
    void callPrimeGen_4() {
        given()
                .when().get("/prime/900")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(6)
    void validateMetricsOutput_2() {
        given()
                .when().get("/prometheus")
                .then()
                .log().body()
                .statusCode(200)

                // Prometheus body has ALL THE THINGS in no particular order

                .body(containsString("dev_ebullient_it_micrometer_mpmetrics_PrimeResource_performedChecks=4"))
                .body(containsString("dev_ebullient_it_micrometer_mpmetrics_PrimeResource_highestPrimeNumberSoFar=887"));
    }
}