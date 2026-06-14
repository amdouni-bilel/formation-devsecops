package com.example.formation_devsecops.service;


import com.example.formation_devsecops.dto.ProduitRequestDTO;
import com.example.formation_devsecops.dto.ProduitResponseDTO;

import java.util.List;

public interface ProduitService {

    ProduitResponseDTO creerProduit(ProduitRequestDTO requestDTO);

    List<ProduitResponseDTO> getTousProduits();

    ProduitResponseDTO getProduitById(Long id);

    ProduitResponseDTO modifierProduit(Long id, ProduitRequestDTO requestDTO);

    void supprimerProduit(Long id);

    List<ProduitResponseDTO> rechercherParCategorie(String categorie);

    List<ProduitResponseDTO> rechercherParNom(String keyword);

}


