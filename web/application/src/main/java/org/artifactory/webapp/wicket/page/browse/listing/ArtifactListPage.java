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
package org.artifactory.webapp.wicket.page.browse.listing;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.repo.RootNodesFilterResult;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.util.WicketUtils;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.artifactory.webapp.servlet.RepoFilter;
import org.artifactory.webapp.servlet.authentication.ArtifactoryBasicAuthenticationEntryPoint;
import org.artifactory.webapp.wicket.page.browse.BrowseUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.StringUtils.rightPad;

/**
 * @author Yoav Aharoni
 */
public class ArtifactListPage extends WebPage {
    public static final String PATH = "_list";
    @SpringBean
    private RepositoryService repositoryService;
    @SpringBean
    private RepositoryBrowsingService repoBrowsingService;
    @SpringBean
    private AuthorizationService authorizationService;
    @SpringBean
    private AddonsManager addonsManager;

    public ArtifactListPage() throws IOException {
        setStatelessHint(true);
        setVersioned(false);

        //Retrieve the repository path from the request
        HttpServletRequest httpServletRequest = WicketUtils.getHttpServletRequest();
        RepoPath repoPath = (RepoPath) httpServletRequest.getAttribute(RepoFilter.ATTR_ARTIFACTORY_REPOSITORY_PATH);
        if (repoPath == null || StringUtils.isEmpty(repoPath.getRepoKey())) {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND);
        }

        addTitle(repoPath);
        Properties requestProps = (Properties) httpServletRequest
                .getAttribute(RepoFilter.ATTR_ARTIFACTORY_REQUEST_PROPERTIES);
        addFileList(repoPath, requestProps);
        addAddress(httpServletRequest);
    }

    private void addTitle(RepoPath repoPath) {
        final String title = String.format("Index of %s/%s", repoPath.getRepoKey(),
                StringEscapeUtils.escapeXml(repoPath.getPath()));
        Label titleLabel = new Label("title", title);
        titleLabel.setEscapeModelStrings(false); // Prevent double escaping
        add(titleLabel);
        add(new Label("pageTitle", title));
    }

    private void addFileList(final RepoPath repoPath, Properties requestProps)
            throws IOException {
        RootNodesFilterResult rootNodesFilterResult= new RootNodesFilterResult();
        final List<? extends BaseBrowsableItem> items = getItems(repoPath, requestProps,rootNodesFilterResult);
        sendChallengeMessageIfListNodeIsEmptyDueToAnonymousPermissionIssue(repoPath, rootNodesFilterResult, items);
             // get max name length
            int maxLength = 4;
            for (BaseBrowsableItem baseBrowsableItem : items) {
                maxLength = Math.max(baseBrowsableItem.getName().length(), maxLength);
            }

            // print head
            addTableHead(maxLength);

            // items
            final boolean printParent = StringUtils.isNotEmpty(repoPath.getPath());
            add(new ItemsListView(items, maxLength + 2, printParent));
    }

    /**
     * send Challenge Message To Anonymous If List Node is empty due to Missing Permission
     * @param repoPath - repo path
     * @param rootNodesFilterResult - object hold empty list canRead flag
     * @param items - list of Items
     * @throws IOException
     */
    private void sendChallengeMessageIfListNodeIsEmptyDueToAnonymousPermissionIssue(RepoPath repoPath,
            RootNodesFilterResult rootNodesFilterResult, List<? extends BaseBrowsableItem> items)
            throws IOException {
            ArtifactoryBasicAuthenticationEntryPoint authenticationEntryPoint=(ArtifactoryBasicAuthenticationEntryPoint)
                    ContextHelper.get().beanForType(BasicAuthenticationEntryPoint.class);
        if (isItemListEmptyDueAnonymousMissingPermission(rootNodesFilterResult, items)){
            HttpServletResponse servletResponse = WicketUtils.getHttpServletResponse();
            ArtifactoryResponse artifactoryResponse = new HttpArtifactoryResponse(servletResponse);
            artifactoryResponse.sendAuthorizationRequired("Browse request for repo:path " +repoPath+" is forbidden " +
                    "for user 'anonymous'.",authenticationEntryPoint.getRealmName());
         }
    }

    /**
     *
     * @param rootNodesFilterResult - object hold List canRead flag
     * @param items list of items
     * @return true is list is empty due to Anonymous Missing Permission to read
     */
    private boolean isItemListEmptyDueAnonymousMissingPermission(RootNodesFilterResult rootNodesFilterResult,
            List<? extends BaseBrowsableItem> items) {
        return items.isEmpty() && !rootNodesFilterResult.isAllItemNodesCanRead();
    }

    private void addTableHead(int maxLength) {
        final StringBuilder head = new StringBuilder();
        head.append(rightPad("Name", maxLength + 2));
        head.append("Last modified      Size");
        add(new Label("head", head.toString()).setEscapeModelStrings(false));
    }

    private void addAddress(HttpServletRequest request) {
        final String version = addonsManager.addonByType(CoreAddons.class).getListBrowsingVersion();
        final String serverName = request.getServerName();
        String address = String.format("%s Server at %s Port %s", version, serverName, request.getServerPort());
        add(new Label("address", address));
    }

    /**
     * sort retrieved browsable item from repository
     * @param repoPath - repository path
     * @param requestProps - request properties
     * @param rootNodesFilterResult - object hold the List node acceptance status flag,if flagged to false meaning
     *                                    at least one node has read permission issue by user
     * @return list of sorted browsable item
     */
    private List<? extends BaseBrowsableItem> getItems(RepoPath repoPath, Properties requestProps,
            RootNodesFilterResult rootNodesFilterResult) {
        boolean updateRootNodesFilterFlag = authorizationService.isAnonymous();
        List<? extends BaseBrowsableItem> items;
        try {
            items = getBrowsableItemsList(repoPath, requestProps,rootNodesFilterResult,
                    updateRootNodesFilterFlag);
        } catch (Exception e) {
            throw new AbortWithHttpErrorCodeException(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        Collections.sort(items, new ItemInfoComparator());
        return items;
    }

    /**
     * get Browsable Item from repositories (local/remote/virtual) based on search criteria
     * @param repoPath - repository path
     * @param requestProps - properties of the request
     * @param rootNodesFilterResult - object hold the List node acceptance status flag,if flagged to false meaning
     *                                    at least one node has read permission issue by user
     * @param updateRootNodesFilterFlag - if true keep the acceptance status flag
     * @return
     */
    private List<? extends BaseBrowsableItem> getBrowsableItemsList(RepoPath repoPath, Properties requestProps,
            RootNodesFilterResult rootNodesFilterResult, boolean updateRootNodesFilterFlag) {
        List<BaseBrowsableItem> items = Lists.newArrayList();
        BrowsableItemCriteria.Builder builder = new BrowsableItemCriteria.Builder(repoPath).
                requestProperties(requestProps);
        boolean includeChecksums = !ConstantValues.uiHideChecksums.getBoolean();
        builder.includeChecksums(includeChecksums);
        BrowsableItemCriteria criteria = builder.build();
        if (repositoryService.remoteRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
           items = repoBrowsingService.getRemoteRepoBrowsableChildren(criteria,updateRootNodesFilterFlag,
                   rootNodesFilterResult);
       } else if (repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
           items = repoBrowsingService.getLocalRepoBrowsableChildren(criteria,updateRootNodesFilterFlag,
                   rootNodesFilterResult);
       } else if (repositoryService.virtualRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
           items = repoBrowsingService.getVirtualRepoBrowsableChildren(criteria,updateRootNodesFilterFlag,
                   rootNodesFilterResult);
       }
        if (!includeChecksums) {
            items = BrowseUtils.filterChecksums(items);
        }
        return items;
    }

    private static class ItemsListView extends WebComponent {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm")
                .withLocale(Locale.ENGLISH);
        private final List<? extends BaseBrowsableItem> items;
        private final int columnSize;
        private final boolean printParent;

        public ItemsListView(List<? extends BaseBrowsableItem> items, int columnSize, boolean printParent) {
            super("list");
            setEscapeModelStrings(false);
            this.items = items;
            this.columnSize = columnSize;
            this.printParent = printParent;
         }

        @Override
        public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
            final Response response = getResponse();
                 if (printParent) {
                    response.write("<a href=\"../\">../</a>\n");

                } else if (items.isEmpty()) {
                    response.write("No items found.\n");
                    return;
                }

                for (BaseBrowsableItem item : items) {
                    String name = item.getName();
                    response.write("<a href=\"");
                    response.write(name);
                    if (item.isFolder()) {
                        response.write("/");
                    }
                    response.write("\">");
                    response.write(StringEscapeUtils.escapeXml(name));
                    if (item.isFolder()) {
                        response.write("/");
                    }
                    response.write("</a>");
                    if (item.isRemote()) {
                        response.write("->");
                    }
                    response.write(StringUtils.repeat(" ", columnSize - name.length()));
                    if (item.getLastModified() > 0) {
                        response.write(DATE_FORMAT.print(item.getLastModified()));
                    } else {
                        response.write("  -");
                    }
                    response.write("  ");

                    long size = item.getSize();
                    if (item.isFolder() || size <= 0) {
                        response.write("  -");
                    } else {
                        response.write(StorageUnit.toReadableString(size));
                    }
                    response.write("\n");
                }
            }
    }

    private static class ItemInfoComparator implements Comparator<BaseBrowsableItem>, Serializable {
        @Override
        public int compare(BaseBrowsableItem o1, BaseBrowsableItem o2) {
            final int folderCmp = Boolean.valueOf(o2.isFolder()).compareTo(o1.isFolder());
            if (folderCmp != 0) {
                return folderCmp;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
