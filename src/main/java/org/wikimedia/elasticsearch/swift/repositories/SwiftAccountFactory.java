package org.wikimedia.elasticsearch.swift.repositories;

import org.javaswift.joss.model.Account;

public class SwiftAccountFactory {

    public static Account createAccount(SwiftService swiftService, String url, String username, String password, String tenantName, String authMethod, String region) {
        if ("KEYSTONE".equals(authMethod.toUpperCase())) {
            return swiftService.swiftKeyStone(url, username, password, tenantName, region );
        }

        if ("TEMPAUTH".equals(authMethod.toUpperCase())) {
            return swiftService.swiftTempAuth(url, username, password);
        }
        return swiftService.swiftBasic(url, username, password);
    }

}
