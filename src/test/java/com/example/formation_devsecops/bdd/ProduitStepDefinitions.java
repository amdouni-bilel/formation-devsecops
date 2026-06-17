package com.example.formation_devsecops.bdd;


import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.cucumber.java.fr.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ProduitStepDefinitions {

    @LocalServerPort
    private int port;

    private Response dernierReponse;
    private Long dernierIdCree;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // ---- Étapes Given ----

    @Given("l'API BeeStore est disponible")
    public void apiDisponible() {
        given().get("/api/v1/produits")
                .then().statusCode(200);
    }

    @Given("un produit {string} avec le prix {double} existe en base")
    public void produitExiste(String nom, Double prix) {
        String body = String.format(
                "{\"nom\":\"%s\",\"prix\":%s,\"stock\":10}", nom, prix);
        Response r = given()
                .contentType("application/json").body(body)
                .post("/api/v1/produits");
        dernierIdCree = r.jsonPath().getLong("id");
    }

    // ---- Étapes When ----

    @When("je crée un produit avec le nom {string} et le prix {double}")
    public void creerProduit(String nom, Double prix) {
        String body = String.format(
                "{\"nom\":\"%s\",\"prix\":%s,\"stock\":5}", nom, prix);
        dernierReponse = given()
                .contentType("application/json").body(body)
                .post("/api/v1/produits");
    }

    @When("je récupère le produit avec l'ID créé")
    public void recupererProduit() {
        dernierReponse = given()
                .get("/api/v1/produits/" + dernierIdCree);
    }

    @When("je supprime le produit avec l'ID {long}")
    public void supprimerProduit(Long id) {
        dernierReponse = given().delete("/api/v1/produits/" + id);
    }

    // ---- Étapes Then ----

    @Then("le statut HTTP de réponse est {int}")
    public void verifierStatut(Integer code) {
        assertThat(dernierReponse.statusCode()).isEqualTo(code);
    }

    @Then("le produit retourné a le nom {string}")
    public void verifierNom(String nom) {
        assertThat(dernierReponse.jsonPath().getString("nom")).isEqualTo(nom);
    }

    @Then("la réponse contient une erreur sur le champ {string}")
    public void verifierErreurChamp(String champ) {
        assertThat(dernierReponse.jsonPath().getString("details." + champ))
                .isNotNull();
    }
}
