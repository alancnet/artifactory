package org.artifactory.request.range;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.artifactory.request.range.ResponseWithRangeSupportHelper.HEADER_CONTENT_TYPE;
import static org.artifactory.request.range.ResponseWithRangeSupportHelper.MULTIPART_BYTERANGES_BOUNDRY_SEP;

/**
 * The class represent single http range
 *
 * @author Gidi Shabat
 */
public class Range implements Comparable<Range> {
    public static final Logger log = LoggerFactory.getLogger(Range.class);
    private long start;
    private long end;
    private long entityLength;
    private String contentType;
    private String contentRange;

    Range(String contentType, long start, long end, long entityLength) {
        this.contentType = HEADER_CONTENT_TYPE + ": " + contentType;
        this.start = start;
        this.end = end;
        this.entityLength = entityLength;
    }

    /**
     * Factory method to construct a byte range from a range header value.
     */
    public static List<Range> constructRange(String rangesHeader, String contentType, long entityLength,
            RangeAwareContext context, IfRangeSelector ifRangeSelector) {
        ArrayList<Range> result = Lists.newArrayList();
        if (ifRangeSelector.isIfChange()) {
            return result;
        }

        if (StringUtils.isBlank(rangesHeader)) {
            return result;
        }
        String[] rangesValues = StringUtils.split(rangesHeader, "=");
        if (rangesValues.length != 2) {
            return result;
        }
        // Transform the stringRanges into List<Ranges>
        loadRanges(rangesHeader, contentType, entityLength, context, result, rangesValues[1]);
        // Merge byte ranges if possible - IE handles this well, FireFox not so much
        mergeRanges(result);
        return result;
    }

    /**
     * Validates and create ranges according to the Range header
     */
    private static void loadRanges(String rangesHeader, String contentType, long entityLength,
            RangeAwareContext context,
            ArrayList<Range> result, String rangesValues) {
        String[] realRanges = StringUtils.split(rangesValues, ",");
        for (String range : realRanges) {
            range = range.trim();
            if (StringUtils.isBlank(range)) {
                log.warn("Not satisfiable range skipping range: '{}' for range header: '{}'", range, rangesHeader);
                continue;
            }
            // strip total if present - it does not give us anything useful
            if (range.indexOf('/') != -1) {
                range = range.substring(0, range.indexOf('/'));
            }
            // find the separator
            int separator = range.indexOf('-');
            if (separator == -1) {
                log.warn("Not satisfiable range skipping range: '{}' for range header: '{}'", range, rangesHeader);
            }
            try {
                // split range and parse values
                long start = 0L;
                if (separator > 0) {
                    start = Long.parseLong(range.substring(0, separator).trim());
                }
                long end = entityLength - 1;
                if (separator != range.length() - 1) {
                    end = Long.parseLong(range.substring(separator + 1).trim());
                }
                // return object to represent the byte-range
                result.add(new Range(contentType, start, end, entityLength));
            } catch (NumberFormatException err) {
                log.warn("Not satisfiable range skipping range: '{}' for range header: '{}'", range, rangesHeader);
            }
        }
        // If none of the ranges in the request's Range header are valid then return SC_REQUESTED_RANGE_NOT_SATISFIABLE
        if (realRanges.length > 0 && result.size() == 0) {
            log.warn("Returning not satisfiable range status since none of the ranges in '{}' is valid", rangesHeader);
            //throw new HTTPException(HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            context.setContentRange("bytes */" + entityLength);
            context.setStatus(HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
        }
    }

    /**
     * The class merges continues ranges
     */
    private static void mergeRanges(ArrayList<Range> result) {
        if (result.size() > 1) {
            Collections.sort(result);
            for (int i = 0; i < result.size() - 1; i++) {
                Range first = result.get(i);
                Range second = result.get(i + 1);
                if (first.getEnd() + 1 >= second.getStart()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Merging byte range: " + first + " with " + second);
                    }
                    if (first.getEnd() < second.getEnd()) {
                        // merge second range into first
                        first.setEnd(second.getEnd());
                    }
                    // else we simply discard the second range - it is contained within the first

                    // delete second range
                    result.remove(i + 1);
                    // reset loop index
                    i--;
                }
            }
        }
    }


    /**
     * Output the header bytes for a multi-part byte range header
     */
    public byte[] outputHeader(boolean firstTime) throws IOException {
        StringBuilder buffer = new StringBuilder();
        // output multi-part boundry separator
        if (!firstTime) {
            buffer.append("\r\n");
        }
        buffer.append(MULTIPART_BYTERANGES_BOUNDRY_SEP).append("\r\n");
        buffer.append(this.contentType).append("\r\n");
        buffer.append(getContentRange()).append("\r\n");
        buffer.append("\r\n");
        return buffer.toString().getBytes();
    }

    /**
     * Return the length in bytes of the byte range content including the header bytes
     */
    public int getLength() {
        // length in bytes of range plus it's header plus section marker and line feed bytes
        return MULTIPART_BYTERANGES_BOUNDRY_SEP.length() + 2 +
                this.contentType.length() + 2 +
                getContentRange().length() + 4 +
                (int) (this.end - this.start) + 2;
    }

    /**
     * Return the Content-Range header string value for this byte range
     */
    private String getContentRange() {
        if (this.contentRange == null) {
            this.contentRange = "Content-Range: bytes " + Long.toString(this.start) + "-" +
                    Long.toString(this.end) + "/" + Long.toString(this.entityLength);
        }
        return this.contentRange;
    }

    @Override
    public String toString() {
        return this.start + "-" + this.end;
    }

    @Override
    public int compareTo(Range o) {
        return this.start > o.start ? 1 : -1;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getEntityLength() {
        return entityLength;
    }
}