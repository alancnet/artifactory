package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoUtils;
import org.artifactory.api.module.regex.NamedMatcher;
import org.artifactory.api.module.regex.NamedPattern;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.configuration.layouts.LayoutConfigViewModel;
import org.artifactory.util.ExceptionUtils;
import org.artifactory.util.RepoLayoutUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class TestArtPathService implements RestService<LayoutConfigViewModel> {
    private static final Logger log = LoggerFactory.getLogger(TestArtPathService.class);

    @Override
    public void execute(ArtifactoryRestRequest<LayoutConfigViewModel> request, RestResponse response) {
        LayoutConfigViewModel layout = request.getImodel();
        try {
            ModuleInfo moduleInfo = null;
            if (layout.isDistinctiveDescriptorPathPattern()) {
                String pathPattern = layout.getDescriptorPathPattern();
                String regExp = RepoLayoutUtils.generateRegExpFromPattern(layout, pathPattern, true);
                moduleInfo = ModuleInfoUtils.moduleInfoFromDescriptorPath(layout.getPathToTest(), layout);
                checkIfEmptyCapturingGroup(moduleInfo, regExp, layout.getPathToTest());
            }

            if ((moduleInfo == null) || !moduleInfo.isValid()) {
                String pathPattern = layout.getArtifactPathPattern();
                String regExp = RepoLayoutUtils.generateRegExpFromPattern(layout, pathPattern, true);
                moduleInfo = ModuleInfoUtils.moduleInfoFromArtifactPath(layout.getPathToTest(), layout);
                checkIfEmptyCapturingGroup(moduleInfo, regExp, layout.getPathToTest());
            }

            response.iModel(moduleInfo);

        } catch (Exception e) {
            String message = "Failed to test path: " + ExceptionUtils.getRootCause(e).getMessage();
            response.error(message);
            log.debug(message);
        }
    }

    private void checkIfEmptyCapturingGroup(ModuleInfo moduleInfo, String regExp, String pathToTest) throws Exception {
        if (!moduleInfo.isValid()) {
            // May be due to empty capturing blocks
            NamedPattern compileArtifactRegex = NamedPattern.compile(regExp);
            NamedMatcher matcher = compileArtifactRegex.matcher(pathToTest);
            if (matcher.regexpMatches() && !matcher.matches()) {
                throw new Exception("Non named capturing groups are not allowed! Use (?:XXX)");
            }
        }
    }
}
