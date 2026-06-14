package com.example.formation_devsecops.exception;

public class ProduitNotFoundException extends RuntimeException {

    public ProduitNotFoundException(String message) {
        super(message);
    }

}

