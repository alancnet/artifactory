package org.artifactory.request.range.stream;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.artifactory.request.range.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Returns single sub-stream by wrapping the stream and skipping irrelevant bytes
 *
 * @author Gidi Shabat
 */
public class SingleRangeInputStream extends InputStream {
    public static final Logger log = LoggerFactory.getLogger(SingleRangeInputStream.class);
    private InputStream inputStream;

    public SingleRangeInputStream(Range range, InputStream inputStream) throws IOException {
        // Skip irrelevant bytes
        ByteStreams.skipFully(inputStream, range.getStart());
        // Limit the stream
        this.inputStream = ByteStreams.limit(inputStream, range.getEnd() - range.getStart() + 1);
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(inputStream);
    }

}
