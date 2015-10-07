package org.artifactory.ui.rest.model.builds;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteBuildsModel extends BaseModel {
    private List<BuildCoordinate> buildsCoordinates = Lists.newArrayList();

    public List<BuildCoordinate> getBuildsCoordinates() {
        return buildsCoordinates;
    }

    public void addBuildCuordianate(String buildName, String buildNumber, long date) {
        buildsCoordinates.add(new BuildCoordinate(buildName, buildNumber, date));
    }

    public void addBuildCuordianate(String buildName) {
        buildsCoordinates.add(new BuildCoordinate(buildName));
    }
}
