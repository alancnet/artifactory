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

package org.artifactory.common.wicket.component.file.path;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.autocomplete.ImprovedAutoCompleteTextField;

import java.io.File;
import java.util.Iterator;

@SuppressWarnings({"UnusedDeclaration"})
public class PathAutoCompleteTextField extends ImprovedAutoCompleteTextField<File> {
    private PathMask mask = PathMask.ALL;
    private PathHelper pathHelper;
    private PathAutoCompleteConverter converter;

    public PathAutoCompleteTextField(String id) {
        this(id, null, new PathHelper());
    }

    public PathAutoCompleteTextField(String id, IModel<File> model) {
        this(id, model, new PathHelper());
    }

    public PathAutoCompleteTextField(String id, String root) {
        this(id, null, root);
    }

    public PathAutoCompleteTextField(String id, IModel<File> model, String root) {
        this(id, model, new PathHelper(root));
    }

    protected PathAutoCompleteTextField(String id, IModel<File> model, PathHelper pathHelper) {
        super(id, model, File.class, new PathAutoCompleteRenderer(pathHelper), DEFAULT_SETTINGS);

        this.pathHelper = pathHelper;
        converter = new PathAutoCompleteConverter(pathHelper);

        add(new CssClass("pathAutoComplete"));
        add(new AttributeModifier("autoCompleteCssClass", "wicket-aa pathAutoComplete_menu"));
    }

    public PathMask getMask() {
        return mask;
    }

    public void setMask(PathMask mask) {
        this.mask = mask;
    }

    public String getRoot() {
        return pathHelper.getWorkingDirectoryPath();
    }

    public void setRoot(String root) {
        pathHelper.setWorkingDirectoryPath(root);
    }

    @Override
    protected Iterator<File> getChoices(String input) {
        return pathHelper.getFiles(input, mask).iterator();
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public IConverter getConverter(Class type) {
        return converter;
    }
}
