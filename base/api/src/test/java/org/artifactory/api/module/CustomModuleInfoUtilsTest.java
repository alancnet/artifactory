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
import org.testng.annotations.Test;

/**
 * @author Noam Y. Tenne
 */
public class CustomModuleInfoUtilsTest extends BaseModuleInfoUtilsTest {

    RepoLayout otherLayout = new RepoLayoutBuilder()
            .artifactPathPattern("[org]/[custom1<popo>]/[module]/[module]-[baseRev](-[custom2<momo>])")
            .descriptorPathPattern("[org](.[custom1<popo>])/[module]/[baseRev]/[module]-[custom2<momo>].[ext]")
            .distinctiveDescriptorPathPattern(true)
            .build();

    RepoLayout constantExtensionLayout = new RepoLayoutBuilder()
            .artifactPathPattern("[org]/[custom1<popo>]/[module]/[module]-[baseRev](-[custom2<momo>]).jar")
            .descriptorPathPattern("[org](.[custom1<popo>])/[module]/[baseRev]/[module]-[custom2<momo>].pom")
            .distinctiveDescriptorPathPattern(true)
            .build();

    public CustomModuleInfoUtilsTest() {
        super(new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[custom1<popo>]/[module]/[baseRev](-[custom2<momo>])")
                .descriptorPathPattern("[org](.[custom1<popo>])/[module]/[baseRev]/[custom2<momo>].[ext]")
                .distinctiveDescriptorPathPattern(true)
                .build()
        );
    }

    @Test
    public void testArtifactModuleToModule() throws Exception {
        testArtifactModuleToModule(repoLayout);
        testArtifactModuleToModule(otherLayout);
    }

    @Test
    public void testArtifactPathToModule() throws Exception {
        testArtifactPathToModule("bob/popo/mcbob/2.2.0-momo", new ModuleInfoBuilder().organization("bob").
                module("mcbob").baseRevision("2.2.0").customField("custom1", "popo").customField("custom2", "momo").
                build(), repoLayout);
        testArtifactPathToModule("jim/popo/bob/1.2.x", new ModuleInfoBuilder().organization("jim").module("bob").
                baseRevision("1.2.x").customField("custom1", "popo").build(), repoLayout);
        testArtifactPathToModule("bob/popo/mcbob/mcbob-2.2.0-momo", new ModuleInfoBuilder().organization("bob").
                module("mcbob").baseRevision("2.2.0").customField("custom1", "popo").customField("custom2", "momo").
                build(), otherLayout);
        testArtifactPathToModule("jim/popo/bob/bob-1.2.x", new ModuleInfoBuilder().organization("jim").module("bob").
                baseRevision("1.2.x").customField("custom1", "popo").build(), otherLayout);
    }

    @Test
    public void testDescriptorPathToModule() throws Exception {
        testDescriptorPathToModule("bob.popo/mcbob/2.2.0/momo.jar", new ModuleInfoBuilder().organization("bob").
                module("mcbob").baseRevision("2.2.0").ext("jar").customField("custom1", "popo").
                customField("custom2", "momo").build(), repoLayout);
        testDescriptorPathToModule("jim/bob/1.2.x/momo.so",
                new ModuleInfoBuilder().organization("jim").module("bob").
                        baseRevision("1.2.x").ext("so").customField("custom2", "momo").build(), repoLayout);
        testDescriptorPathToModule("bob.popo/mcbob/2.2.0/mcbob-momo.jar", new ModuleInfoBuilder().organization("bob").
                module("mcbob").baseRevision("2.2.0").ext("jar").customField("custom1", "popo").
                customField("custom2", "momo").build(), otherLayout);
        testDescriptorPathToModule("jim/bob/1.2.x/bob-momo.so",
                new ModuleInfoBuilder().organization("jim").module("bob").
                        baseRevision("1.2.x").ext("so").customField("custom2", "momo").build(), otherLayout);
    }

    @Test
    public void testDescriptorPathToModuleWithConstantExtension() throws Exception {
        testDescriptorPathToModule("jim.popo/bob/1.2.x/bob-momo.pom", new ModuleInfoBuilder().organization("jim")
                .module("bob").baseRevision("1.2.x").ext("pom").customField("custom1", "popo")
                .customField("custom2", "momo").build(), constantExtensionLayout);
    }

    @Test
    public void testCustom1() throws Exception {
        RepoLayout someLayout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[baseRev](-[folderItegRev])/[type]/[artifact<\\w+>]/[baseRev]" +
                        "(-[fileItegRev])(-[classifier]).[ext]").build();
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("net.sourceforge.basher").module("basher-booter").
                baseRevision("1.0.10").type("jar").ext("jar").customField("artifact", "myartifact").build();
        testArtifactModuleToModule(moduleInfo, someLayout);
        testArtifactPathToModule("net.sourceforge.basher/basher-booter/1.0.10/jar/myartifact/1.0.10.jar", moduleInfo,
                someLayout);
    }

    @Test
    public void testCustom2() throws Exception {
        RepoLayout someLayout = new RepoLayoutBuilder()
                .artifactPathPattern("[org]/[module]/[artifact<\\w+>]/[baseRev](-[folderItegRev])/[type]/[baseRev]" +
                        "(-[fileItegRev])(-[classifier]).[ext]").build();
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("net.sourceforge.basher").module("basher-booter").
                baseRevision("1.0.10").type("jar").ext("jar").customField("artifact", "myartifact").build();
        testArtifactModuleToModule(moduleInfo, someLayout);
        testArtifactPathToModule("net.sourceforge.basher/basher-booter/myartifact/1.0.10/jar/1.0.10.jar", moduleInfo,
                someLayout);
    }

    private void testArtifactModuleToModule(RepoLayout layoutToUse) throws Exception {
        ModuleInfo moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0")
                .customField("custom1", "popo").customField("custom2", "momo").build();
        testArtifactModuleToModule(moduleInfo, layoutToUse);

        moduleInfo = new ModuleInfoBuilder().organization("org.moo").module("bob").baseRevision("1.0")
                .customField("custom1", "popo").build();
        testArtifactModuleToModule(moduleInfo, layoutToUse);
    }
}
