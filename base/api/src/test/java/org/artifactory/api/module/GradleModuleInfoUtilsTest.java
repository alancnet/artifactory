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

package org.artifactory.api.module;

import org.artifactory.util.RepoLayoutUtils;
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
@Test
public class GradleModuleInfoUtilsTest extends BaseModuleInfoUtilsTest {

    public GradleModuleInfoUtilsTest() {
        super(RepoLayoutUtils.GRADLE_DEFAULT);
    }

    public void testRelease() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                ext("jar");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("org.moo/bob/1.0/bob-1.0.jar", artifactModuleInfo);

        builder.ext("xml");
        testDescriptorPathToModule("org.moo/bob/ivy-1.0.xml", builder.build());
    }

    public void testReleaseChecksum() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                ext("jar.sha1");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("org.moo/bob/1.0/bob-1.0.jar.sha1", artifactModuleInfo);
    }

    public void testReleaseWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                classifier("sources").ext("jar").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("org.moo/bob/1.0/bob-1.0-sources.jar", moduleInfo);
    }

    public void testReleaseChecksumWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                classifier("sources").ext("jar.sha1").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("org.moo/bob/1.0/bob-1.0-sources.jar.sha1", moduleInfo);
    }

    public void testIntegration() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("11111111111111").fileIntegrationRevision("22222222222222").ext("jar");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("org.moo/bob/1.0-11111111111111/bob-1.0-22222222222222.jar", artifactModuleInfo);

        builder.folderIntegrationRevision(null);
        builder.ext("xml");
        testDescriptorPathToModule("org.moo/bob/ivy-1.0-22222222222222.xml", builder.build());
    }

    public void testIntegrationChecksum() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("11111111111111").fileIntegrationRevision("22222222222222").ext("jar.md5");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("org.moo/bob/1.0-11111111111111/bob-1.0-22222222222222.jar.md5", artifactModuleInfo);
    }

    public void testIntegrationWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("11111111111111").fileIntegrationRevision("22222222222222").
                classifier("sources").ext("jar").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("org.moo/bob/1.0-11111111111111/bob-1.0-22222222222222-sources.jar", moduleInfo);
    }

    public void testIntegrationChecksumWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("11111111111111").fileIntegrationRevision("22222222222222").
                classifier("sources").ext("jar.sha1").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("org.moo/bob/1.0-11111111111111/bob-1.0-22222222222222-sources.jar.sha1", moduleInfo);
    }

    public void testSuperLongRelease() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").ext("jar");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/bob-mcmoo-mbob-1.0.jar",
                artifactModuleInfo);

        builder.ext("xml");
        testDescriptorPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/ivy-1.0.xml",
                builder.build());
    }

    public void testSuperLongReleaseChecksum() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").ext("jar.sha1");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/" +
                "bob-mcmoo-mbob-1.0.jar.sha1", artifactModuleInfo);
    }

    public void testSuperLongReleaseWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").classifier("sources").ext("jar").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/" +
                "bob-mcmoo-mbob-1.0-sources.jar", moduleInfo);
    }

    public void testSuperLongReleaseChecksumWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").classifier("sources").ext("jar.sha1").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/" +
                "bob-mcmoo-mbob-1.0-sources.jar.sha1", moduleInfo);
    }

    public void testSuperLongIntegration() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("11111111111111").
                fileIntegrationRevision("22222222222222").ext("jar");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-11111111111111/" +
                "bob-mcmoo-mbob-1.0-22222222222222.jar", artifactModuleInfo);

        builder.folderIntegrationRevision(null);
        builder.ext("xml");
        testDescriptorPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/" +
                "ivy-1.0-22222222222222.xml", builder.build());
    }

    public void testSuperLongIntegrationChecksum() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("11111111111111").
                fileIntegrationRevision("22222222222222").ext("jar.md5");
        ModuleInfo artifactModuleInfo = builder.build();
        testArtifactModuleToModule(artifactModuleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-11111111111111/" +
                "bob-mcmoo-mbob-1.0-22222222222222.jar.md5", artifactModuleInfo);
    }

    public void testSuperLongIntegrationWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("11111111111111").
                fileIntegrationRevision("22222222222222").classifier("sources").ext("jar").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-11111111111111/" +
                "bob-mcmoo-mbob-1.0-22222222222222-sources.jar", moduleInfo);
    }

    public void testSuperLongIntegrationChecksumWithClassifier() {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("11111111111111").
                fileIntegrationRevision("22222222222222").classifier("sources").ext("jar.sha1").build();
        testArtifactModuleToModule(moduleInfo);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-11111111111111/" +
                "bob-mcmoo-mbob-1.0-22222222222222-sources.jar.sha1", moduleInfo);
    }
}
