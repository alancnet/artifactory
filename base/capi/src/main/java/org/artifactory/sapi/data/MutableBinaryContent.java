package org.artifactory.sapi.data;

/**
 * Date: 8/5/11
 * Time: 2:29 PM
 *
 * @author Fred Simon
 */
public interface MutableBinaryContent extends BinaryContent {
    void setLastModified(long lastModified);
}
