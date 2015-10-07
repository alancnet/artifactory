package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.checksums;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("checksumsMatch")
public class Checksums {

    private String sha2;
    private String sha1;
    private String sha1Value;
    private String md5;
    private boolean showFixChecksums; //Signifies if the 'fix checksums' button should be shown on UI
    private String message;           //If show fix checksums button this holds the relevant warning

    public String getSha2() {
        return sha2;
    }

    public void setSha2(String sha2) {
        this.sha2 = sha2;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha1Value() {
        return sha1Value;
    }

    public void setSha1Value(String sha1Value) {
        this.sha1Value = sha1Value;
    }

    public boolean isShowFixChecksums() {
        return showFixChecksums;
    }

    public void setShowFixChecksums(boolean showFixChecksums) {
        this.showFixChecksums = showFixChecksums;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void updateFileInfoCheckSum(FileInfo itemInfo, LocalRepoDescriptor localRepoDescriptor,
                                       boolean userHasPermissionsToFix) {
        boolean isLocalRepo = localRepoDescriptor.isLocal();
        String md5 = "";
        boolean checksumsMatch = true;
        ChecksumInfo md5Info = getChecksumOfType(itemInfo, ChecksumType.md5);
        if (md5Info != null) {
            checksumsMatch &= md5Info.checksumsMatch();
            md5 = buildChecksumString(md5Info, isLocalRepo);
        }

        String sha1 = "";
        ChecksumInfo sha1Info = getChecksumOfType(itemInfo, ChecksumType.sha1);
        if (sha1Info != null) {
            checksumsMatch &= sha1Info.checksumsMatch();
            sha1 = buildChecksumString(sha1Info, isLocalRepo);
            sha1Value = sha1Info.getActual();
        }
        this.setMd5(md5);
        this.setSha1(sha1);

        if (checksumsMatch || isOneMissingOtherMatches(sha1Info, md5Info)) {
            showFixChecksums = false;
        } else {
            if (userHasPermissionsToFix && !RepoType.Docker.equals(localRepoDescriptor.getType())) {
                showFixChecksums = true;
            }
            message = prepareFixChecksumsMessage(userHasPermissionsToFix, isLocalRepo, md5Info, sha1Info);
        }
    }

    public void updatePropertiesChecksums(FileInfo fileInfo) {
        Properties properties = ContextHelper.get().getRepositoryService().getProperties(fileInfo.getRepoPath());
        if (properties != null) {
            String sha256 = properties.getFirst("sha256");
            if (StringUtils.isNotBlank(sha256)) {
                this.setSha2("SHA-2: " + sha256);
            }
        }
    }

    private ChecksumInfo getChecksumOfType(org.artifactory.fs.FileInfo file, ChecksumType checksumType) {
        return file.getChecksumsInfo().getChecksumInfo(checksumType);
    }

    private String prepareFixChecksumsMessage(boolean userHasPermissionsToFix, boolean isLocalRepo,
                                              ChecksumInfo md5Info, ChecksumInfo sha1Info) {
        StringBuilder message = new StringBuilder();
        if (isAllChecksumsMissing(sha1Info, md5Info)) {
            if (isLocalRepo) {
                message.append("Client did not publish a checksum value.\n");
            } else {
                message.append("Remote checksum doesn't exist.\n");
            }
        } else if (isAllChecksumsBroken(sha1Info, md5Info) || isOneOkOtherBroken(sha1Info, md5Info) ||
                isOneMissingOtherBroken(sha1Info, md5Info)) {
            String repoClass = isLocalRepo ? "Uploaded" : "Remote";
            message = new StringBuilder().append(repoClass).append(" checksum doesn't match the actual checksum.\n ")
                    .append("Please redeploy the artifact with a correct checksum.");
        }
        if (userHasPermissionsToFix) {
            message.append("If you trust the ").append(isLocalRepo ? "uploaded" : "remote")
                    .append(" artifact you can accept the actual checksum by clicking the 'Fix Checksum' button.");
        }
        return message.toString();
    }

    private boolean isChecksumMatch(ChecksumInfo info) {
        return info != null && info.checksumsMatch();
    }

    private boolean isChecksumBroken(ChecksumInfo info) {
        return info != null && !info.checksumsMatch();
    }

    private boolean isChecksumMissing(ChecksumInfo info) {
        return info == null || info.getOriginal() == null;
    }

    /**
     * @return Check if one of the {@link ChecksumType} is ok and the other broken
     */
    private boolean isOneOkOtherBroken(ChecksumInfo sha1Info, ChecksumInfo md5Info) {
        return (isChecksumMatch(sha1Info) && isChecksumBroken(md5Info))
                || (isChecksumMatch(md5Info) && isChecksumBroken(sha1Info));
    }

    /**
     * @return Check if one of the {@link ChecksumType} is missing and the other is broken (i.e don't match).
     */
    private boolean isOneMissingOtherBroken(ChecksumInfo sha1Info, ChecksumInfo md5Info) {
        return isChecksumMatch(sha1Info) && isChecksumBroken(md5Info) || (isChecksumMatch(md5Info)
                && isChecksumBroken(sha1Info));
    }

    /**
     * @return Check if one of the {@link ChecksumType} is missing and the other matches.
     */
    private boolean isOneMissingOtherMatches(ChecksumInfo sha1Info, ChecksumInfo md5Info) {
        return isChecksumMatch(sha1Info) && (isChecksumMissing(md5Info)) || ((isChecksumMatch(md5Info)) &&
                (isChecksumMissing(sha1Info)));
    }

    /**
     * @return Check that all {@link ChecksumType}s are broken (but are <b>NOT<b/> missing)
     */
    private boolean isAllChecksumsBroken(ChecksumInfo... checksumInfos) {
        for (ChecksumInfo type : checksumInfos) {
            if (!isChecksumBroken(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Check that all {@link ChecksumType}s are <b>missing<b/>
     */
    private boolean isAllChecksumsMissing(ChecksumInfo... checksumInfos) {
        for (ChecksumInfo type : checksumInfos) {
            if (!isChecksumMissing(type)) {
                return false;
            }
        }
        return true;
    }

    private String buildChecksumString(ChecksumInfo checksumInfo, boolean isLocalRepo) {
        StringBuilder sb = new StringBuilder()
                .append(checksumInfo.getType()).append(": ")
                .append(checksumInfo.getActual()).append(" (")
                .append(isLocalRepo ? "Uploaded" : "Remote").append(": ");
        if (checksumInfo.getOriginal() != null) {
            if (checksumInfo.checksumsMatch()) {
                sb.append("Identical");
            } else {
                sb.append(checksumInfo.getOriginal());
            }
        } else {
            sb.append("None");
        }

        return sb.append(")").toString();
    }
}
