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

import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoLayoutBuilder;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
@Test
public class SpringMaven23ModuleInfoUtilsTest extends BaseModuleInfoUtilsTest {

    public SpringMaven23ModuleInfoUtilsTest() {
        super(RepoLayoutUtils.MAVEN_2_DEFAULT);
    }

    public void testSpringDistSnapshot() {
        RepoLayout layout = new RepoLayoutBuilder().artifactPathPattern("[org]/[module]-[baseRev]" +
                ".[fileItegRev](-[classifier]).[ext]").
                fileIntegrationRevisionRegExp("?:CI\\-(\\d+)").build();

        ModuleInfo module = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.3").fileIntegrationRevision("31").ext("zip").build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.3.CI-31.zip", module, layout);

        ModuleInfo moduleWithClassifier = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.3").fileIntegrationRevision("31").classifier("with-dependencies").ext("zip").
                build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.3.CI-31-with-dependencies.zip", moduleWithClassifier, layout);
    }

    public void testSpringDistMilestone() {
        RepoLayout layout = new RepoLayoutBuilder().artifactPathPattern("[org]/[module]-[baseRev]" +
                ".[fileItegRev](-[classifier]).[ext]").
                fileIntegrationRevisionRegExp("?:M(\\d+)").build();

        ModuleInfo module = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.0").fileIntegrationRevision("1").ext("zip").build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.0.M1.zip", module, layout);

        ModuleInfo moduleWithClassifier = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.0").fileIntegrationRevision("1").classifier("with-dependencies").ext("zip").
                build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.0.M1-with-dependencies.zip", moduleWithClassifier, layout);
    }

    public void testSpringDistRelease() {
        RepoLayout layout = new RepoLayoutBuilder().artifactPathPattern("[org]/[module]-[baseRev]" +
                ".RELEASE(-[classifier]).[ext]").build();

        ModuleInfo module = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.0").ext("zip").build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.0.RELEASE.zip", module, layout);

        ModuleInfo moduleWithClassifier = new ModuleInfoBuilder().organization("FLEX").module("spring-flex").
                baseRevision("1.0.0").classifier("with-dependencies").ext("zip").build();
        testArtifactPathToModule("FLEX/spring-flex-1.0.0.RELEASE-with-dependencies.zip", moduleWithClassifier, layout);
    }

    public void testSpringArtifacts() {

        ModuleInfo module = new ModuleInfoBuilder().organization("com.springsource").module("groovy-eclipse-compiler").
                baseRevision("0.0.1").folderIntegrationRevision("SNAPSHOT").fileIntegrationRevision("SNAPSHOT").
                ext("jar").build();
        testArtifactPathToModule("com/springsource/groovy-eclipse-compiler/0.0.1-SNAPSHOT/" +
                "groovy-eclipse-compiler-0.0.1-SNAPSHOT.jar", module);

        module = new ModuleInfoBuilder().organization("com.springsource.insight").
                module("com.springsource.insight.collection.tcserver").baseRevision("1.0.0.M2-RC2").
                classifier("sources").ext("jar").build();
        testArtifactPathToModule("com/springsource/insight/com.springsource.insight.collection.tcserver/1.0.0.M2-RC2/" +
                "com.springsource.insight.collection.tcserver-1.0.0.M2-RC2-sources.jar",
                module);

        module = new ModuleInfoBuilder().organization("org.apache.ivy").module("ivy").
                baseRevision("2.0.0-alpha2-incubating").ext("jar").build();
        testArtifactPathToModule("org/apache/ivy/ivy/2.0.0-alpha2-incubating/ivy-2.0.0-alpha2-incubating.jar", module);
    }

    public void testSpringBundles() {
        ModuleInfo module = new ModuleInfoBuilder().organization("org.antlr").
                module("com.springsource.org.antlr.stringtemplate").baseRevision("3.1.0.b1").classifier("license").
                ext("txt").build();
        testArtifactPathToModule("org/antlr/com.springsource.org.antlr.stringtemplate/3.1.0.b1/" +
                "com.springsource.org.antlr.stringtemplate-3.1.0.b1-license.txt", module);

        module = new ModuleInfoBuilder().organization("org.apache.maven.plugins").module("maven-par-plugin").
                baseRevision("1.0.0.BUILD").folderIntegrationRevision("SNAPSHOT").
                fileIntegrationRevision("20090624.153253-1").classifier("sources").ext("jar").build();
        testArtifactPathToModule("org/apache/maven/plugins/maven-par-plugin/1.0.0.BUILD-SNAPSHOT/" +
                "maven-par-plugin-1.0.0.BUILD-20090624.153253-1-sources.jar", module);

        module = new ModuleInfoBuilder().organization("org.apache.maven.plugins").module("maven-par-plugin").
                baseRevision("1.0.0.BUILD").folderIntegrationRevision("SNAPSHOT").
                fileIntegrationRevision("20090624.153253-1").ext("jar").build();
        testArtifactPathToModule("org/apache/maven/plugins/maven-par-plugin/1.0.0.BUILD-SNAPSHOT/" +
                "maven-par-plugin-1.0.0.BUILD-20090624.153253-1.jar", module);
    }
}
