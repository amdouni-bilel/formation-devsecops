package com.example.formation_devsecops.service.imp;


import com.example.formation_devsecops.dto.ProduitRequestDTO;
import com.example.formation_devsecops.dto.ProduitResponseDTO;
import com.example.formation_devsecops.entity.Produit;
import com.example.formation_devsecops.exception.ProduitNotFoundException;
import com.example.formation_devsecops.repository.ProduitRepository;
import com.example.formation_devsecops.service.ProduitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor   // Injection par constructeur (meilleure pratique Spring)
@Slf4j                     // Génère automatiquement le logger 'log'
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;

    // -------------------------------------------------------
    // CREATION
    // -------------------------------------------------------
    @Override
    @Transactional
    public ProduitResponseDTO creerProduit(ProduitRequestDTO requestDTO) {
        log.info("[SERVICE] Création produit : {}", requestDTO.getNom());
        Produit produit = mapToEntity(requestDTO);
        Produit saved = produitRepository.save(produit);
        log.info("[SERVICE] Produit créé avec ID : {}", saved.getId());
        return mapToResponseDTO(saved);
    }

    // -------------------------------------------------------
    // LECTURE
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponseDTO> getTousProduits() {
        return produitRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProduitResponseDTO getProduitById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() ->
                        new ProduitNotFoundException(
                                "Produit non trouvé avec l'ID : " + id));
        return mapToResponseDTO(produit);
    }

    // -------------------------------------------------------
    // MISE A JOUR
    // -------------------------------------------------------
    @Override
    @Transactional
    public ProduitResponseDTO modifierProduit(Long id, ProduitRequestDTO dto) {
        log.info("[SERVICE] Modification produit ID : {}", id);
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() ->
                        new ProduitNotFoundException(
                                "Produit non trouvé avec l'ID : " + id));
        // Calculer le stock final attendu sans modifier l'entité tout de suite
        Integer stockAvant = produit.getStock();
        Integer stockDto = dto.getStock();
        Integer stockFinal = (stockDto != null) ? stockDto : stockAvant;

        // Règle métier : n'autorise pas d'activer un produit si la requête demande l'activation
        // et que le stock final est null ou égal à 0
        boolean demandeActivation = Boolean.TRUE.equals(dto.getActif());
        if (demandeActivation && (stockFinal == null || stockFinal.intValue() == 0)) {
            throw new IllegalStateException("Impossible d'activer le produit : stock insuffisant");
        }

        // Appliquer ensuite les modifications
        produit.setNom(dto.getNom());
        produit.setDescription(dto.getDescription());
        produit.setPrix(dto.getPrix());
        produit.setStock(stockFinal);
        produit.setCategorie(dto.getCategorie());
        if (dto.getActif() != null) produit.setActif(dto.getActif());

        return mapToResponseDTO(produitRepository.save(produit));
    }

    // -------------------------------------------------------
    // SUPPRESSION
    // -------------------------------------------------------
    @Override
    @Transactional
    public void supprimerProduit(Long id) {
        log.info("[SERVICE] Suppression produit ID : {}", id);
        if (!produitRepository.existsById(id)) {
            throw new ProduitNotFoundException(
                    "Produit non trouvé avec l'ID : " + id);
        }
        produitRepository.deleteById(id);
    }

    // -------------------------------------------------------
    // RECHERCHES
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponseDTO> rechercherParCategorie(String categorie) {
        return produitRepository.findByCategorieIgnoreCase(categorie)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProduitResponseDTO> rechercherParNom(String keyword) {
        return produitRepository.findByNomContainingIgnoreCase(keyword)
                .stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // MAPPING PRIVE : DTO <-> Entity
    // -------------------------------------------------------
    private Produit mapToEntity(ProduitRequestDTO dto) {
        return Produit.builder()
                .nom(dto.getNom())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .stock(dto.getStock())
                .categorie(dto.getCategorie())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();
    }

    private ProduitResponseDTO mapToResponseDTO(Produit p) {
        return ProduitResponseDTO.builder()
                .id(p.getId())
                .nom(p.getNom())
                .description(p.getDescription())
                .prix(p.getPrix())
                .stock(p.getStock())
                .categorie(p.getCategorie())
                .actif(p.getActif())
                .dateCreation(p.getDateCreation())
                .build();
    }

}
