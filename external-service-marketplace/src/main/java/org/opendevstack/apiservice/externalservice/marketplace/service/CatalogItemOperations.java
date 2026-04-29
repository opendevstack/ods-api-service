package org.opendevstack.apiservice.externalservice.marketplace.service;

import org.opendevstack.apiservice.externalservice.marketplace.openapi.model.ProjectComponentExtendedInfo;

import java.util.Base64;

public class CatalogItemOperations {

    private CatalogItemOperations() {
    }

    public static byte[] encodeId(String id) {
        return Base64.getUrlEncoder().encode(id.getBytes());
    }

    public static byte[] decodeId(String id) {
        return Base64.getUrlDecoder().decode(id);
    }

    public static String buildCatalogItemId(ProjectComponentExtendedInfo component) {
        if (component == null || component.getCatalogItemId() == null || component.getCatalogItemRef() == null) {
            return null;
        }
        return new String(encodeId(
                new String(decodeId(component.getCatalogItemId())) + new String(decodeId(component.getCatalogItemRef()))));
    }
}
