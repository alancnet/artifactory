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

package org.artifactory.build;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.artifact.PromotionResult;
import org.artifactory.common.StatusEntry;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.util.DoesNotExistException;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.builder.PromotionStatusBuilder;
import org.jfrog.build.api.release.Promotion;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Noam Y. Tenne
 */
public class BuildPromotionHelper extends BaseBuildPromoter {
    private static final Logger log = LoggerFactory.getLogger(BuildPromotionHelper.class);

    public PromotionResult promoteBuild(BuildRun buildRun, Promotion promotion) {
        Build build = getBuild(buildRun);

        if (build == null) {
            throw new DoesNotExistException("Unable to find build '" + buildRun.getName() + "' #" +
                    buildRun.getNumber() + ".");
        }

        PromotionResult promotionResult = new PromotionResult();

        String targetRepo = promotion.getTargetRepo();
        BasicStatusHolder statusHolder = new BasicStatusHolder();

        Set<RepoPath> itemsToMove = null;
        if (StringUtils.isBlank(targetRepo)) {
            statusHolder.status("Skipping build item relocation: no target repository selected.", log);
        } else {
            assertRepoExists(targetRepo);
            itemsToMove = collectItems(build, promotion, statusHolder);

            promoteBuildItems(promotion, statusHolder, itemsToMove);
        }

        /*
        *  A list of properties to attach to the build's artifacts (regardless if "targetRepo" is used).
        * */
        Properties properties = (Properties) InfoFactoryHolder.get().createProperties();
        Map<String, Collection<String>> promotionProperties = promotion.getProperties();
        if ((promotionProperties != null) && !promotionProperties.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : promotionProperties.entrySet()) {
                properties.putAll(entry.getKey(), entry.getValue());
            }
        }
        if (!properties.isEmpty()) {
            Set<RepoPath> modifiedItems = collectTheRightItems(promotion, build, targetRepo, statusHolder,
                    itemsToMove);

            if (!modifiedItems.isEmpty()) {
                tagBuildItemsWithProperties(modifiedItems, properties, promotion.isFailFast(), promotion.isDryRun(),
                        statusHolder);
            }
        }

        performPromotionIfNeeded(statusHolder, build, promotion);
        appendMessages(promotionResult, statusHolder);

        return promotionResult;
    }

    private Set<RepoPath> collectTheRightItems(Promotion promotion, Build build, String targetRepo,
            BasicStatusHolder statusHolder, Set<RepoPath> itemsToMove) {
        //Collect artifacts only from the target repository
        Set<RepoPath> modifiedItems = Sets.newHashSet();
        if (!StringUtils.isBlank(targetRepo)) {
            if (itemsToMove != null) {
                for (RepoPath item : itemsToMove) {
                    modifiedItems.add(new RepoPathImpl(targetRepo, item.getPath()));
                }
            }
        } else {
            //In case the target repository is not defined, collect the items form the source.
            modifiedItems = collectItems(build, promotion, statusHolder);
        }
        return modifiedItems;
    }

    private void performPromotionIfNeeded(BasicStatusHolder statusHolder, Build build, Promotion promotion) {
        String status = promotion.getStatus();

        if (statusHolder.hasErrors() || statusHolder.hasWarnings()) {
            statusHolder.status("Skipping promotion status update: item promotion was completed with errors " +
                    "and warnings.", log);
            return;
        }

        if (StringUtils.isBlank(status)) {
            statusHolder.status("Skipping promotion status update: no status received.", log);
            return;
        }

        PromotionStatusBuilder statusBuilder = new PromotionStatusBuilder(status).
                user(authorizationService.currentUsername()).repository(promotion.getTargetRepo()).
                comment(promotion.getComment()).ciUser(promotion.getCiUser());

        String timestamp = promotion.getTimestamp();

        if (StringUtils.isNotBlank(timestamp)) {
            try {
                ISODateTimeFormat.dateTime().parseMillis(timestamp);
            } catch (Exception e) {
                statusHolder.error("Skipping promotion status update: invalid\\unparsable timestamp " + timestamp +
                        ".", log);
                return;
            }
            statusBuilder.timestamp(timestamp);
        } else {
            statusBuilder.timestampDate(new Date());
        }

        if (promotion.isDryRun()) {
            return;
        }

        buildService.addPromotionStatus(build, statusBuilder.build());
        log.info("Promotion completed successfully for build name '{}' and number '{}' with status of '{}'",
                build.getName(), build.getNumber(), status);
    }

    private void promoteBuildItems(Promotion promotion, BasicStatusHolder status, Set<RepoPath> itemsToMove) {
        String targetRepo = promotion.getTargetRepo();
        boolean dryRun = promotion.isDryRun();
        if (!itemsToMove.isEmpty()) {
            if (promotion.isCopy()) {
                try {
                    status.merge(copy(itemsToMove, targetRepo, dryRun, promotion.isFailFast()));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    status.error("Error occurred while copying: " + e.getMessage(), e, log);
                }
            } else {
                try {
                    status.merge(move(itemsToMove, targetRepo, dryRun, promotion.isFailFast()));
                } catch (Exception e) {
                    status.error("Error occurred while moving: " + e.getMessage(), e, log);
                }
            }
        }
    }

    private void appendMessages(PromotionResult promotionResult, BasicStatusHolder statusHolder) {
        for (StatusEntry statusEntry : statusHolder.getEntries()) {
            promotionResult.messages.add(new PromotionResult.PromotionResultMessages(statusEntry));
        }
    }
}
