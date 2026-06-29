package org.opendevstack.apiservice.project.facade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.service.MarketplaceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CatalogItemsFacade {

    private final MarketplaceService marketplaceExternalService;

    public List<CatalogItem> getAllCatalogItems() throws MarketplaceException {
        return marketplaceExternalService.getAllCatalogItems();
    }

}
