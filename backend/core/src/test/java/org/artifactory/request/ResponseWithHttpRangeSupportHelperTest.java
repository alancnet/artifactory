package org.artifactory.request;

import org.apache.commons.io.IOUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.request.range.RangeAwareContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.artifactory.request.range.ResponseWithRangeSupportHelper.createRangeAwareContext;

/**
 * @author Gidi Shabat
 */
@Test
public class ResponseWithHttpRangeSupportHelperTest {
    private byte[] content;

    @BeforeClass
    public void init() {
        ArtifactoryHome.bind(new ArtifactoryHome(new File("./target/test/DebianEventsTest")));
        content = "0123456789ABCDEFGI".getBytes();
    }

    @Test
    public void noRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, null, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(content.length, context.getContentLength(), "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, content, "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), -1, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void emptyRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(content.length, context.getContentLength(), "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, content, "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), -1, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void invalidRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "ranges:a-b";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(content.length, context.getContentLength(), "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, content, "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), -1, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void singleRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "byte=1-10";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(10, context.getContentLength(), "1-10");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, "123456789A".getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-10", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), "bytes 1-10/18", "Expecting no change in the content type");
    }

    @Test
    public void singleWithStartOnlyRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "bytes=1-";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(content.length - 1, context.getContentLength(),
                "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        Assert.assertEquals(resultContent, "123456789ABCDEFGI".getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-17", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), "bytes 1-17/18", "Expecting no change in the content type");
    }

    @Test
    public void multiRangeRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "bytes=1-3,7-10";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(context.getContentLength(), 196, "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        String expectedResult = "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 1-3/18\r\n" +
                "\r\n" +
                "123\r\n" +
                "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 7-10/18\r\n" +
                "\r\n" +
                "789A--BCD64322345343217845286A--\r\n";
        Assert.assertEquals(resultContent, expectedResult.getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-3-7-10", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "multipart/byteranges; boundary=BCD64322345343217845286A",
                "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void multiRangeDescendingOrderRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "bytes=7-10,1-3";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(context.getContentLength(), 196, "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        String expectedResult = "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 1-3/18\r\n" +
                "\r\n" +
                "123\r\n" +
                "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 7-10/18\r\n" +
                "\r\n" +
                "789A--BCD64322345343217845286A--\r\n";
        Assert.assertEquals(resultContent, expectedResult.getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-3-7-10", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "multipart/byteranges; boundary=BCD64322345343217845286A",
                "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void multiWithPartialErrorRangeDescendingOrderRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "bytes=7-10,12-a,1-3";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(context.getContentLength(), 196, "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        String expectedResult = "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 1-3/18\r\n" +
                "\r\n" +
                "123\r\n" +
                "--BCD64322345343217845286A\r\n" +
                "Content-Type: pdf\r\n" +
                "Content-Range: bytes 7-10/18\r\n" +
                "\r\n" +
                "789A--BCD64322345343217845286A--\r\n";
        Assert.assertEquals(resultContent, expectedResult.getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-3-7-10", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "multipart/byteranges; boundary=BCD64322345343217845286A",
                "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), null, "Expecting no change in the content type");
    }

    @Test
    public void multiWithMergeRangeDescendingOrderRequest() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        String range = "bytes=7-10,1-6";
        RangeAwareContext context = createRangeAwareContext(inputStream, content.length, range, null, "pdf",
                -1, null);
        // Assert content length
        Assert.assertEquals(context.getContentLength(), 10, "Expecting no change in the content length");
        // Assert content
        byte[] resultContent = IOUtils.toByteArray(context.getInputStream());
        String expectedResult = "123456789A";
        Assert.assertEquals(resultContent, expectedResult.getBytes(), "Expecting no change in the content");
        //Assert status
        Assert.assertEquals(context.getStatus(), 206, "Expecting no status");
        //Assert etag extension
        Assert.assertEquals(context.getEtagExtension(), "-1-10", "Expecting no etag to be set");
        // Assert content type
        Assert.assertEquals(context.getContentType(), "pdf", "Expecting no change in the content type");
        // Assert content range
        Assert.assertEquals(context.getContentRange(), "bytes 1-10/18", "Expecting no change in the content type");
    }

}
