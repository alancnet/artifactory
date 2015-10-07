package org.artifactory.ui.rest.service.utils;

import org.artifactory.ui.rest.service.utils.cron.GetCronNextTimeService;
import org.artifactory.ui.rest.service.utils.groups.GetGroupPermissionsService;
import org.artifactory.ui.rest.service.utils.predefinevalues.GetPreDefineValuesService;
import org.artifactory.ui.rest.service.utils.repoPropertySet.GetRepoPropertySetService;
import org.artifactory.ui.rest.service.utils.repositories.GetAllRepositoriesService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class UtilsServiceFactory {

    @Lookup
    public abstract GetCronNextTimeService getGetCronNextTimeService();

    @Lookup
    public abstract GetAllRepositoriesService getGetAllRepositoriesService();

    @Lookup
    public abstract GetPreDefineValuesService getGetPreDefineValuesService();

    @Lookup
    public abstract GetRepoPropertySetService getGetRepoPropertySetService();

    @Lookup
    public abstract GetGroupPermissionsService getGroupPermissions();
}
