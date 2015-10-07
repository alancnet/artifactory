/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
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
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.autocomplete.ImprovedAutoCompleteTextField;

import java.util.Iterator;

/**
 * Auto completion text field that when provided with a repository key (using {@code setRepoKey}) provides suggestions
 * from the underlying repository paths.
 *
 * TODO can be merged with {@link org.artifactory.common.wicket.component.file.path.PathAutoCompleteTextField}
 * @author Yoav Luft
 */
public class RepoPathAutoCompleteTextField extends ImprovedAutoCompleteTextField<String> {

    private PathMask mask = PathMask.ALL;
    private RepoPathHelper pathHelper;

    private RepoPathAutoCompleteConverter converter;

    public RepoPathAutoCompleteTextField(String id, RepositoryService repositoryService) {
        this(id, null, new RepoPathHelper(repositoryService, null));
    }

    protected RepoPathAutoCompleteTextField(String id, IModel<String> model, RepoPathHelper pathHelper) {
        super(id, model, String.class, new RepoPathAutoCompleteRenderer(), DEFAULT_SETTINGS);

        this.pathHelper = pathHelper;
        converter = new RepoPathAutoCompleteConverter(pathHelper);

        add(new CssClass("pathAutoComplete"));
        add(new AttributeModifier("autoCompleteCssClass", "wicket-aa pathAutoComplete_menu"));
    }

    public PathMask getMask() {
        return mask;
    }

    public void setMask(PathMask mask) {
        this.mask = mask;
    }

    @Override
    protected Iterator<String> getChoices(String input) {
        return pathHelper.getPaths(input, mask).iterator();
    }

    @SuppressWarnings({"RefusedBequest"})
    @Override
    public IConverter getConverter(Class type) {
        return converter;
    }

    public String getRepoKey() {
        return pathHelper.getRepoKey();
    }

    public void setRepoKey(String repoKey) {
        pathHelper.setRepoKey(repoKey);
    }
}
