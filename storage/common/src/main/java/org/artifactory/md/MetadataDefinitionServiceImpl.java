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

package org.artifactory.md;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.fs.FileInfo;
import org.artifactory.fs.FolderInfo;
import org.artifactory.fs.MutableFileInfo;
import org.artifactory.fs.MutableFolderInfo;
import org.artifactory.fs.MutableStatsInfo;
import org.artifactory.fs.MutableWatchersInfo;
import org.artifactory.fs.StatsInfo;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.mime.MavenNaming;
import org.artifactory.spring.Reloadable;
import org.artifactory.version.CompoundVersionDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author freds
 * @date Sep 3, 2008
 */
@Service
@Reloadable(beanClass = MetadataDefinitionService.class)
public class MetadataDefinitionServiceImpl implements MetadataDefinitionService {
    private static final Logger log = LoggerFactory.getLogger(MetadataDefinitionServiceImpl.class);

    private final Map<Class, MetadataDefinition> mdDefsByClass = Maps.newHashMap();
    private final Map<String, MetadataDefinition> mdDefsByName = Maps.newHashMap();

    @Override
    public void init() {
        // Internal metadata
        FolderInfoXmlProvider folderInfoXmlProvider = new FolderInfoXmlProvider();
        createMetadataDefinition(FolderInfo.class, MutableFolderInfo.class,
                folderInfoXmlProvider,
                new FolderInfoPersistenceHandler(folderInfoXmlProvider), true);
        FileInfoXmlProvider fileInfoXmlProvider = new FileInfoXmlProvider();
        createMetadataDefinition(FileInfo.class, MutableFileInfo.class,
                fileInfoXmlProvider,
                new FileInfoPersistenceHandler(fileInfoXmlProvider), true);
        WatchersXmlProvider watchersXmlProvider = new WatchersXmlProvider();
        createMetadataDefinition(WatchersInfo.class, MutableWatchersInfo.class,
                watchersXmlProvider,
                new WatchersPersistenceHandler(watchersXmlProvider), true);

        // Additional persistent metadata
        StatsInfoXmlProvider statsInfoXmlProvider = new StatsInfoXmlProvider();
        createMetadataDefinition(StatsInfo.class, MutableStatsInfo.class,
                statsInfoXmlProvider,
                new StatsInfoPersistenceHandler(statsInfoXmlProvider), false);
        PropertiesXmlProvider propertiesXmlProvider = new PropertiesXmlProvider();
        createMetadataDefinition(PropertiesInfo.class, MutablePropertiesInfo.class,
                propertiesXmlProvider,
                new PropertiesPersistenceHandler(propertiesXmlProvider), false,
                Properties.class);
        GenericXmlProvider mavenMetadataXmlProvider = new GenericXmlProvider(MavenNaming.MAVEN_METADATA_NAME);
        createMetadataDefinition(String.class, String.class,
                mavenMetadataXmlProvider,
                new GenericPersistenceHandler(mavenMetadataXmlProvider), false);
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
        mdDefsByClass.clear();
        mdDefsByName.clear();
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T, MT> MetadataDefinition<T, MT> getMetadataDefinition(Class<T> clazz) {
        MetadataDefinition<T, MT> definition = mdDefsByClass.get(clazz);
        if (definition == null) {
            throw new IllegalArgumentException("Creating new Metadata on the fly for: '" + clazz +
                    "'. Should have been initialized!");
        }
        return definition;
    }

    @Override
    public MetadataDefinition getMetadataDefinition(String metadataName, boolean createIfEmpty) {
        if (StringUtils.isBlank(metadataName)) {
            throw new IllegalArgumentException("Metadata type name to locate cannot be null.");
        }

        MetadataDefinition definition = mdDefsByName.get(metadataName);
        if (definition == null && createIfEmpty) {
            log.debug("Creating new Metadata definition on demand for '{}'.", metadataName);
            GenericXmlProvider xmlProvider = new GenericXmlProvider(metadataName);
            definition = new MetadataDefinition<>(xmlProvider,
                    new GenericPersistenceHandler(xmlProvider), false);
        }
        return definition;
    }

    @Override
    public Set<MetadataDefinition<?, ?>> getAllMetadataDefinitions(boolean includeInternal) {
        Set<MetadataDefinition<?, ?>> result = new HashSet<>();
        Collection<MetadataDefinition> mdDefColl = mdDefsByName.values();
        for (MetadataDefinition definition : mdDefColl) {
            if (includeInternal || !definition.isInternal()) {
                result.add(definition);
            }
        }
        return result;
    }

    @Override
    public MetadataDefinition<FileInfo, MutableFileInfo> getFileInfoMd() {
        return mdDefsByClass.get(FileInfo.class);
    }

    @Override
    public MetadataDefinition<FolderInfo, MutableFolderInfo> getFolderInfoMd() {
        return mdDefsByClass.get(FolderInfo.class);
    }

    private <T, MT> MetadataDefinition createMetadataDefinition(
            Class<T> clazz,
            Class<MT> mClazz,
            XmlMetadataProvider<T, MT> xmlProvider,
            MetadataPersistenceHandler<T, MT> persistenceHandler,
            boolean internal, Class... extraClassKeys) {
        MetadataDefinition definition = new MetadataDefinition<>(xmlProvider, persistenceHandler, internal);
        if (clazz != String.class) {
            mdDefsByClass.put(clazz, definition);
        }
        if (mClazz != String.class) {
            mdDefsByClass.put(mClazz, definition);
        }
        for (Class extraClassKey : extraClassKeys) {
            mdDefsByClass.put(extraClassKey, definition);
        }
        mdDefsByName.put(definition.getMetadataName(), definition);
        return definition;
    }
}
