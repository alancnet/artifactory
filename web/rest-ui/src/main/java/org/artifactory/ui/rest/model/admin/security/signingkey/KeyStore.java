package org.artifactory.ui.rest.model.admin.security.signingkey;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class KeyStore extends BaseModel {

    private Boolean isKeyValid;
    private String password;
    private String alias;
    private String keyPairName;
    private String fileName;
    private String privateKeyPassword;
    private boolean keyStoreExist;
    List<String> keyStorePairNames;

    public KeyStore() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public KeyStore(Boolean isKeyValid, String alias, String fileName, String password) {
        this.isKeyValid = isKeyValid;
        this.alias = alias;
        this.fileName = fileName;
        this.password = password;
    }

    public Boolean isKeyValid() {
        return isKeyValid;
    }

    public void setIsKeyValid(Boolean isKeyValid) {
        this.isKeyValid = isKeyValid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

    public void setKeyPairName(String keyPairName) {
        this.keyPairName = keyPairName;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPrivateKeyPassword() {
        return privateKeyPassword;
    }

    public void setPrivateKeyPassword(String privateKeyPassword) {
        this.privateKeyPassword = privateKeyPassword;
    }

    public boolean isKeyStoreExist() {
        return keyStoreExist;
    }

    public void setKeyStoreExist(boolean keyStoreExist) {
        this.keyStoreExist = keyStoreExist;
    }

    public List<String> getKeyStorePairNames() {
        return keyStorePairNames;
    }

    public void setKeyStorePairNames(List<String> keyStorePairNames) {
        this.keyStorePairNames = keyStorePairNames;
    }
}
