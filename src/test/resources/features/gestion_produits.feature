# Feature : description de la fonctionnalité testée
Feature: Gestion des produits BeeStore
  En tant que gestionnaire de stock
  Je veux gérer les produits via l'API REST
  Afin de maintenir le catalogue à jour

  # -------------------------------------------------------
  # Scenario : cas de test individuel
  # -------------------------------------------------------

  Scenario: Créer un produit valide
    Given l'API BeeStore est disponible
    When je crée un produit avec le nom "MacBook" et le prix 1499.99
    Then le statut HTTP de réponse est 201
    And le produit retourné a le nom "MacBook"

  Scenario: Récupérer un produit existant par son ID
    Given un produit "iPhone" avec le prix 999.99 existe en base
    When je récupère le produit avec l'ID créé
    Then le statut HTTP de réponse est 200
    And le produit retourné a le nom "iPhone"

  # 🔐 DevSecOps : scénario de test de sécurité en Gherkin
  Scenario: Rejeter un produit avec des données invalides
    Given l'API BeeStore est disponible
    When je crée un produit avec le nom "" et le prix -10
    Then le statut HTTP de réponse est 400
    And la réponse contient une erreur sur le champ "nom"

  Scenario: Supprimer un produit inexistant retourne 404
    Given l'API BeeStore est disponible
    When je supprime le produit avec l'ID 99999
    Then le statut HTTP de réponse est 404
