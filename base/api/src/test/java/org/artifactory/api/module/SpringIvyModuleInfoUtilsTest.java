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

import org.artifactory.descriptor.repo.RepoLayoutBuilder;
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
@Test
public class SpringIvyModuleInfoUtilsTest extends BaseModuleInfoUtilsTest {

    public SpringIvyModuleInfoUtilsTest() {
        super(new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](.[folderItegRev])/" +
                        "[module](-[classifier])-[baseRev](.[fileItegRev]).[ext]")
                .distinctiveDescriptorPathPattern(true)
                .descriptorPathPattern("[org]/[module]/[baseRev](.[folderItegRev])/" +
                        "ivy-[baseRev](.[fileItegRev]).xml")
                .folderIntegrationRevisionRegExp("CI-(?:(?:[A-Z]\\d+\\-[A-Z]\\d+)|(?:\\d+))")
                .fileIntegrationRevisionRegExp("CI-(?:(?:[A-Z]\\d+\\-[A-Z]\\d+)|(?:\\d+))")
                .build());
    }

    public void testSpringBundlesExternal() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("ch.qos.logback").
                module("com.springsource.ch.qos.logback.classic").baseRevision("0.9.24").ext("jar");

        testArtifactPathToModule("ch.qos.logback/com.springsource.ch.qos.logback.classic/0.9.24/" +
                "com.springsource.ch.qos.logback.classic-0.9.24.jar", builder.build());

        builder.classifier("sources");
        testArtifactPathToModule("ch.qos.logback/com.springsource.ch.qos.logback.classic/0.9.24/" +
                "com.springsource.ch.qos.logback.classic-sources-0.9.24.jar", builder.build());

        builder.classifier(null);
        builder.ext("xml");
        testDescriptorPathToModule("ch.qos.logback/com.springsource.ch.qos.logback.classic/0.9.24/ivy-0.9.24.xml",
                builder.build());
    }

    public void testSpringBundlesRelease() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.springframework.flex").
                module("org.springframework.flex").baseRevision("1.0.3.RELEASE").ext("jar");

        testArtifactPathToModule("org.springframework.flex/org.springframework.flex/1.0.3.RELEASE/" +
                "org.springframework.flex-1.0.3.RELEASE.jar", builder.build());

        builder.classifier("sources");
        testArtifactPathToModule("org.springframework.flex/org.springframework.flex/1.0.3.RELEASE/" +
                "org.springframework.flex-sources-1.0.3.RELEASE.jar", builder.build());

        builder.classifier(null);
        builder.ext("xml");
        testDescriptorPathToModule("org.springframework.flex/org.springframework.flex/1.0.3.RELEASE/" +
                "ivy-1.0.3.RELEASE.xml", builder.build());
    }

    public void testSpringBundlesMilestone() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.springframework.flex").
                module("org.springframework.flex").baseRevision("1.0.0.RC2").ext("jar");

        testArtifactPathToModule("org.springframework.flex/org.springframework.flex/1.0.0.RC2/" +
                "org.springframework.flex-1.0.0.RC2.jar", builder.build());

        builder.classifier("sources");
        testArtifactPathToModule("org.springframework.flex/org.springframework.flex/1.0.0.RC2/" +
                "org.springframework.flex-sources-1.0.0.RC2.jar", builder.build());

        builder.classifier(null);
        builder.ext("xml");
        testDescriptorPathToModule("org.springframework.flex/org.springframework.flex/1.0.0.RC2/" +
                "ivy-1.0.0.RC2.xml", builder.build());
    }

    public void testSpringBundlesSnapshot1() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("com.springsource.manifestexpander").
                module("com.springsource.manifestexpander").baseRevision("1.0.0").folderIntegrationRevision("CI-23").
                fileIntegrationRevision("CI-23").ext("jar");

        testArtifactPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/1.0.0.CI-23/" +
                "com.springsource.manifestexpander-1.0.0.CI-23.jar", builder.build());

        builder.classifier("sources");
        testArtifactPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/1.0.0.CI-23/" +
                "com.springsource.manifestexpander-sources-1.0.0.CI-23.jar", builder.build());

        builder.classifier(null);
        builder.ext("xml");
        testDescriptorPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/1.0.0.CI-23/" +
                "ivy-1.0.0.CI-23.xml", builder.build());
    }

    public void testSpringBundlesSnapshot2() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("com.springsource.manifestexpander").
                module("com.springsource.manifestexpander").baseRevision("1.0.0").folderIntegrationRevision(
                "CI-R11-B2").
                fileIntegrationRevision("CI-R11-B2").ext("jar");

        testArtifactPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/" +
                "1.0.0.CI-R11-B2/com.springsource.manifestexpander-1.0.0.CI-R11-B2.jar", builder.build());

        builder.classifier("sources");
        testArtifactPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/" +
                "1.0.0.CI-R11-B2/com.springsource.manifestexpander-sources-1.0.0.CI-R11-B2.jar", builder.build());

        builder.classifier(null);
        builder.ext("xml");
        testDescriptorPathToModule("com.springsource.manifestexpander/com.springsource.manifestexpander/" +
                "1.0.0.CI-R11-B2/ivy-1.0.0.CI-R11-B2.xml", builder.build());
    }
}
