package org.wikimedia.elasticsearch.swift.repositories;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.exception.CommandException;
import org.javaswift.joss.model.Account;

public class SwiftService extends AbstractLifecycleComponent<SwiftService> {
    // The account we'll be connecting to Swift with
    private Account swiftUser;

    /**
     * Constructor
     * 
     * @param settings
     *            Settings for our repository. Injected.
     */
    @Inject
    public SwiftService(Settings settings) {
        super(settings);
    }

    /**
     * Create a Swift account object and connect it to Swift
     * 
     * @param url
     *            The auth url (eg: localhost:8080/auth/v1.0/)
     * @param username
     *            The username
     * @param password
     *            The password
     */
    public synchronized Account swiftBasic(String url, String username, String password, String preferredRegion) {
        if (swiftUser != null) {
            return swiftUser;
        }

        try {
            AccountConfig conf = getStandardConfig(url, username, password, AuthenticationMethod.BASIC, preferredRegion);
            swiftUser = new AccountFactory(conf).createAccount();
        } catch (CommandException ce) {
            throw new ElasticsearchIllegalArgumentException("Unable to authenticate to Swift Basic " + url + "/" + username + "/" + password, ce);
        }
        return swiftUser;
    }

    public synchronized Account swiftKeyStone(String url, String username, String password, String tenantName, String preferredRegion) {
        if (swiftUser != null) {
            return swiftUser;
        }

        try {
            AccountConfig conf = getStandardConfig(url, username, password, AuthenticationMethod.KEYSTONE, preferredRegion);
            conf.setTenantName(tenantName);
            swiftUser = new AccountFactory(conf).createAccount();
        } catch (CommandException ce) {
            throw new ElasticsearchIllegalArgumentException(
                    "Unable to authenticate to Swift Keystone " + url + "/" + username + "/" + password + "/" + tenantName, ce);
        }
        return swiftUser;
    }

    public synchronized Account swiftTempAuth(String url, String username, String password, String preferredRegion) {
        if (swiftUser != null) {
            return swiftUser;
        }

        try {
            AccountConfig conf = getStandardConfig(url, username, password, AuthenticationMethod.TEMPAUTH, preferredRegion);
            swiftUser = new AccountFactory(conf).createAccount();
        } catch (CommandException ce) {
            throw new ElasticsearchIllegalArgumentException("Unable to authenticate to Swift Temp", ce);
        }
        return swiftUser;
    }

    private AccountConfig getStandardConfig(String url, String username, String password, AuthenticationMethod method, String preferredRegion) {
        AccountConfig conf = new AccountConfig();
        conf.setAuthUrl(url);
        conf.setUsername(username);
        conf.setPassword(password);
        conf.setAuthenticationMethod(method);
        conf.setAllowContainerCaching(false);
        conf.setAllowCaching(false);
        conf.setPreferredRegion(preferredRegion);
        return conf;
    }

    /**
     * Start the service. No-op here.
     */
    @Override
    protected void doStart() throws ElasticsearchException {
    }

    /**
     * Stop the service. No-op here.
     */
    @Override
    protected void doStop() throws ElasticsearchException {
    }

    /**
     * Close the service. No-op here.
     */
    @Override
    protected void doClose() throws ElasticsearchException {
    }
}
