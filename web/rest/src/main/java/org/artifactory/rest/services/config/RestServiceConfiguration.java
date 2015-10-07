package org.artifactory.rest.services.config;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.security.mission.control.MissionControlProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.io.File;
import java.io.IOException;

/**
 * @author Chen Keinan
 */
@Configuration
@ImportResource({"classpath:/META-INF/spring/restContext.xml"})
public class RestServiceConfiguration {

    @Bean(name = "missionControlProperties")
    public MissionControlProperties getDbProperties() throws IOException {
        ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
        File storagePropsFile = artifactoryHome.getMissionControlPropertiesFile();
        return new MissionControlProperties(storagePropsFile);
    }

}
