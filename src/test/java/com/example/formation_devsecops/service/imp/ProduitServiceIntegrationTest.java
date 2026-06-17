package com.example.formation_devsecops.service.imp;

import com.example.formation_devsecops.dto.ProduitRequestDTO;
import com.example.formation_devsecops.dto.ProduitResponseDTO;
import com.example.formation_devsecops.exception.ProduitNotFoundException;
import com.example.formation_devsecops.service.ProduitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// @SpringBootTest : démarre le contexte Spring complet
// @AutoConfigureTestDatabase(replace = NONE) : utilise la config de test (H2)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional  // Chaque test est rollbacké : pas de pollution entre tests
class ProduitServiceIntegrationTest {

    // @Autowired : injection du vrai service (avec vrai Repository et BDD H2)
    @Autowired
    private ProduitService produitService;

    private ProduitRequestDTO buildRequest(String nom, Double prix, Integer stock) {
        ProduitRequestDTO dto = new ProduitRequestDTO();
        dto.setNom(nom);
        dto.setPrix(prix);
        dto.setStock(stock);
        dto.setCategorie("Test");
        return dto;
    }

    @Test
    @DisplayName("Intégration : créer et retrouver un produit en BDD")
    void creerEtRecuperer_ShouldPersistInDatabase() {
        // GIVEN : créer un produit via le service (persisté en H2)
        ProduitResponseDTO created = produitService.creerProduit(
                buildRequest("Tablet Pro", 599.99, 20));

        // WHEN : le récupérer par son ID réel
        ProduitResponseDTO found = produitService.getProduitById(created.getId());

        // THEN : les données persistées sont identiques
        assertThat(found.getNom()).isEqualTo("Tablet Pro");
        assertThat(found.getPrix()).isEqualTo(599.99);
        assertThat(found.getDateCreation()).isNotNull();
    }

    @Test
    @DisplayName("Intégration : modifier un produit en BDD")
    void modifierProduit_ShouldUpdateInDatabase() {
        // GIVEN
        ProduitResponseDTO created = produitService.creerProduit(
                buildRequest("Clavier", 79.99, 100));

        // WHEN
        ProduitResponseDTO updated = produitService.modifierProduit(
                created.getId(), buildRequest("Clavier Mécanique", 129.99, 80));

        // THEN
        assertThat(updated.getNom()).isEqualTo("Clavier Mécanique");
        assertThat(updated.getPrix()).isEqualTo(129.99);
    }

    @Test
    @DisplayName("Intégration : supprimer → le produit ne doit plus exister")
    void supprimerProduit_ShouldRemoveFromDatabase() {
        // GIVEN
        ProduitResponseDTO created = produitService.creerProduit(
                buildRequest("Ecran", 299.99, 5));
        Long id = created.getId();

        // WHEN
        produitService.supprimerProduit(id);

        // THEN : ProduitNotFoundException doit être levée
        assertThatThrownBy(() -> produitService.getProduitById(id))
                .isInstanceOf(ProduitNotFoundException.class);
    }

    @Test
    @DisplayName("Intégration : lister tous les produits créés")
    void getTousProduits_ShouldReturnAllPersisted() {
        // GIVEN : créer 3 produits
        produitService.creerProduit(buildRequest("PC Bureau", 899.99, 3));
        produitService.creerProduit(buildRequest("Webcam", 49.99, 30));
        produitService.creerProduit(buildRequest("Hub USB", 19.99, 100));

        // WHEN
        List<ProduitResponseDTO> list = produitService.getTousProduits();

        // THEN
        assertThat(list).hasSizeGreaterThanOrEqualTo(3);
    }

}

