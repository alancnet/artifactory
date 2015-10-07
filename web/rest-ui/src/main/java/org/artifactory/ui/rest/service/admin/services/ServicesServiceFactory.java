package org.artifactory.ui.rest.service.admin.services;

import org.artifactory.ui.rest.service.admin.services.backups.*;
import org.artifactory.ui.rest.service.admin.services.filesystem.BrowseFileSystemService;
import org.artifactory.ui.rest.service.admin.services.indexer.GetIndexerService;
import org.artifactory.ui.rest.service.admin.services.indexer.RunIndexNowService;
import org.artifactory.ui.rest.service.admin.services.indexer.UpdateIndexerService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ServicesServiceFactory {
    //backups services
    @Lookup
    public abstract CreateBackupService createBackupService();

    @Lookup
    public abstract UpdateBackupService updateBackupService();

    @Lookup
    public abstract GetBackupService getBackupService();

    @Lookup
    public abstract DeleteBackupService deleteBackupService();

    @Lookup
    public abstract RunNowBackupService runNowBackupService();
    // file system browser
    @Lookup
    public abstract BrowseFileSystemService browseFileSystemService();
    //indexer service
    @Lookup
    public abstract UpdateIndexerService updateIndexerService();

    @Lookup
    public abstract GetIndexerService getIndexerService();

    @Lookup
    public abstract RunIndexNowService runIndexNowService();


}
