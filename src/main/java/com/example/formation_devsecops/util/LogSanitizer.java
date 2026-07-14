package com.example.formation_devsecops.util;

/**
 * Utilitaire pour assainir les valeurs insérées dans les logs.
 *
 * SpotBugs signale des risques d'injection CRLF lorsque des données
 * externes sont loggées telles quelles. Cette classe centralise la
 * sanitation minimale (suppression de \r et \n) avant d'écrire
 * dans les logs.
 */
public final class LogSanitizer {

    private LogSanitizer() {
        // utilitaire
    }

    /**
     * Supprime les caractères de nouvelle ligne \r et \n d'une chaîne.
     * Retourne "null" si l'objet est null.
     */
    public static String sanitize(String s) {
        if (s == null) return "null";
        return s.replaceAll("[\\r\\n]", "");
    }

    /**
     * Sanitize pour tout objet (appelle String.valueOf puis sanitize).
     */
    public static String sanitize(Object o) {
        return sanitize(String.valueOf(o));
    }
}

