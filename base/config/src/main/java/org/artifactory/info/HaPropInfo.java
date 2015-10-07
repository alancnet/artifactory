package org.artifactory.info;

import com.google.common.collect.Lists;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ha.HaNodeProperties;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Yoav Luft
 */
public class HaPropInfo extends BasePropInfoGroup {
    @Override
    public boolean isInUse() {
        return ArtifactoryHome.get().isHaConfigured();
    }

    @Override
    public InfoObject[] getInfo() {
        if (ArtifactoryHome.get().isHaConfigured()) {
            HaNodeProperties haNodeProperties = ArtifactoryHome.get().getHaNodeProperties();
            if (haNodeProperties != null) {
                Properties nodeProps = haNodeProperties.getProperties();
                List<InfoObject> infoObjects = Lists.newArrayList();
                for (Map.Entry<Object, Object> prop : nodeProps.entrySet()) {
                    InfoObject infoObject = new InfoObject(prop.getKey().toString(), prop.getValue().toString());
                    infoObjects.add(infoObject);
                }
                return  infoObjects.toArray(new InfoObject[infoObjects.size()]);
            }
        }
        // else
        return new InfoObject[0];
    }
}
