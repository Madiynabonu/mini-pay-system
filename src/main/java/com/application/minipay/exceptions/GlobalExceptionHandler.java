package com.application.minipay.exceptions;

import org.springframework.http.ProblemDetail;
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
}
