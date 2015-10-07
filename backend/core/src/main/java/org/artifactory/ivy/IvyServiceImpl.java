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

package org.artifactory.ivy;

import org.apache.commons.io.IOUtils;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.util.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@Service
public class IvyServiceImpl implements IvyService {

    private static final Logger log = LoggerFactory.getLogger(IvyServiceImpl.class);

    @Autowired
    private InternalRepositoryService repositoryService;

    @Override
    public ModuleDescriptor parseIvyFile(File file) {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            return parseIvy(input, file.length());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Could not parse Ivy file.", e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public ModuleDescriptor parseIvyFile(RepoPath repoPath) {
        LocalRepo localRepo = repositoryService.localOrCachedRepositoryByKey(repoPath.getRepoKey());
        String content = localRepo.getTextFileContent(repoPath);
        StringInputStream input = null;
        try {
            input = new StringInputStream(content);
            return parseIvy(input, content.length());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Could not parse Ivy file.", e);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    private ModuleDescriptor parseIvy(InputStream input, long contentLength) {
        IvyParser ivyParser = new IvyParser();
        try {
            ModuleDescriptor md = ivyParser.getModuleDescriptorForStringContent(input, contentLength);
            return md;
        } catch (Exception e) {
            log.warn("Could not parse the item at {} as a valid Ivy file.", e);
            return null;
        }
    }
}

