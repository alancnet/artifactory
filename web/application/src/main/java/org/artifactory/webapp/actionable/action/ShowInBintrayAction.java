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

package org.artifactory.webapp.actionable.action;

import org.artifactory.api.bintray.BintrayItemInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.webapp.actionable.ActionableItem;
import org.artifactory.webapp.actionable.event.RepoAwareItemEvent;

/**
 * Link to the item path at Bintray
 *
 * @author Gidi Shabat
 */
public class ShowInBintrayAction extends RepoAwareItemAction {
    public static final String ACTION_NAME = "Show in Bintray";
    private BintrayItemInfo searchResult;

    public ShowInBintrayAction(BintrayItemInfo searchResult) {
        super(ACTION_NAME);
        this.searchResult = searchResult;
    }

    @Override
    public void onAction(RepoAwareItemEvent e) {

    }

    @Override
    public String getActionLinkURL(ActionableItem actionableItem) {
        String bintrayUrl = ConstantValues.bintrayUrl.getString();
        StringBuilder builder = new StringBuilder();
        builder.append(bintrayUrl).append("/");
        builder.append("version").append("/");
        builder.append("show").append("/");
        builder.append("general").append("/");
        builder.append(searchResult.getOwner()).append("/");
        builder.append(searchResult.getRepo()).append("/");
        builder.append(searchResult.getPackage()).append("/");
        builder.append(searchResult.getVersion()).append("/");
        return builder.toString();
    }
}