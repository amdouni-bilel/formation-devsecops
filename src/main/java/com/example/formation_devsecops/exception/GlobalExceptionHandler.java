package com.example.formation_devsecops.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Structure de réponse d'erreur uniforme
    record ErrorResponse(int status, String message,
                         LocalDateTime timestamp, Object details) {
    }

    // Gestion des erreurs de validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> details = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        details.put(error.getField(),
                                error.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("details", details);

        return ResponseEntity.badRequest().body(response);
    }

    // Gestion : produit non trouvé → 404
    @ExceptionHandler(ProduitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ProduitNotFoundException ex) {
        log.info("[NOT_FOUND] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(404, ex.getMessage(),
                        LocalDateTime.now(), null));
    }

    // Gestion générique → 500
    // 🔐 On retourne un message générique au client, PAS la stack trace !
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("[ERREUR] Erreur inattendue", ex); // log complet côté serveur
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(500,
                        "Une erreur interne est survenue. Contactez l'administrateur.",
                        LocalDateTime.now(), null));
    }

}

