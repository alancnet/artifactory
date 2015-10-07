/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.repo.interceptor;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.api.repo.exception.maven.BadPomException;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.maven.PomTargetPathValidator;
import org.artifactory.md.Properties;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.StoringRepo;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.fs.MutableVfsFile;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.ImportInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interceptor that checks the validity of POM files and if it represents a maven plugin.
 *
 * @author Yossi Shaul
 */
public class MavenPomInterceptor extends StorageInterceptorAdapter implements ImportInterceptor {
    private static final Logger log = LoggerFactory.getLogger(MavenPomInterceptor.class);

    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder) {
        if (!fsItem.isFile()) {
            return;
        }

        VfsFile fsFile = (VfsFile) fsItem;
        if (MavenNaming.isPom(fsItem.getName())) {
            InternalRepositoryService repoService = ContextHelper.get().beanForType(
                    InternalRepositoryService.class);
            StoringRepo storingRepo = repoService.storingRepositoryByKey(fsItem.getRepoKey());
            if (!storingRepo.isReal()) {
                return;
            }
            boolean suppressPomConsistencyChecks = storingRepo.isSuppressPomConsistencyChecks();
            ModuleInfo moduleInfo = storingRepo.getItemModuleInfo(fsItem.getPath());
            PomTargetPathValidator pomValidator = new PomTargetPathValidator(fsItem.getPath(), moduleInfo);
            InputStream is = fsFile.getStream();
            try {
                pomValidator.validate(is, suppressPomConsistencyChecks);
            } catch (BadPomException e) {
                throw new RuntimeException("Failed to validate pom file: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse pom file: " + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }

            if (pomValidator.isMavenPlugin()) {
                log.debug("Marking {} as maven plugin", fsItem.getRepoPath());
                MutableVfsFile mutableFile = storingRepo.getMutableFile(fsItem.getRepoPath());
                Properties properties = mutableFile.getProperties();
                properties.put(PropertiesService.MAVEN_PLUGIN_PROPERTY_NAME, Boolean.toString(true));
                mutableFile.setProperties(properties);
            }
        }
    }

    @Override
    public void afterImport(VfsItem fsItem, MutableStatusHolder statusHolder) {
        afterCreate(fsItem, statusHolder);
    }
}
