package org.artifactory.ui.utils;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.io.IOUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.ui.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Chen Keinan
 */
public class MultiPartUtils {
    private static final Logger log = LoggerFactory.getLogger(MultiPartUtils.class);

    public static final int DEFAULT_BUFF_SIZE = 8192;

    /**
     * fetch file data from request and save it to temp folder
     *
     * @param uploadDir - temp folder
     * @param fileNames
     */
    public static void saveFileDataToTemp(CentralConfigService centralConfigService,
            FormDataMultiPart formDataMultiPart, String uploadDir, List<String> fileNames, boolean isUnique) {
        int fileUploadMaxSizeMb = centralConfigService.getMutableDescriptor().getFileUploadMaxSizeMb();
        // get uploaded file map
        Map<String, List<FormDataBodyPart>> fields = formDataMultiPart.getFields();
        long sizeInBytes = getContentLengthFromMultiPart(formDataMultiPart);
        fields.forEach((name, dataBody) -> {
            List<FormDataBodyPart> formDataBodyParts = fields.get(name);
            formDataBodyParts.forEach(formDataBodyPart -> {
                // get file name and data
                InputStream inputStream = formDataBodyPart.getEntityAs(InputStream.class);
                long sizeInMb = FileUtils.bytesToMB(sizeInBytes);
                if (sizeInMb > fileUploadMaxSizeMb && fileUploadMaxSizeMb > 0) {
                    throw new BadRequestException("Uploaded file size is bigger than " + fileUploadMaxSizeMb + "MB");
                }
                String fileName = formDataBodyPart.getContentDisposition().getFileName();
                try {
                    fileName = URLDecoder.decode(fileName, "UTF-8");
                    if (isUnique) {
                        fileName = UUID.randomUUID().toString() + "_" + fileName;
                    }
                    String fileLocation = uploadDir + File.separator + fileName;
                    FileUtils.copyInputStreamToFile(inputStream, new File(fileLocation));
                    fileNames.add(fileName);
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
            });
        });
    }

    /**
     * get content length from multi part
     *
     * @param formDataMultiPart - form data multi part
     * @return - content length in bytes
     */
    private static long getContentLengthFromMultiPart(FormDataMultiPart formDataMultiPart) {
        return Long.parseLong(String.valueOf(formDataMultiPart.getHeaders().get("Content-Length").get(0)));
    }


    /**
     * fetch file data from request and save it to temp folder
     *
     * @param uploadDir - temp folder
     * @param fileNames
     */


    /**
     * save file to folder on server
     * @param centralConfigService - central config service
     * @param formDataMultiPart - multi part  - file content
     * @param uploadDir - upload dir
     * @param fileName - file name
     */
    public static void saveSpecificFile(CentralConfigService centralConfigService,
                                          FormDataMultiPart formDataMultiPart, String uploadDir, String fileName) {
        int fileUploadMaxSizeMb = centralConfigService.getMutableDescriptor().getFileUploadMaxSizeMb();
        // get uploaded file map
        Map<String, List<FormDataBodyPart>> fields = formDataMultiPart.getFields();
        fields.forEach((name, dataBody) -> {
            List<FormDataBodyPart> formDataBodyParts = fields.get(name);
                // get file name and data
                byte[] fileAsBytes = formDataBodyParts.get(0).getValueAs(byte[].class);
                if (FileUtils.bytesToMeg(fileAsBytes.length) > fileUploadMaxSizeMb && fileUploadMaxSizeMb > 0) {
                    throw new BadRequestException("Uploaded file size is bigger than :" + fileUploadMaxSizeMb);
                }
                String fileLocation = uploadDir + File.separator + fileName;
                FileUtils.writeFile(fileLocation, fileAsBytes);
        });
    }

    /**
     * create temp folder if not exist
     * @param uploadDir - temp directory
     */
    public static void createTempFolderIfNotExist(String uploadDir) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(uploadDir));
        } catch (IOException e) {
            throw new  RuntimeException(e);
        }
    }

    /**
     * save extracted file to temp folder
     * @param uploadedFile - uploaded file
     * @param artifactoryResponse - encapsulate data require to response
     */
    public static void saveUploadFileAsExtracted(File uploadedFile, RestResponse artifactoryResponse) throws Exception {
        ZipInputStream zipinputstream = null;
        FileOutputStream fos = null;
        File destFolder;
        try {
            zipinputstream = new ZipInputStream(new FileInputStream(uploadedFile));
            ArtifactoryHome artifactoryHome = ContextHelper.get().getArtifactoryHome();
            destFolder = new File(artifactoryHome.getTempUploadDir(), uploadedFile.getName() + "_extract");
            //  org.apache.commons.io.FileUtils.deleteDirectory(destFolder);
            byte[] buf = new byte[MultiPartUtils.DEFAULT_BUFF_SIZE];
            ZipEntry zipentry;
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = zipentry.getName();
                File destFile = new File(destFolder, entryName);

                if (zipentry.isDirectory()) {
                    if (!destFile.exists()) {
                        if (!destFile.mkdirs()) {
                            artifactoryResponse.error("Cannot create directory " + destFolder);
                            return;
                        }
                    }
                } else {
                    fos = new FileOutputStream(destFile);
                    int n;
                    while ((n = zipinputstream.read(buf, 0, MultiPartUtils.DEFAULT_BUFF_SIZE)) > -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.close();
                }
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
            }
        } catch (Exception e) {
            String errorMessage = "Error during import of " + uploadedFile.getName();
            artifactoryResponse.error(errorMessage);
            log.error(errorMessage, e);
            throw new Exception(errorMessage);
        } finally {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(zipinputstream);
            org.apache.commons.io.FileUtils.deleteQuietly(uploadedFile);
        }
    }
}
