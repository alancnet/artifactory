package org.artifactory.storage.db.fs.itest.service;

import com.google.common.collect.Sets;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.checksum.ChecksumInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.fs.ItemInfo;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.FolderInfoImpl;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.binstore.service.InternalBinaryStore;
import org.artifactory.storage.db.itest.DbBaseTest;
import org.artifactory.storage.fs.service.FileService;
import org.artifactory.util.PathValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.HashSet;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Integration test for special characters repo paths saving.
 *
 * @author Shay Yaakov
 * @see PathValidator
 */
@Test
public class FileServiceSpecialCharsTest extends DbBaseTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private BinaryStore binaryStore;

    private String sha1;

    @BeforeClass
    void insertBinaryEntry() throws IOException {
        bindDummyContext();
        try {
            // Create a dummy binary entry for testing file creation
            BinaryInfo binaryInfo = binaryStore.addBinary(new ByteInputStream(new byte[]{1, 2, 3}, 3));
            sha1 = binaryInfo.getSha1();
        } finally {
            unbindDummyContext();
        }
    }

    public void createFolderValidChars() {
        createFolder("~`@#$%^&()-+{}[];.+");
        createFolder("a;nt/&ant(#)-junit{1}/[2]1.6.5~");
        createFolder("ca/juliusd  avies/not-y$t-commons-ssl/&0.3.9/&n!t-yet-comm#ns-ssl-0.3.;.()%&r");
    }

    public void createFileValidChars() {
        createFile("log4j/log4j/1.2.9/log4j-1.2.9.jar.md5");
        createFile("javassist/javassist/3.4.GA/javassist-3.4.GA.jar.sha1");
        createFile("rome/rome/0.9/rome-0.9.pom.tmp.sha1.tmp");
        createFile(".index");
        createFile(".index/");
        createFile(".index/nexus-maven-repository-index.gz");
        createFile(")/(& ^%$#. {}()[]+/~");
    }

    public void rootRepo() {
        createFolder("");
    }

    public void invalidAllSpaces() {
        createFile("   ");
        createFolder("              ");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleColon() {
        createFile(":");
        createFolder(":");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleAmpersand() {
        createFile("&");
        createFolder("&");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleDot() {
        createFile(".");
        createFolder(".");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSingleDotDot() {
        createFile("..");
        createFolder("..");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidDot() {
        createFile("org/antlr/./3.4/antlr-master-3.4");
        createFolder("org/antlr/./3.4/antlr-master-3.4");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidDotDot() {
        createFile("javacvs/javacvs/../javacvs-atlassian-20080407.jar.sha1");
        createFolder("msv/msv/../msv-20020414");
    }

    @Test
    public void validCarretLeft() {
        createFile("javacvs/javacvs/javacvs-at<lassian-20080407.jar.sha1");
        createFolder("msv/msv/msv-<20020414");
    }

    @Test
    public void validCarretRight() {
        createFile("edu/ucar/netcdf/>4.2-min");
        createFolder("edu/ucar/netcdf4>.2-min");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalQuestionMark() {
        createFile("edu/ucar/ne?tcdf/4.2-min");
        createFolder("edu/ucar?/netcdf4.2-min");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalQuotationMark() {
        createFile("vxcvxv\"");
        createFolder("/vxcvxcv/af.asd/asdasdw\"sss\"df.jar");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalPipe() {
        createFile("bbb/ccc|cccc/df.jar");
        createFolder("fold|er");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalStar() {
        createFile("ddd**ddd");
        createFolder("*");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void illegalColon() {
        createFile("jaxen/jaxen/1.1.:1/jaxen-1.1.1.pom");
        createFolder("jaxen/jaxen/|1.1.1");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSpaceBeforeSlash() {
        createFile("sdfsdfsdf  /sdgfdfg.sdfd");
        createFolder("bvsfg/sdfsfew/sdfsdf  /");
    }

    @Test(expectedExceptions = InvalidPathException.class)
    public void invalidSpaceAfterSlash() {
        createFile("vvc/cccx/sdsd/ cvcv/");
        createFolder("asdasd/aserwe/dfhgc/bv/ sdfsdf/dfsdf");
    }

    private void createFolder(String path) {
        FolderInfoImpl folderSave = new FolderInfoImpl(new RepoPathImpl("repo1", path));
        long folderId = fileService.createFolder(folderSave);
        assertTrue(fileService.exists(folderSave.getRepoPath()));
        ItemInfo itemInfo = fileService.loadItem(folderSave.getRepoPath());
        assertNotNull(itemInfo);
        assertTrue(itemInfo.isFolder());
        assertTrue(fileService.deleteItem(folderId));
    }

    private void createFile(String path) {
        HashSet<ChecksumInfo> checksums = Sets.newHashSet(new ChecksumInfo(ChecksumType.sha1, sha1, sha1));

        FileInfoImpl fileSave = new FileInfoImpl(new RepoPathImpl("repo1", path));
        fileSave.setChecksums(checksums);
        long fileId = fileService.createFile(fileSave);
        assertTrue(fileService.exists(fileSave.getRepoPath()));
        assertNotNull(fileService.loadItem(fileSave.getRepoPath()));
        assertTrue(fileService.deleteItem(fileId));
    }
}
