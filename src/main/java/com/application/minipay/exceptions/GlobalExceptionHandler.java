package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException businessException) {


        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(businessException.getHttpStatus(), businessException.getMessage());
        problemDetail.setProperty("code", businessException.getErrorCode());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        problemDetail.setProperty("code", "INTERNAL_ERROR");
        return problemDetail;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage());
        if ("Idempotency-Key".equals(exception.getHeaderName())) {
            problemDetail.setProperty("code", "IDEMPOTENCY_KEY_REQUIRED");
        } else {
            problemDetail.setProperty("code", "MISSING_REQUIRED_HEADER");
        }
        return problemDetail;

    }
}
