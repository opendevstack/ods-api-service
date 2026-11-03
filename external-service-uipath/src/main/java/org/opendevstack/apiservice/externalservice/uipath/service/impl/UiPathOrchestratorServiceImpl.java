package org.opendevstack.apiservice.externalservice.uipath.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.uipath.config.UiPathProperties;
import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.QueueItemStatus;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathAuthResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathODataResponse;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult;
import org.opendevstack.apiservice.externalservice.uipath.service.UiPathOrchestratorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of UiPathOrchestratorService for UIPath Orchestrator.
 * Provides integration with UIPath Orchestrator for managing queue items
 * and checking robot execution status.
 */
@Service("uiPathOrchestratorService")
@Slf4j
public class UiPathOrchestratorServiceImpl implements UiPathOrchestratorService {

    private final RestClient restClient;
    private final UiPathProperties properties;

    public UiPathOrchestratorServiceImpl(
            @Qualifier("uiPathRestClient") RestClient restClient,
            @Qualifier("uiPathOrchestratorProperties") UiPathProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public String authenticate() throws UiPathException.AuthenticationException {
        log.debug("Authenticating to UIPath Orchestrator at {}", properties.getHost());

        try {
            UiPathAuthRequest authRequest = new UiPathAuthRequest(
                    properties.getTenancyName(),
                    properties.getClientId(),
                    properties.getClientSecret());

            UiPathAuthResponse authResponse = restClient.post()
                    .uri(properties.getLoginUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(authRequest)
                    .retrieve()
                    .body(UiPathAuthResponse.class);

            if (authResponse != null && authResponse.isSuccess()
                    && StringUtils.hasText(authResponse.getToken())) {
                log.debug("Successfully authenticated to UIPath Orchestrator");
                return authResponse.getToken();
            }

            String errorMsg = (authResponse != null && authResponse.getError() != null)
                    ? authResponse.getError()
                    : "Unknown authentication error";
            throw new UiPathException.AuthenticationException("Authentication failed: " + errorMsg);

        } catch (RestClientException e) {
            log.error("Failed to authenticate to UIPath Orchestrator: {}", e.getMessage(), e);
            throw new UiPathException.AuthenticationException("Authentication failed", e);
        }
    }

    @Override
    public UiPathQueueItem addQueueItem(UiPathQueueItemRequest request)
            throws UiPathException.QueueItemCreationException {

        String reference = request.getItemData() != null
                ? request.getItemData().getReference()
                : "unknown";
        log.info("Adding queue item with reference '{}'", reference);

        try {
            String token = authenticate();

            UiPathQueueItem queueItem = restClient.post()
                    .uri(properties.getQueueItemsUrl())
                    .headers(h -> applyAuthHeaders(h, token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(UiPathQueueItem.class);

            if (queueItem != null) {
                log.info("Successfully created queue item with ID {} and reference '{}'",
                        queueItem.getId(), reference);
                return queueItem;
            }
            throw new UiPathException.QueueItemCreationException(reference, "Empty response body");

        } catch (UiPathException.AuthenticationException e) {
            log.error("Failed to authenticate before adding queue item: {}", e.getMessage(), e);
            throw new UiPathException.QueueItemCreationException(reference, e);
        } catch (RestClientException e) {
            log.error("Failed to add queue item with reference '{}': {}", reference, e.getMessage(), e);
            throw new UiPathException.QueueItemCreationException(reference, e);
        }
    }

    @Override
    @Async
    public CompletableFuture<UiPathQueueItem> addQueueItemAsync(UiPathQueueItemRequest request) {
        try {
            return CompletableFuture.completedFuture(addQueueItem(request));
        } catch (UiPathException.QueueItemCreationException e) {
            log.error("Async queue item creation failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public UiPathQueueItem getQueueItemById(Long queueItemId)
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {

        log.debug("Getting queue item by ID: {}", queueItemId);

        try {
            String token = authenticate();
            String url = properties.getQueueItemsUrl() + "(" + queueItemId + ")";

            UiPathQueueItem queueItem = restClient.get()
                    .uri(url)
                    .headers(h -> applyAuthHeaders(h, token))
                    .retrieve()
                    .body(UiPathQueueItem.class);

            if (queueItem != null) {
                log.debug("Found queue item {} with status: {}", queueItemId, queueItem.getStatus());
                return queueItem;
            }
            throw new UiPathException.QueueItemNotFoundException(queueItemId.toString());

        } catch (UiPathException.AuthenticationException e) {
            log.error("Authentication failed while getting queue item: {}", e.getMessage());
            throw new UiPathException.StatusCheckException(queueItemId.toString(), e);
        } catch (RestClientException e) {
            log.debug("Queue item not found: {}", queueItemId);
            throw new UiPathException.QueueItemNotFoundException(queueItemId.toString());
        }
    }

    @Override
    public List<UiPathQueueItem> getQueueItemsByReference(String reference)
            throws UiPathException.StatusCheckException {

        log.debug("Getting queue items by reference: '{}'", reference);

        try {
            String token = authenticate();
            String url = UriComponentsBuilder.fromUriString(properties.getQueueItemsUrl())
                    .queryParam("$filter", "Reference eq '" + reference + "'")
                    .build()
                    .toUriString();

            UiPathODataResponse<UiPathQueueItem> odataResponse = restClient.get()
                    .uri(url)
                    .headers(h -> applyAuthHeaders(h, token))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            List<UiPathQueueItem> items = (odataResponse != null) ? odataResponse.getValue() : null;
            log.debug("Found {} queue item(s) with reference '{}'",
                    items != null ? items.size() : 0, reference);
            return items != null ? items : List.of();

        } catch (UiPathException.AuthenticationException e) {
            log.error("Authentication failed while querying by reference: {}", e.getMessage());
            throw new UiPathException.StatusCheckException(reference, e);
        } catch (RestClientException e) {
            log.error("Failed to query queue items by reference '{}': {}", reference, e.getMessage(), e);
            throw new UiPathException.StatusCheckException(reference, e);
        }
    }

    @Override
    public Optional<UiPathQueueItem> getLatestQueueItemByReference(String reference)
            throws UiPathException.StatusCheckException {

        log.debug("Getting latest queue item by reference: '{}'", reference);
        List<UiPathQueueItem> items = getQueueItemsByReference(reference);

        if (items.isEmpty()) {
            log.debug("No queue items found with reference '{}'", reference);
            return Optional.empty();
        }

        Optional<UiPathQueueItem> latestItem = items.stream()
                .max(Comparator.comparing(UiPathQueueItem::getId));

        latestItem.ifPresent(item ->
                log.debug("Latest queue item for reference '{}' is ID {} with status: {}",
                        reference, item.getId(), item.getStatus()));
        return latestItem;
    }

    @Override
    public boolean hasQueueItemFinalized(String reference)
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {

        log.debug("Checking if queue item with reference '{}' has finalized", reference);
        Optional<UiPathQueueItem> latestItem = getLatestQueueItemByReference(reference);

        if (latestItem.isEmpty()) {
            throw new UiPathException.QueueItemNotFoundException(reference, "reference");
        }

        UiPathQueueItem item = latestItem.get();
        boolean finalized = item.isFinalized();
        log.debug("Queue item {} (reference '{}') finalized status: {} (status: {})",
                item.getId(), reference, finalized, item.getStatus());
        return finalized;
    }

    @Override
    public boolean hasQueueItemFinalizedById(Long queueItemId)
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException {

        log.debug("Checking if queue item {} has finalized", queueItemId);
        UiPathQueueItem item = getQueueItemById(queueItemId);
        boolean finalized = item.isFinalized();
        log.debug("Queue item {} finalized status: {} (status: {})",
                queueItemId, finalized, item.getStatus());
        return finalized;
    }

    @Override
    public boolean validateConnection() {
        try {
            String token = authenticate();
            boolean isValid = StringUtils.hasText(token);
            log.debug("Connection validation: {}", isValid ? "successful" : "failed");
            return isValid;
        } catch (Exception e) {
            log.warn("Connection validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return validateConnection();
        } catch (Exception e) {
            log.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public UiPathQueueItemResult checkQueueItemByReference(String reference) {
        if (reference == null || reference.isEmpty()) {
            log.debug("No UIPath reference provided, returning NO_REFERENCE result");
            return UiPathQueueItemResult.noReference();
        }

        try {
            log.debug("Checking UIPath queue item status for reference: '{}'", reference);
            Optional<UiPathQueueItem> queueItem = getLatestQueueItemByReference(reference);

            if (queueItem.isEmpty()) {
                log.warn("UIPath queue item not found for reference: '{}'", reference);
                return UiPathQueueItemResult.notFound(reference);
            }

            UiPathQueueItem item = queueItem.get();
            QueueItemStatus status = item.getStatusEnum();
            log.debug("UIPath queue item '{}' status: {}", reference, status);

            if (!status.isFinalState()) {
                log.debug("UIPath queue item '{}' is still in progress with status: {}", reference, status);
                return UiPathQueueItemResult.inProgress(item);
            }

            if (!status.isSuccessful()) {
                log.warn("UIPath queue item '{}' failed with status: {}", reference, status);
                return UiPathQueueItemResult.failure(item);
            }

            log.debug("UIPath queue item '{}' completed successfully", reference);
            return UiPathQueueItemResult.success(item);

        } catch (UiPathException.StatusCheckException e) {
            log.error("Failed to check UIPath status for reference '{}': {}", reference, e.getMessage(), e);
            return UiPathQueueItemResult.error("Failed to check UIPath status", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error checking UIPath status for reference '{}': {}", reference, e.getMessage(), e);
            return UiPathQueueItemResult.error("Unexpected error checking UIPath", e.getMessage());
        }
    }

    /**
     * Applies Bearer token auth and optional organization unit header.
     */
    private void applyAuthHeaders(HttpHeaders headers, String token) {
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(properties.getOrganizationUnitId())) {
            headers.set("X-UIPATH-OrganizationUnitId", properties.getOrganizationUnitId());
        }
    }
}
