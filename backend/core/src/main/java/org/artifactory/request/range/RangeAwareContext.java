package org.artifactory.request.range;

import java.io.InputStream;

/**
 * Contain the data needed to decorate the HttpResponse in order to support HTTP ranges
 *
 * @author Gidi Shabat
 */
public class RangeAwareContext {
    private long contentLength;
    private InputStream inputStream;
    private int status = -1;
    private String contentType;
    private String etagExtension;
    private String contentRange;

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentRange() {
        return contentRange;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public String getEtagExtension() {
        return etagExtension;
    }

    public void setEtagExtension(String etagExtension) {
        this.etagExtension = etagExtension;
    }
}
