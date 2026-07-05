package com.example.formation_devsecops.controller;

import com.example.formation_devsecops.dto.ProduitRequestDTO;
import com.example.formation_devsecops.dto.ProduitResponseDTO;
import com.example.formation_devsecops.exception.ProduitNotFoundException;
import com.example.formation_devsecops.service.ProduitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest : charge uniquement ProduitController (pas toute l'appli)
@WebMvcTest(ProduitController.class)
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;  // injecté automatiquement par @WebMvcTest

    @Autowired
    private ObjectMapper objectMapper;  // convertit Java <-> JSON


    @MockBean
    private ProduitService produitService;  // mock du service

    private ProduitResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = ProduitResponseDTO.builder()
                .id(1L).nom("Laptop").prix(999.99).stock(5)
                .categorie("Informatique").actif(true).build();
    }

    // ============================================================
    // TEST 1 : GET /api/v1/produits → 200 OK + JSON array
    // ============================================================
    @Test
    @DisplayName("GET /api/v1/produits → 200 + liste JSON")
    void getTousProduits_ShouldReturn200WithList() throws Exception {
        when(produitService.getTousProduits()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())              // 200
                .andExpect(jsonPath("$").isArray())       // réponse = tableau JSON
                .andExpect(jsonPath("$[0].nom").value("Laptop"))
                .andExpect(jsonPath("$[0].prix").value(999.99));
    }

    // ============================================================
    // TEST 2 : GET /api/v1/produits/{id} → 200 + produit
    // ============================================================
    @Test
    @DisplayName("GET /api/v1/produits/1 → 200 + produit JSON")
    void getProduitById_ShouldReturn200() throws Exception {
        when(produitService.getProduitById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/produits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Laptop"));
    }

    // ============================================================
    // TEST 3 : GET /api/v1/produits/99 → 404 Not Found
    // ============================================================
    @Test
    @DisplayName("GET /api/v1/produits/99 → 404 Not Found")
    void getProduitById_WhenNotFound_ShouldReturn404() throws Exception {
        when(produitService.getProduitById(99L))
                .thenThrow(new ProduitNotFoundException("Non trouvé"));

        mockMvc.perform(get("/api/v1/produits/99"))
                .andExpect(status().isNotFound())  // 404
                .andExpect(jsonPath("$.status").value(404));
    }

    // ============================================================
    // TEST 4 : POST /api/v1/produits → 201 Created
    // ============================================================
    @Test
    @DisplayName("POST /api/v1/produits → 201 Created")
    void creerProduit_ShouldReturn201() throws Exception {
        ProduitRequestDTO request = new ProduitRequestDTO();
        request.setNom("Laptop");
        request.setPrix(999.99);
        request.setStock(5);

        when(produitService.creerProduit(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  // 201
                .andExpect(jsonPath("$.id").value(1));
    }

    // ============================================================
    // TEST 5 🔐 DevSecOps : Validation → 400 Bad Request
    // ============================================================
    @Test
    @DisplayName("🔐 POST avec nom vide → 400 Bad Request (validation)")
    void creerProduit_WithInvalidData_ShouldReturn400() throws Exception {
        // Body avec nom vide et prix négatif → doit échouer la validation
        String invalidJson = "{\"nom\":\"\",\"prix\":-5,\"stock\":-1}";

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())  // 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.nom").exists())
                .andExpect(jsonPath("$.details.prix").exists());
    }

    // ============================================================
    // TEST 6 : DELETE /api/v1/produits/1 → 204 No Content
    // ============================================================
    @Test
    @DisplayName("DELETE /api/v1/produits/1 → 204 No Content")
    void supprimerProduit_ShouldReturn204() throws Exception {
        doNothing().when(produitService).supprimerProduit(1L);

        mockMvc.perform(delete("/api/v1/produits/1"))
                .andExpect(status().isNoContent());  // 204
    }

}
