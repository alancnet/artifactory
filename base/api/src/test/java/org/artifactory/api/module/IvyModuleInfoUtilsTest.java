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
public class IvyModuleInfoUtilsTest extends BaseModuleInfoUtilsTest {

    public IvyModuleInfoUtilsTest() {
        super(RepoLayoutUtils.IVY_DEFAULT);
    }

    public void testRelease() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                ext("jar").type("jar");

        testArtifactModuleToModule(builder.build());

        builder.ext("xml");
        builder.type("xml");
        testDescriptorPathToModule("org.moo/bob/1.0/xmls/ivy-1.0.xml", builder.build());
    }

    public void testReleaseWithClassifier() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                classifier("sources").ext("jar").type("java-source");

        testArtifactModuleToModule(builder.build());
    }

    public void testUniqueSnapshot() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("22222222222222").fileIntegrationRevision("22222222222222").ext("jar").
                type("jar");
        testArtifactModuleToModule(builder.build());

        testArtifactPathToModule("org.moo/bob/1.0-22222222222222/jars/bob-1.0-22222222222222.jar", builder.build());

        builder.ext("xml");
        builder.type("xml");
        testDescriptorPathToModule("org.moo/bob/1.0-22222222222222/xmls/ivy-1.0-22222222222222.xml", builder.build());
    }

    public void testUniqueSnapshotWithClassifier() {
        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("22222222222222").fileIntegrationRevision("22222222222222").
                classifier("sources").ext("jar").type("java-source").build();
        testArtifactModuleToModule(expectedModule);

        testArtifactPathToModule("org.moo/bob/1.0-22222222222222/java-sources/bob-sources-1.0-22222222222222.jar",
                expectedModule);
    }

    public void testLowercaseSnapshot() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .folderIntegrationRevisionRegExp("\\d{8}")
                .fileIntegrationRevisionRegExp("\\d{8}")
                .build();

        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("com").module("popo").baseRevision("snapshot").
                folderIntegrationRevision("20080530").fileIntegrationRevision("20080530").ext("pom").type("pom").
                build();

        testArtifactPathToModule("com/popo/snapshot-20080530/poms/popo-snapshot-20080530.pom", expectedModule, layout);
    }

    public void testTimezoneSnapshot() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .folderIntegrationRevisionRegExp("\\d{12}\\+\\d{4}")
                .fileIntegrationRevisionRegExp("\\d{12}\\+\\d{4}")
                .build();

        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("com").module("popo").
                baseRevision("0.9-build-daemon").folderIntegrationRevision("200805300000+3200").
                fileIntegrationRevision("200805300000+3200").classifier("bin").ext("pom").type("pom").build();

        testArtifactPathToModule(
                "com/popo/0.9-build-daemon-200805300000+3200/poms/popo-bin-0.9-build-daemon-200805300000+3200.pom",
                expectedModule, layout);
    }

    public void testLinkedInIvyModule() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .distinctiveDescriptorPathPattern(true)
                .descriptorPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).ivy")
                .folderIntegrationRevisionRegExp("\\d{14}")
                .fileIntegrationRevisionRegExp("\\d{14}")
                .build();

        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                folderIntegrationRevision("22222222222222").fileIntegrationRevision("22222222222222").type("ivy").
                ext("ivy");
        testDescriptorPathToModule("org.moo/bob/1.0-22222222222222/ivys/bob-1.0-22222222222222.ivy", builder.build(),
                layout);
    }

    public void testGradleStyle() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[orgPath]/[module]/[type]s/[module]-[baseRev]" +
                        "(-[fileItegRev])(-[classifier]).[ext]")
                .distinctiveDescriptorPathPattern(true)
                .descriptorPathPattern("[orgPath]/[module]/[type]s/ivy-[baseRev]" +
                        "(-[fileItegRev]).xml")
                .folderIntegrationRevisionRegExp("\\d{14}")
                .fileIntegrationRevisionRegExp("\\d{14}")
                .build();

        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0").
                type("jar").ext("jar");

        //Test release - fails. no way to guess properly
        //testArtifactPathToModule("org/moo/bob/jars/bob-1.0.jar", builder.build(), layout);

        //Test snapshot artifact
        builder.fileIntegrationRevision("22222222222222");
        testArtifactPathToModule("org/moo/bob/jars/bob-1.0-22222222222222.jar", builder.build(), layout);

        //Test artifact with classifer
        builder.classifier("sources");
        testArtifactPathToModule("org/moo/bob/jars/bob-1.0-22222222222222-sources.jar", builder.build(), layout);

        //Test module
        builder.classifier(null);
        builder.type("xml");
        builder.ext("xml");
        testDescriptorPathToModule("org/moo/bob/xmls/ivy-1.0-22222222222222.xml", builder.build(), layout);
    }

    public void testSuperLongRelease() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").ext("jar").type("jar");

        testArtifactModuleToModule(builder.build());
        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/jars/" +
                "bob-mcmoo-mbob-1.0.jar", builder.build());

        builder.ext("xml");
        builder.type("xml");
        testDescriptorPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/xmls/ivy-1.0.xml",
                builder.build());
    }

    public void testSuperLongReleaseWithClassifier() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").classifier("sources").ext("jar").type("java-source");

        testArtifactModuleToModule(builder.build());
        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0/java-sources/" +
                "bob-mcmoo-mbob-sources-1.0.jar", builder.build());
    }

    public void testSuperLongUniqueSnapshot() {
        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("22222222222222").
                fileIntegrationRevision("22222222222222").ext("jar").type("jar");
        testArtifactModuleToModule(builder.build());

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-22222222222222/jars/" +
                "bob-mcmoo-mbob-1.0-22222222222222.jar", builder.build());

        builder.ext("xml");
        builder.type("xml");
        testDescriptorPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-22222222222222/" +
                "xmls/ivy-1.0-22222222222222.xml", builder.build());
    }

    public void testSuperLongUniqueSnapshotWithClassifier() {
        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("22222222222222").
                fileIntegrationRevision("22222222222222").classifier("sources").ext("jar").type("java-source").build();
        testArtifactModuleToModule(expectedModule);

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-22222222222222/" +
                "java-sources/bob-mcmoo-mbob-sources-1.0-22222222222222.jar", expectedModule);
    }

    public void testSuperLongLowercaseSnapshot() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .folderIntegrationRevisionRegExp("\\d{8}")
                .fileIntegrationRevisionRegExp("\\d{8}")
                .build();

        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("snapshot").folderIntegrationRevision("20080530").
                fileIntegrationRevision("20080530").ext("pom").type("pom").build();

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/snapshot-20080530/poms/" +
                "bob-mcmoo-mbob-snapshot-20080530.pom", expectedModule, layout);
    }

    public void testSuperLongTimezoneSnapshot() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .folderIntegrationRevisionRegExp("\\d{12}\\+\\d{4}")
                .fileIntegrationRevisionRegExp("\\d{12}\\+\\d{4}")
                .build();

        ModuleInfo expectedModule = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("0.9-build-daemon").
                folderIntegrationRevision("200805300000+3200").fileIntegrationRevision("200805300000+3200").
                classifier("bin").ext("pom").type("pom").build();

        testArtifactPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/" +
                "0.9-build-daemon-200805300000+3200/poms/bob-mcmoo-mbob-bin-0.9-build-daemon-200805300000+3200.pom",
                expectedModule, layout);
    }

    public void testSuperLongLinkedInIvyModule() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).[ext]")
                .distinctiveDescriptorPathPattern(true)
                .descriptorPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]s/" +
                        "[module](-[classifier])-[baseRev](-[fileItegRev]).ivy")
                .folderIntegrationRevisionRegExp("\\d{14}")
                .fileIntegrationRevisionRegExp("\\d{14}")
                .build();

        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").folderIntegrationRevision("22222222222222").
                fileIntegrationRevision("22222222222222").type("ivy").ext("ivy");
        testDescriptorPathToModule("this.is.a-super.ultra-really.mega-long.org/bob-mcmoo-mbob/1.0-22222222222222/" +
                "ivys/bob-mcmoo-mbob-1.0-22222222222222.ivy", builder.build(), layout);
    }

    public void testSuperLongGradleStyle() {
        RepoLayout layout = new RepoLayoutBuilder()
                .artifactPathPattern("[orgPath]/[module]/[type]s/[module]-[baseRev]" +
                        "(-[fileItegRev])(-[classifier]).[ext]")
                .distinctiveDescriptorPathPattern(true)
                .descriptorPathPattern("[orgPath]/[module]/[type]s/ivy-[baseRev]" +
                        "(-[fileItegRev]).xml")
                .folderIntegrationRevisionRegExp("\\d{14}")
                .fileIntegrationRevisionRegExp("\\d{14}")
                .build();

        ModuleInfoBuilder builder = new ModuleInfoBuilder().organization("this.is.a-super.ultra-really.mega-long.org").
                module("bob-mcmoo-mbob").baseRevision("1.0").type("jar").ext("jar");

        //Test release - fails. no way to guess properly
        //testArtifactPathToModule("org/moo/bob-mcmoo-mbob/jars/bob-mcmoo-mbob-1.0.jar", builder.build(), layout);

        //Test snapshot artifact
        builder.fileIntegrationRevision("22222222222222");
        testArtifactPathToModule("this/is/a-super/ultra-really/mega-long/org/bob-mcmoo-mbob/jars/" +
                "bob-mcmoo-mbob-1.0-22222222222222.jar", builder.build(), layout);

        //Test artifact with classifer
        builder.classifier("sources");
        testArtifactPathToModule("this/is/a-super/ultra-really/mega-long/org/bob-mcmoo-mbob/jars/" +
                "bob-mcmoo-mbob-1.0-22222222222222-sources.jar", builder.build(), layout);

        //Test module
        builder.classifier(null);
        builder.type("xml");
        builder.ext("xml");
        testDescriptorPathToModule("this/is/a-super/ultra-really/mega-long/org/bob-mcmoo-mbob/xmls/" +
                "ivy-1.0-22222222222222.xml", builder.build(), layout);
    }
}