package org.artifactory.rest.common;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.ContainerResponse;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.FooterMessage;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.api.security.UserGroupService;
import org.artifactory.api.storage.StorageQuotaInfo;
import org.artifactory.storage.StorageService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.artifactory.addon.FooterMessage.FooterMessageVisibility.admin;
import static org.artifactory.addon.FooterMessage.FooterMessageVisibility.isVisible;

/**
 * @author Gidi Shabat
 */
public class GlobalMessageProvider {
    private static final Logger log = LoggerFactory.getLogger(GlobalMessageProvider.class);
    private long lastUpdateTime = 0;
    private List<FooterMessage> cache=Lists.newArrayList();
    private volatile ReentrantLock lock = new ReentrantLock();

    public void decorateWithGlobalMessages(ContainerResponse response, AddonsManager addonsManager,
            StorageService storageService,AuthorizationService authenticationService) {
        try {
            boolean admin=authenticationService.isAdmin();
            boolean notAnonymous = !authenticationService.isAnonymous();
            // Try to update the cache if needed
            triggerCacheUpdateProcessIfNeeded(addonsManager, storageService);
            // update response header with message in cache
            ObjectMapper mapper = new ObjectMapper();
            Predicate<FooterMessage> predicate = p -> isVisible(p.getVisibility(), admin, notAnonymous);
            List<FooterMessage> collect = cache.stream().filter(predicate).collect(Collectors.toList());
            String json = mapper.writeValueAsString(collect);
            response.getResponse().getMetadata().add("Artifactory-UI-messages", json);
        } catch (Exception e) {
            log.error("Fail to attache global message to response header", e);
        }
    }

    private void triggerCacheUpdateProcessIfNeeded(AddonsManager addonsManager, StorageService storageService) {
        long currentTime = System.currentTimeMillis();
        // update the cache every 5 seconds
        if (currentTime - lastUpdateTime > TimeUnit.SECONDS.toMillis(5)) {
            // Only one thread is allowed to update the cache
            // all the other requests will use the old cache value
            boolean acquireLock = lock.tryLock();
            try {
                if (acquireLock) {
                    List<FooterMessage> list = Lists.newArrayList();
                    decorateHeadersWithLicenseNotInstaled(list, addonsManager);
                    decorateHeaderWithQuotaMessage(list, storageService);
                    // update the cache and the last update time
                    lastUpdateTime=currentTime;
                    cache = list;
                }
            } finally {
                if (acquireLock) {
                    lock.unlock();
                }
            }
        }
    }

    private void decorateHeadersWithLicenseNotInstaled(List<FooterMessage> list, AddonsManager addonsManager) {
        FooterMessage licenseMessage = addonsManager.getLicenseFooterMessage();
        if (licenseMessage != null) {
            list.add(licenseMessage);
        }
    }

    private void decorateHeaderWithQuotaMessage(List<FooterMessage> list, StorageService storageService) {
        StorageQuotaInfo storageQuotaInfo = storageService.getStorageQuotaInfo(0);
        if (storageQuotaInfo != null) {
            boolean limitReached = storageQuotaInfo.isLimitReached();
            boolean warningReached = storageQuotaInfo.isWarningLimitReached();
            if (limitReached) {
                String errorMessage = storageQuotaInfo.getErrorMessage();
                list.add(FooterMessage.createError(errorMessage, admin));
            }else if (warningReached) {
                String warningMessage = storageQuotaInfo.getWarningMessage();
                list.add(FooterMessage.createWarning(warningMessage, admin));
            }
        }
    }

}
