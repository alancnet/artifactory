package org.artifactory.storage.binstore.service.providers;

import org.artifactory.binstore.BinaryInfo;
import org.artifactory.storage.binstore.service.BinaryNotFoundException;
import org.artifactory.storage.binstore.service.BinaryProviderBase;
import org.artifactory.storage.binstore.service.annotation.BinaryProviderClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Support S3 Multi tries
 * Each request is being requested N times N=binary.provider.binaryProvider.max.retry.number property value (default =5)
 *
 * @author Gidi Shabat
 */
@BinaryProviderClassInfo(nativeName = "retry")
public class RetryBinaryProvider extends BinaryProviderBase {
    private static final Logger log = LoggerFactory.getLogger(RetryBinaryProvider.class);
    private int interval;
    private int maxTrays;

    @Override
    public void initialize() {
        this.maxTrays = getIntParam("maxTrays", getStorageProperties().getMaxRetriesNumber());
        this.interval = getIntParam("interval", getStorageProperties().getDelayBetweenRetries());
    }

    @Override
    public boolean exists(String sha1, long length) {
        return isExist(sha1, length, 0);
    }

    @Override
    public InputStream getStream(String path) {
        return getStream(path, 0);
    }

    @Override
    public BinaryInfo addStream(InputStream inputStream) throws IOException {
        return addStream(inputStream, 0);
    }

    @Override
    public boolean delete(String path) {
        return delete(path, 0);
    }

    public boolean delete(String path, int trying) {
        try {
            return next().delete(path);
        } catch (Exception e) {
            if (trying < maxTrays) {
                waitDelayTime();
                log.trace(
                        "Failed to delete blob from  '{}'  from next binary provider, trying again for the  '{}'  time",
                        path, trying);
                return delete(path, ++trying);
            } else {
                log.error("Failed to delete blob  '{}'  item from next binary provider", path, e);
                return false;
            }
        }
    }

    public boolean isExist(String sha1, long length, int trying) {
        try {
            return next().exists(sha1, length);
        } catch (Exception e) {
            if (trying < maxTrays) {
                waitDelayTime();
                log.trace(
                        "Failed to check if blob  '{}'  exist in next binary provider, trying again for the  '{}'  time",
                        sha1, trying);
                return isExist(sha1, length, ++trying);
            } else {
                log.error("Failed to check if blob  '{}'  exist in next binary provider", sha1, e);
                throw e;
            }
        }
    }

    @Nonnull
    public InputStream getStream(String path, int trying) {
        try {
            return next().getStream(path);
        } catch (Exception e) {
            if (e instanceof BinaryNotFoundException) {
                throw e;
            }
            if (trying < maxTrays) {
                waitDelayTime();
                log.trace("Failed to fetch  blob  '{}'  from next binary provider, trying again for the  '{}'  time",
                        path, trying);
                return getStream(path, ++trying);
            } else {
                log.error("Failed to fetch blob  '{}'  from next binary provider", path, e);
                throw e;
            }
        }
    }

    public BinaryInfo addStream(InputStream inputStream, int trying) throws IOException {
        try {
            return next().addStream(inputStream);
        } catch (Exception e) {
            if (trying < maxTrays) {
                log.warn(
                        "Failed to add  blob to next binary provider for the '{}' time, a retry will start in seconds ",
                        trying + 1);
                log.debug(
                        "Failed to add  blob to next binary provider for the '{}' time, a retry will start in seconds ",
                        trying + 1, e);
                waitDelayTime();
                return addStream(inputStream, ++trying);
            } else {
                log.error("Failed to add blob to next binary provider", e);
                throw e;
            }
        }

    }

    private void waitDelayTime() {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {
            log.debug("waiting {} milli seconds before next retry");
        }
    }
}
