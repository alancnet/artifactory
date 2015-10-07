/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.security;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.security.MasterEncryptionService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.layout.EncryptConfigurationInterceptor;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.security.interceptor.MissionControlEncryptInterceptor;
import org.artifactory.security.interceptor.StoragePropertiesEncryptInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the master encryption service based on symmetric key and Base58 encoding.
 *
 * @author Yossi Shaul
 */
@Service
public class MasterEncryptionServiceImpl implements MasterEncryptionService {

    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void encrypt() {
        AccessLogger.approved("Encrypting with master encryption key");
        // Create the master key if needed
        if (!CryptoHelper.hasMasterKey()) {
            CryptoHelper.createMasterKeyFile();
        }
        StoragePropertiesEncryptInterceptor storagePropertiesEncryptInterceptor = new StoragePropertiesEncryptInterceptor();
        storagePropertiesEncryptInterceptor.encryptOrDecryptStoragePropertiesFile(true);
        MissionControlEncryptInterceptor missionControlEncryptInterceptor=new MissionControlEncryptInterceptor();
        missionControlEncryptInterceptor.encryptOrDecryptMissionControlPropertiesFile(true);
        // config interceptor will encrypt the config before it is saved to the database
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }

    @Override
    public void decrypt() {
        if (!CryptoHelper.hasMasterKey()) {
            throw new IllegalStateException("Cannot decrypt without master key file");
        }
        AccessLogger.approved("Decrypting with master encryption key");
        MutableCentralConfigDescriptor mutableDescriptor = centralConfigService.getMutableDescriptor();
        StoragePropertiesEncryptInterceptor storagePropertiesEncryptInterceptor = new StoragePropertiesEncryptInterceptor();
        storagePropertiesEncryptInterceptor.encryptOrDecryptStoragePropertiesFile(false);
        MissionControlEncryptInterceptor missionControlEncryptInterceptor=new MissionControlEncryptInterceptor();
        missionControlEncryptInterceptor.encryptOrDecryptMissionControlPropertiesFile(false);
        EncryptConfigurationInterceptor.decrypt(mutableDescriptor);
        CryptoHelper.removeMasterKeyFile();
        centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);
    }
}
