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

package org.artifactory.webapp.wicket.util;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the ItemCssClass.
 *
 * @author Yossi Shaul
 */
@Test
public class CssClassTest extends ArtifactoryHomeBoundTest {

    public void pomCss() {
        String path = "/a/path/to/somewhere/my.pom";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "pom", "Expected pom css class");
    }

    public void jarCss() {
        String path = "my.jar";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "jar", "Expected jar css class");
    }

    public void rarCss() {
        String path = "my.jar";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "jar", "Expected jar css class");
    }

    public void zipAsDocCss() {
        String path = "a/path/to/my.zip";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "jar", "Expected jar css class");
    }

    public void parentCss() {
        String path = "..";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "parent", "Expected parent css class");
    }

    public void genericCss() {
        String path = "/none/of/the/standard.par";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "doc", "Expected doc css class");
    }

    public void genericCss2() {
        String path = "/none/of/the/standard.pom.tot";
        ItemCssClass cssClass = ItemCssClass.getFileCssClass(path);
        Assert.assertEquals(cssClass.getCssClass(), "doc", "Expected doc css class");
    }

}
