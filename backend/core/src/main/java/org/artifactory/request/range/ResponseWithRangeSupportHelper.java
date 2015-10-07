package org.artifactory.request.range;

import org.artifactory.request.range.stream.MultiRangeInputStream;
import org.artifactory.request.range.stream.SingleRangeInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT;
import static org.artifactory.common.ConstantValues.httpRangeSupport;
import static org.artifactory.request.range.IfRangeSelector.constructIfRange;
import static org.artifactory.request.range.Range.constructRange;

/**
 * The class creates context that contains the data needed to decorate the httpResponse in order to support HTTP ranges
 *
 * @author Gidi Shabat
 */
public class ResponseWithRangeSupportHelper {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String MULTIPART_BYTERANGES_BOUNDRY = "BCD64322345343217845286A";
    public static final String MULTIPART_BYTERANGES_HEADER = "multipart/byteranges; boundary=" + MULTIPART_BYTERANGES_BOUNDRY;
    public static final String MULTIPART_BYTERANGES_BOUNDRY_SEP = "--" + MULTIPART_BYTERANGES_BOUNDRY;
    public static final String MULTIPART_BYTERANGES_BOUNDRY_END = MULTIPART_BYTERANGES_BOUNDRY_SEP + "--";
    private static final Logger log = LoggerFactory.getLogger(ResponseWithRangeSupportHelper.class);

    public static RangeAwareContext createRangeAwareContext(InputStream in, long length, String rangesString,
            String ifRange, String mimeType, long lastModified, String sha1) throws IOException {
        RangeAwareContext context = new RangeAwareContext();
        // resolve the If-Range behaviour from the If-Range header
        IfRangeSelector ifRangeSelector = constructIfRange(ifRange, lastModified, sha1);
        // Resolve the ranges from the ranges header
        List<Range> ranges = constructRange(rangesString, mimeType, length, context, ifRangeSelector);
        // Do not continue if error occurred
        if (context.getStatus() > 0) {
            return context;
        }
        // Fill the result context according to the ranges.
        if (!httpRangeSupport.getBoolean() || ranges.size() == 0) {
            handleSimpleResponse(in, length, mimeType, context);
        } else if (ranges.size() == 1) {
            handleSingleRangeResponse(in, ranges, mimeType, context);
        } else {
            handleMultiRangeResponse(in, ranges, context);
        }
        return context;
    }

    private static void handleSimpleResponse(InputStream in, long length, String mimeType, RangeAwareContext context)
            throws IOException {
        log.debug("Preparing response for simple response (None range response)");
        // Update headers, content type, content length and logs
        context.setContentLength(length);
        context.setContentType(mimeType);
        context.setInputStream(in);
        context.setEtagExtension("");
    }

    private static void handleSingleRangeResponse(InputStream in, List<Range> ranges, String mimeType,
            RangeAwareContext context) throws IOException {
        log.debug("Preparing response for single range response");
        // Resolve range
        Range range = ranges.get(0);
        String totalLength = range.getEntityLength() >= 0 ? Long.toString(range.getEntityLength()) : "*";
        String contentRange = "bytes " + range.getStart() + "-" + range.getEnd() + "/" + totalLength;
        context.setStatus(SC_PARTIAL_CONTENT);
        context.setContentType(mimeType);
        context.setContentRange(contentRange);
        context.setContentLength((range.getEnd() - range.getStart()) + 1L);
        context.setInputStream(new SingleRangeInputStream(range, in));
        context.setEtagExtension("-" + range.getStart() + "-" + range.getEnd());
    }

    private static void handleMultiRangeResponse(InputStream in, List<Range> ranges, RangeAwareContext context)
            throws IOException {
        log.debug("Preparing response for multi range response");
        // calculate response content length
        long length = MULTIPART_BYTERANGES_BOUNDRY_END.length() + 2;
        for (Range r : ranges) {
            length += r.getLength();
        }
        context.setStatus(SC_PARTIAL_CONTENT);
        context.setContentType(MULTIPART_BYTERANGES_HEADER);
        context.setContentLength(length);
        context.setInputStream(new MultiRangeInputStream(ranges, in));
        StringBuilder builder = new StringBuilder();
        for (Range range : ranges) {
            builder.append("-").append(range.getStart()).append("-").append(range.getEnd());
        }
        context.setEtagExtension(builder.toString());
    }
}
