package org.artifactory.ui.rest.service.artifacts.browse.generic;

import com.google.common.collect.Lists;
import com.sun.jersey.api.NotFoundException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.CoreAddons;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.*;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.storage.StorageUnit;
import org.artifactory.common.ConstantValues;
import org.artifactory.md.Properties;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.utils.BrowseUtils;
import org.artifactory.webapp.servlet.HttpArtifactoryResponse;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

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
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BrowseNativeService implements RestService<String> {
    private static final Logger log = LoggerFactory.getLogger(BrowseNativeService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd-MMM-yyyy HH:mm")
            .withLocale(Locale.ENGLISH);
    private List<? extends BaseBrowsableItem> items;
    private int columnSize;
    private boolean printParent;
    public static final String REALM = "Artifactory Realm";
    public static final String ATTR_ARTIFACTORY_REPOSITORY_PATH = "artifactory.repository_path";
    public static final String ATTR_ARTIFACTORY_REQUEST_PROPERTIES = "artifactory.request_properties";
    public String head;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RepositoryBrowsingService repoBrowsingService;
    @Autowired
    private AuthorizationService authorizationService;
    @Autowired
    CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest<String> request, RestResponse response) {
        try {
            // get Html native browsing page
            String page = buildPage(request, response);
            // update response
            response.iModel(page);
        }
        catch (Exception e){
            response.error(e.getMessage()).responseCode(HttpStatus.SC_NOT_FOUND);
            log.debug(e.getMessage() + " - " + HttpStatus.SC_NOT_FOUND);
        }
    }

    /**
     * build browsing native page with data
     *
     * @param artifactoryRequest  - encapsulate data related to request
     * @param artifactoryResponse - encapsulate data related to response
     * @return - native browsing html page
     */
    private String buildPage(ArtifactoryRestRequest artifactoryRequest, RestResponse artifactoryResponse) {
        StringBuilder page = new StringBuilder();
        try {
            HttpServletRequest httpServletRequest = artifactoryRequest.getServletRequest();
            RepoPath repoPath = (RepoPath) httpServletRequest.getAttribute(ATTR_ARTIFACTORY_REPOSITORY_PATH);
            if (repoPath == null || StringUtils.isEmpty(repoPath.getRepoKey())) {
                throw new NotFoundException("Repository Path Not Found");
            }
            // fetch page props
            Properties requestProps = (Properties) httpServletRequest
                    .getAttribute(ATTR_ARTIFACTORY_REQUEST_PROPERTIES);
            updatePageData(repoPath, requestProps, artifactoryResponse);
            // fetch page title
            String title = getPageTitle(repoPath);
            // fetch page body
            String body = getBody();
            // fetch page footer
            String addressFooter = getAddressFooter(httpServletRequest);
            // create html page
            createHtmlPage(page, title, body, addressFooter);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return page.toString();
    }

    /**
     * create html page and related tags
     *
     * @param page          - page string builder
     * @param title         - page title
     * @param body          - page body
     * @param addressFooter - page footer
     */
    private void createHtmlPage(StringBuilder page, String title, String body, String addressFooter) {
        page.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
        page.append("<html>\n");
        page.append("<head>");
        page.append("<title>" + title + "</title>\n");
        page.append("</head>\n");
        page.append("<body>\n");
        page.append("<h1>" + title + "</h1>\n");
        page.append("<pre>" + head + "</pre>");
        page.append("<hr/>\n" + "<pre>");
        page.append(body);
        page.append("</pre>\n" + "<hr/>");
        page.append("<address style=\"font-size:small;\">").append(addressFooter).append("</address>");
        page.append("</body>");
        page.append("</html>");
    }

    /**
     * get path title
     * @param repoPath - repository path
     * @return - native browsing page title
     */
    private String getPageTitle(RepoPath repoPath) {
        StringBuilder titleBuilder = new StringBuilder(String.format("Index of %s/%s", repoPath.getRepoKey(),
                StringEscapeUtils.escapeXml(repoPath.getPath())));
        return titleBuilder.toString();
    }

    /**
     * update page related data
     * @param repoPath - repository path
     * @param requestProps - request related props
     * @param artifactoryResponse - encapsulate data related response
     * @throws IOException
     */
    private void updatePageData(final RepoPath repoPath, Properties requestProps, RestResponse artifactoryResponse)
            throws IOException {
        RootNodesFilterResult rootNodesFilterResult = new RootNodesFilterResult();
        final List<? extends BaseBrowsableItem> items = getItems(repoPath, requestProps, rootNodesFilterResult);
        sendChallengeMessage(repoPath, rootNodesFilterResult, items, artifactoryResponse.getServletResponse());
        // get max name length
        int maxLength = 4;
        for (BaseBrowsableItem baseBrowsableItem : items) {
            maxLength = Math.max(baseBrowsableItem.getName().length(), maxLength);
        }
        // set table head
        setTableHead(maxLength);
        // items
        this.printParent = StringUtils.isNotEmpty(repoPath.getPath());
        this.items = items;
        this.columnSize = maxLength + 2;
    }

    /**
     * send Challenge Message To Anonymous If List Node is empty due to Missing Permission
     *
     * @param repoPath              - repo path
     * @param rootNodesFilterResult - object hold empty list canRead flag
     * @param items                 - list of Items
     * @throws IOException
     */
    private void sendChallengeMessage(RepoPath repoPath,
                                      RootNodesFilterResult rootNodesFilterResult,
                                      List<? extends BaseBrowsableItem> items,
                                      HttpServletResponse response) throws IOException {
        ContextHelper.get().beanForType(BasicAuthenticationEntryPoint.class);
        if (isItemListEmptyDueAnonymousMissingPermission(rootNodesFilterResult, items)) {
            ArtifactoryResponse artifactoryResponse = new HttpArtifactoryResponse(response);
            artifactoryResponse.sendAuthorizationRequired("Browse request for repo:path " + repoPath + " is forbidden " +
                    "for user 'anonymous'.", REALM);
        }
    }

    /**
     * @param rootNodesFilterResult - object hold List canRead flag
     * @param items                 list of items
     * @return true is list is empty due to Anonymous Missing Permission to read
     */
    private boolean isItemListEmptyDueAnonymousMissingPermission(RootNodesFilterResult rootNodesFilterResult,
                                                                 List<? extends BaseBrowsableItem> items) {
        return items.isEmpty() && !rootNodesFilterResult.isAllItemNodesCanRead();
    }

    /**
     * get page head data
     *
     * @param maxLength - max node name length
     * @return - page head
     */
    private void setTableHead(int maxLength) {
        final StringBuilder head = new StringBuilder();
        head.append(rightPad("Name", maxLength + 2));
        head.append("Last modified      Size");
        this.head = head.toString();
    }

    /**
     * get addresses footer
     *
     * @param request - http servlet request
     * @return - address footer
     */
    private String getAddressFooter(HttpServletRequest request) {
        StringBuilder addressBuilder = new StringBuilder();
        String versionInfo = getListBrowsingVersion();
        String serverName = request.getServerName();
        addressBuilder.append(String.format("%s Server at %s Port %s", versionInfo, serverName, request.getServerPort()));
        return addressBuilder.toString();
    }

    /**
     * get List browsing versions
     *
     * @return - list browsing versions
     */
    public String getListBrowsingVersion() {
        CoreAddons addon =  ContextHelper.get().beanForType(AddonsManager.class).addonByType(CoreAddons.class);
        return addon.getListBrowsingVersion();
    }

    /**
     * sort retrieved browsable item from repository
     *
     * @param repoPath              - repository path
     * @param requestProps          - request properties
     * @param rootNodesFilterResult - object hold the List node acceptance status flag,if flagged to false meaning
     *                              at least one node has read permission issue by user
     * @return list of sorted browsable item
     */
    private List<? extends BaseBrowsableItem> getItems(RepoPath repoPath, Properties requestProps,
                                                       RootNodesFilterResult rootNodesFilterResult) {
        boolean updateRootNodesFilterFlag = authorizationService.isAnonymous();
        List<? extends BaseBrowsableItem> items;
        try {
            items = getBrowsableItemsList(repoPath, requestProps, rootNodesFilterResult,
                    updateRootNodesFilterFlag);
        } catch (Exception e) {
            throw new RuntimeException("not found");
        }
        Collections.sort(items, new ItemInfoComparator());
        return items;
    }

    /**
     * get Browsable Item from repositories (local/remote/virtual) based on search criteria
     *
     * @param repoPath                  - repository path
     * @param requestProps              - properties of the request
     * @param rootNodesFilterResult     - object hold the List node acceptance status flag,if flagged to false meaning
     *                                  at least one node has read permission issue by user
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
            items = repoBrowsingService.getRemoteRepoBrowsableChildren(criteria, updateRootNodesFilterFlag,
                    rootNodesFilterResult);
        } else if (repositoryService.localOrCachedRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
            items = repoBrowsingService.getLocalRepoBrowsableChildren(criteria, updateRootNodesFilterFlag,
                    rootNodesFilterResult);
        } else if (repositoryService.virtualRepoDescriptorByKey(repoPath.getRepoKey()) != null) {
            items = repoBrowsingService.getVirtualRepoBrowsableChildren(criteria, updateRootNodesFilterFlag,
                    rootNodesFilterResult);
        }
        if (!includeChecksums) {
            items = BrowseUtils.filterChecksums(items);
        }
        return items;
    }

    /**
     * get html page nodes body
     *
     * @return - html body
     */
    public String getBody() {
        StringBuilder bodyBuilder = new StringBuilder();
        if (printParent) {
            bodyBuilder.append("<a href=\"../\">../</a>\n");

        } else if (items.isEmpty()) {
            bodyBuilder.append("No items found.\n");
            return bodyBuilder.toString();
        }

        for (BaseBrowsableItem item : items) {
            String name = item.getName();
            bodyBuilder.append("<a href=\"");
            bodyBuilder.append(name);
            if (item.isFolder()) {
                bodyBuilder.append("/");
            }
            bodyBuilder.append("\">");
            bodyBuilder.append(StringEscapeUtils.escapeXml(name));
            if (item.isFolder()) {
                bodyBuilder.append("/");
            }
            bodyBuilder.append("</a>");
            if (item.isRemote()) {
                bodyBuilder.append("->");
            }
            bodyBuilder.append(StringUtils.repeat(" ", columnSize - name.length()));
            if (!item.isFolder()) {
                bodyBuilder.append(" ");
            }
            if (item.getLastModified() > 0) {
                bodyBuilder.append(DATE_FORMAT.print(item.getLastModified()));
            } else {
                bodyBuilder.append("  -");
            }
            bodyBuilder.append("  ");

            long size = item.getSize();
            if (item.isFolder() || size <= 0) {
                bodyBuilder.append("  -");
            } else {
                bodyBuilder.append(StorageUnit.toReadableString(size));
            }
            bodyBuilder.append("\n");
        }
        return bodyBuilder.toString();
    }

    /**
     * item comparator for sorting
     */
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

