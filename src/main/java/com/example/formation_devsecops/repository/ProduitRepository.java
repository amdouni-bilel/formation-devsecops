package com.example.formation_devsecops.repository;


import com.example.formation_devsecops.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    // Spring Data génère : SELECT * FROM produits WHERE actif = true
    List<Produit> findByActifTrue();

    // SELECT * FROM produits WHERE UPPER(categorie) = UPPER(?)
    List<Produit> findByCategorieIgnoreCase(String categorie);

    // SELECT * FROM produits WHERE UPPER(nom) LIKE UPPER('%?%')
    List<Produit> findByNomContainingIgnoreCase(String keyword);

    // 🔐 DevSecOps : JPQL avec paramètres nommés → protection SQL Injection
    // Jamais de concaténation de chaînes dans les requêtes !
    @Query("SELECT p FROM Produit p WHERE p.prix BETWEEN :min AND :max")
    List<Produit> findByPrixBetween(@Param("min") Double min,
                                    @Param("max") Double max);

}

