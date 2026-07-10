package org.opendevstack.apiservice.marketplacemetrics.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.marketplacemetrics.controller.MarketplaceMetricsController;
import org.opendevstack.apiservice.marketplacemetrics.exception.MarketplaceMetricsException;
import org.opendevstack.apiservice.marketplacemetrics.model.RestErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MarketplaceMetricsController.class)
@Slf4j
public class MarketplaceMetricsExceptionHandler {

    @ExceptionHandler(MarketplaceMetricsException.class)
    public ResponseEntity<RestErrorMessage> handleMarketplaceMetricsException(MarketplaceMetricsException ex) {
        log.error("Marketplace metrics retrieval failed: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new RestErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> handleGenericException(Exception ex) {
        log.error("Unexpected error while processing marketplace metrics request: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RestErrorMessage("An error occurred while processing the request."));
    }
}

