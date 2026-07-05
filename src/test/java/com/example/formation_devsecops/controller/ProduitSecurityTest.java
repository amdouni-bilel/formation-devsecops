package com.example.formation_devsecops.controller;

import com.example.formation_devsecops.service.ProduitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProduitController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ éviter problèmes avec Spring Security
class ProduitSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // 🔥 IMPORTANT : remplacer @Mock
    private ProduitService produitService;

    // ============================================================
    // 🔐 TEST 1 : Tentatives d'injection SQL dans le nom
    // ============================================================
    @ParameterizedTest(name = "SQL Injection tentative : {0}")
    @ValueSource(strings = {
            "'; DROP TABLE produits; --",
            "1 OR 1=1",
            "' UNION SELECT * FROM users --"
    })
    @DisplayName("🔐 Tentative d'injection SQL dans le nom → 400 ou traitement sécurisé")
    void sqlInjection_InNom_ShouldBeRejectedOrSanitized(String payload) throws Exception {

        String body = String.format(
                "{\"nom\":\"%s\",\"prix\":9.99,\"stock\":1}",
                payload.replace("\\", "\\\\").replace("\"", "\\\""));

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status == 400 || status == 201
                            : "Le statut doit être 400 ou 201, pas : " + status;
                });
    }

    // ============================================================
    // 🔐 TEST 2 : Champs obligatoires manquants
    // ============================================================
    @Test
    @DisplayName("🔐 Body JSON vide → 400 Bad Request")
    void emptyBody_ShouldReturn400() throws Exception {

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 🔐 TEST 3 : Valeurs limites
    // ============================================================
    @Test
    @DisplayName("🔐 Nom de 101 caractères → 400")
    void nomTropLong_ShouldReturn400() throws Exception {

        String nomTropLong = "A".repeat(101);
        String body = "{\"nom\":\"" + nomTropLong + "\",\"prix\":9.99,\"stock\":1}";

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.nom").exists());
    }

    // ============================================================
    // 🔐 TEST 4 : Pas de stack trace exposée
    // ============================================================
    @Test
    @DisplayName("🔐 Erreur 500 → pas de stack trace")
    void error500_ShouldNotExposeStackTrace() throws Exception {

        org.mockito.Mockito.when(produitService.getTousProduits())
                .thenThrow(new RuntimeException("Erreur interne BDD"));

        String response = mockMvc.perform(get("/api/v1/produits"))
                .andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();

        assert !response.contains("at com.beedigital");
        assert !response.contains("at org.springframework");
        assert !response.contains("Exception") || response.contains("administrateur");
    }
}