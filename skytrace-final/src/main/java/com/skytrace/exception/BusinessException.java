package com.skytrace.exception;

/**
 * Levee quand une regle metier est violee (ex: bagage deja livre, identifiant deja utilise...).
 * Traduite en HTTP 409 (Conflict) par le GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
