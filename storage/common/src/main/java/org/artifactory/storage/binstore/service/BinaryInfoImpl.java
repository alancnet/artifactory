package org.artifactory.storage.binstore.service;

import org.apache.commons.lang.StringUtils;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.io.checksum.Sha1Md5ChecksumInputStream;

/**
 * @author Gidi Shabat
 */
public class BinaryInfoImpl implements BinaryInfo {
    private long length;
    private String md5;
    private String sha1;


    public BinaryInfoImpl(long length, String md5, String sha1) {
        this.length = length;
        this.md5 = md5;
        this.sha1 = sha1;
    }
    public BinaryInfoImpl(Sha1Md5ChecksumInputStream checksumInputStream) {
        this.sha1 = checksumInputStream.getSha1();
        this.md5 = checksumInputStream.getMd5();
        this.length = checksumInputStream.getTotalBytesRead();
        simpleValidation();
    }

    public BinaryInfoImpl(String sha1, String md5, long length) {
        this.sha1 = sha1;
        this.md5 = md5;
        this.length = length;
        simpleValidation();
    }

    private void simpleValidation() {
        if (StringUtils.isBlank(sha1) || sha1.length() != ChecksumType.sha1.length()) {
            throw new IllegalArgumentException("SHA1 value '" + sha1 + "' is not a valid checksum");
        }
        if (StringUtils.isBlank(md5) || md5.length() != ChecksumType.md5.length()) {
            throw new IllegalArgumentException("MD5 value '" + md5 + "' is not a valid checksum");
        }
        if (length < 0L) {
            throw new IllegalArgumentException("Length " + length + " is not a valid length");
        }
    }

    public boolean isValid() {
        simpleValidation();
        return ChecksumType.sha1.isValid(sha1) && ChecksumType.md5.isValid(md5);
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return sha1.equals(((BinaryInfoImpl) o).sha1);
    }

    @Override
    public int hashCode() {
        return sha1.hashCode();
    }

    @Override
    public String toString() {
        return "{" + sha1 + ',' + md5 + ',' + length + '}';
    }
}
