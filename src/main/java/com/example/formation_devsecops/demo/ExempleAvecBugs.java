//package com.example.formation_devsecops.demo;
//
//
///**
// * Classe de démonstration avec des bugs intentionnels
// * pour montrer comment SonarCloud les détecte
// */
//public class ExempleAvecBugs {
//
//    // BUG 1: Méthode qui peut causer une NullPointerException
//    public String getNomComplet(String prenom, String nom) {
//        // SonarCloud détectera: Possible NullPointerException
//        return prenom.toUpperCase() + " " + nom.toUpperCase();
//    }
//
//    // BUG 2: Division par zéro potentielle
//    public int diviser(int a, int b) {
//        // SonarCloud détectera: Division by zero possible
//        return a / b;
//    }
//
//    // BUG 3: Ressource non fermée
//    public void lireFichier(String path) {
//        // SonarCloud détectera: Resource leak
//        java.io.FileReader reader = null;
//        try {
//            reader = new java.io.FileReader(path);
//            // ... lecture
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        // reader n'est jamais fermé !
//    }
//
//    // CODE SMELL 1: Méthode trop complexe (complexité cyclomatique élevée)
//    public String evaluerNote(int note) {
//        if (note >= 90) {
//            return "Excellent";
//        } else if (note >= 80) {
//            return "Très bien";
//        } else if (note >= 70) {
//            return "Bien";
//        } else if (note >= 60) {
//            return "Assez bien";
//        } else if (note >= 50) {
//            return "Passable";
//        } else {
//            return "Insuffisant";
//        }
//    }
//
//    // CODE SMELL 2: Code dupliqué
//    public void methode1() {
//        System.out.println("Début du traitement");
//        System.out.println("Traitement en cours...");
//        System.out.println("Fin du traitement");
//    }
//
//    public void methode2() {
//        System.out.println("Début du traitement");
//        System.out.println("Traitement en cours...");
//        System.out.println("Fin du traitement");
//    }
//
//    // VULNERABILITY: Injection SQL potentielle
//    public void rechercherUtilisateur(String nom) {
//        String query = "SELECT * FROM users WHERE name = '" + nom + "'";
//        // SonarCloud détectera: SQL Injection vulnerability
//        System.out.println("Exécution de: " + query);
//    }
//}
