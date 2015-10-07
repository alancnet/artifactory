package org.artifactory.ui.rest.service.admin.configuration.repositories.util;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.descriptor.repo.RepoDescriptor;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;

/**
 * Reorders the list of repositories based on a list sent by the UI
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReorderRepositoriesService implements RestService<List<String>> {
    private static final Logger log = LoggerFactory.getLogger(ReorderRepositoriesService.class);

    private static final String LOCAL_TYPE = "local";
    private static final String REMOTE_TYPE = "remote";
    private static final String VIRTUAL_TYPE = "virtual";

    @Autowired
    private CentralConfigService configService;

    @Override
    @SuppressWarnings("unchecked") //yeah this code is disgusting, no time for something else
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String repoType = request.getPathParamByKey("repoType");
        log.debug("Processing reorder repos request for type {}", repoType);
        if (StringUtils.isBlank(repoType)) {
            response.error("The type of repositories to reorder must be specified").responseCode(SC_BAD_REQUEST);
        }
        List<String> newOrderRepoKeyList = request.getModels();
        if (CollectionUtils.isNullOrEmpty(newOrderRepoKeyList)) {
            response.error("No list to reorder by was sent.").responseCode(SC_BAD_REQUEST);
            return;
        }
        Map<String, ? extends RepoDescriptor> currentRepoMap;
        MutableCentralConfigDescriptor mutableDescriptor = configService.getMutableDescriptor();
        switch (repoType) {
            case LOCAL_TYPE:
                currentRepoMap = configService.getDescriptor().getLocalRepositoriesMap();
                LinkedHashMap<String, LocalRepoDescriptor> localMap = (LinkedHashMap<String, LocalRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (localMap.size() > 0) {
                    mutableDescriptor.setLocalRepositoriesMap(localMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case REMOTE_TYPE:
                currentRepoMap = configService.getDescriptor().getRemoteRepositoriesMap();
                LinkedHashMap<String, RemoteRepoDescriptor> remoteMap = (LinkedHashMap<String, RemoteRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (remoteMap.size() > 0) {
                    mutableDescriptor.setRemoteRepositoriesMap(remoteMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            case VIRTUAL_TYPE:
                currentRepoMap = configService.getDescriptor().getVirtualRepositoriesMap();
                LinkedHashMap<String, VirtualRepoDescriptor> virtualMap = (LinkedHashMap<String, VirtualRepoDescriptor>)
                        getNewOrderRepoMap(response, repoType, newOrderRepoKeyList, currentRepoMap);
                if (virtualMap.size() > 0) {
                    mutableDescriptor.setVirtualRepositoriesMap(virtualMap);
                    configService.saveEditedDescriptorAndReload(mutableDescriptor);
                }
                break;
            default:
                response.error("Invalid repository type given: " + repoType).responseCode(SC_BAD_REQUEST);
        }
    }

    private LinkedHashMap<String, ? extends RepoDescriptor> getNewOrderRepoMap(RestResponse<? extends Object> response,
            String repoType,
            List<String> newOrderRepoKeyList, Map<String, ? extends RepoDescriptor> currentRepoMap) {
        LinkedHashMap<String, RepoDescriptor> newRepoMap = Maps.newLinkedHashMap();
        if (isLegalReorder(response, repoType, newOrderRepoKeyList, currentRepoMap)) {
            for (String repoKey : newOrderRepoKeyList) {
                newRepoMap.put(repoKey, currentRepoMap.get(repoKey));
            }
            log.info("Reordering {} repositories list.", repoType);
        }
        return newRepoMap;
    }

    private boolean isLegalReorder(RestResponse response, String repoType, List<String> newOrderRepoKeyList,
            Map<String, ? extends RepoDescriptor> currentRepoMap) {
        Set<String> oldOrderRepoKeys = currentRepoMap.keySet();
        if (oldOrderRepoKeys.size() != newOrderRepoKeyList.size()) {
            log.debug("Current {} repo map size: {}", repoType, oldOrderRepoKeys.size());
            log.debug("New Order list size : {}", newOrderRepoKeyList.size());
            response.error("The size of the list to order by does not match the size of the current repo list, " +
                    "aborting.").responseCode(SC_BAD_REQUEST);
            return false;
        } else if (!newOrderRepoKeyList.containsAll(oldOrderRepoKeys)) {
            response.error("The new order list is missing \\ has excess repositories that are currently saved." +
                    repoType).responseCode(SC_BAD_REQUEST);
            return false;
        }
        return true;
    }
}