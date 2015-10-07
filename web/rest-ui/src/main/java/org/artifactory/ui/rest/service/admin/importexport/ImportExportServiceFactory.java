package org.artifactory.ui.rest.service.admin.importexport;

import org.artifactory.ui.rest.service.admin.importexport.exportdata.ExportRepositoryService;
import org.artifactory.ui.rest.service.admin.importexport.exportdata.ExportSystemService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.ImportRepositoryService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.ImportSystemService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.UploadExtractedZipService;
import org.artifactory.ui.rest.service.admin.importexport.importdata.UploadSystemExtractedZipService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class ImportExportServiceFactory {

    @Lookup
    public abstract ImportRepositoryService importRepositoryService();

    @Lookup
    public abstract UploadExtractedZipService uploadExtractedZip();

    @Lookup
    public abstract ImportSystemService importSystem();

    @Lookup
    public abstract ExportRepositoryService exportRepository();

    @Lookup
    public abstract ExportSystemService exportSystem();

    @Lookup
    public abstract UploadSystemExtractedZipService uploadSystemExtractedZip();


}
