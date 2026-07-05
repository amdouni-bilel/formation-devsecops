package com.example.formation_devsecops.service.imp;

import com.example.formation_devsecops.dto.ProduitRequestDTO;
import com.example.formation_devsecops.dto.ProduitResponseDTO;
import com.example.formation_devsecops.entity.Produit;
import com.example.formation_devsecops.exception.ProduitNotFoundException;
import com.example.formation_devsecops.repository.ProduitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) : active Mockito pour JUnit 5
@ExtendWith(MockitoExtension.class)
class ProduitServiceImplTest {

    // @Mock : crée un faux ProduitRepository (pas de BDD réelle)
    @Mock
    private ProduitRepository produitRepository;

    // @InjectMocks : crée ProduitServiceImpl et injecte le mock dedans
    @InjectMocks
    private ProduitServiceImpl produitService;

    // Fixture : objet réutilisable dans tous les tests
    private Produit produitFixture;
    private ProduitRequestDTO requestDTOFixture;

    // @BeforeEach : exécuté AVANT chaque test → prépare les données
    @BeforeEach
    void setUp() {
        produitFixture = Produit.builder()
                .id(1L).nom("Laptop Pro").prix(1299.99)
                .stock(10).categorie("Informatique").actif(true)
                .build();

        requestDTOFixture = new ProduitRequestDTO();
        requestDTOFixture.setNom("Laptop Pro");
        requestDTOFixture.setPrix(1299.99);
        requestDTOFixture.setStock(10);
        requestDTOFixture.setCategorie("Informatique");
    }

    // ============================================================
    // TEST 1 : Création d'un produit — CAS NOMINAL
    // ============================================================
    @Test
    @DisplayName("creerProduit : doit retourner le DTO avec l'ID généré")
    void creerProduit_ShouldReturnResponseDTO() {
        // GIVEN : le mock simule la sauvegarde en BDD
        when(produitRepository.save(any(Produit.class)))
                .thenReturn(produitFixture);

        // WHEN : on appelle la méthode à tester
        ProduitResponseDTO result = produitService.creerProduit(requestDTOFixture);

        // THEN : on vérifie le résultat avec AssertJ
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNom()).isEqualTo("Laptop Pro");
        assertThat(result.getPrix()).isEqualTo(1299.99);

        // VERIFY : le repository.save() a bien été appelé exactement 1 fois
        verify(produitRepository, times(1)).save(any(Produit.class));
    }

    // ============================================================
    // TEST 2 : Récupération par ID — PRODUIT EXISTE
    // ============================================================
    @Test
    @DisplayName("getProduitById : doit retourner le produit si l'ID existe")
    void getProduitById_WhenExists_ShouldReturnDTO() {
        // GIVEN
        when(produitRepository.findById(1L))
                .thenReturn(Optional.of(produitFixture));

        // WHEN
        ProduitResponseDTO result = produitService.getProduitById(1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Laptop Pro");
    }

    // ============================================================
    // TEST 3 : Récupération par ID — PRODUIT INEXISTANT
    // ============================================================
    @Test
    @DisplayName("getProduitById : doit lever ProduitNotFoundException si ID inexistant")
    void getProduitById_WhenNotExists_ShouldThrowException() {
        // GIVEN : le mock retourne Optional.empty() (produit inexistant)
        when(produitRepository.findById(99L))
                .thenReturn(Optional.empty());

        // THEN : on s'attend à ce qu'une exception soit levée
        assertThatThrownBy(() -> produitService.getProduitById(99L))
                .isInstanceOf(ProduitNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ============================================================
    // TEST 4 : Liste de tous les produits
    // ============================================================
    @Test
    @DisplayName("getTousProduits : doit retourner la liste complète")
    void getTousProduits_ShouldReturnList() {
        // GIVEN
        Produit p2 = Produit.builder().id(2L).nom("Souris").prix(29.99).stock(50).build();
        when(produitRepository.findAll()).thenReturn(List.of(produitFixture, p2));

        // WHEN
        List<ProduitResponseDTO> result = produitService.getTousProduits();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting("nom")
                .containsExactly("Laptop Pro", "Souris");
    }

    // ============================================================
    // TEST 5 : Suppression — PRODUIT EXISTE
    // ============================================================
    @Test
    @DisplayName("supprimerProduit : doit appeler deleteById si l'ID existe")
    void supprimerProduit_WhenExists_ShouldCallDelete() {
        // GIVEN
        when(produitRepository.existsById(1L)).thenReturn(true);

        // WHEN
        produitService.supprimerProduit(1L);

        // THEN : deleteById doit être appelé exactement 1 fois
        verify(produitRepository, times(1)).deleteById(1L);
    }

    // ============================================================
    // TEST 6 : Suppression — PRODUIT INEXISTANT
    // ============================================================
    @Test
    @DisplayName("supprimerProduit : doit lever exception si ID inexistant")
    void supprimerProduit_WhenNotExists_ShouldThrowException() {
        // GIVEN
        when(produitRepository.existsById(99L)).thenReturn(false);

        // THEN
        assertThatThrownBy(() -> produitService.supprimerProduit(99L))
                .isInstanceOf(ProduitNotFoundException.class);

        // VERIFY : deleteById ne doit JAMAIS être appelé
        verify(produitRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // TEST 7 : Tests paramétrés — plusieurs prix invalides
    // ============================================================
    @ParameterizedTest
    @DisplayName("creerProduit avec prix valide : différentes valeurs")
    @ValueSource(doubles = {0.01, 9.99, 100.0, 9999.99})
    void creerProduit_WithValidPrix_ShouldNotThrow(double prix) {
        requestDTOFixture.setPrix(prix);
        Produit p = Produit.builder().id(1L).nom("Test").prix(prix).stock(1).build();
        when(produitRepository.save(any())).thenReturn(p);

        assertThatCode(() -> produitService.creerProduit(requestDTOFixture))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("TDD Red : activer un produit avec stock=0 doit lever une exception")
    void activerProduit_WithStockZero_ShouldThrowException() {

        // GIVEN : produit avec stock = 0
        Produit produitSansStock = Produit.builder()
                .id(1L).nom("Article").prix(9.99).stock(0).actif(false).build();

        when(produitRepository.findById(1L))
                .thenReturn(Optional.of(produitSansStock));

        // ✅ AJOUT IMPORTANT : mock du save
        // rendu lenient pour éviter UnnecessaryStubbing si la méthode n'est pas appelée
        org.mockito.Mockito.lenient()
                .when(produitRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProduitRequestDTO activerDTO = new ProduitRequestDTO();
        activerDTO.setNom("Article");
        activerDTO.setPrix(9.99);
        activerDTO.setStock(0);
        activerDTO.setActif(true); // activation

        // THEN : doit lever IllegalStateException (règle métier)
        assertThatThrownBy(() -> produitService.modifierProduit(1L, activerDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stock");
    }


}
