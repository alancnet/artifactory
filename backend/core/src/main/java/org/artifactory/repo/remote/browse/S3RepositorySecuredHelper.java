/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.repo.remote.browse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.utils.CharsetNames;
import org.artifactory.repo.HttpRepo;
import org.artifactory.security.crypto.CryptoHelper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author mamo
 */
public class S3RepositorySecuredHelper {

    public static final String S3_ENDPOINT = "s3.amazonaws.com";
    private static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Builds a secured rest url request for Amazon S3
     *
     * @param url        The path to the item, for example https://s3.amazonaws.com/bucket/folder/file.ext
     * @param prefix     The prefix to the item, for example folder/file.ext
     * @param httpRepo   The repository assuming to contain the access key/secret key in username/password, respectively
     * @param expiration The time of expiration for the generated request in millis
     * @return A secured rest url request
     * @throws Exception
     */
    public static String buildSecuredS3RequestUrl(String url, String prefix, HttpRepo httpRepo,
            long expiration) {
        String awsAccessKey = httpRepo.getUsername();
        String awsSecretKey = CryptoHelper.decryptIfNeeded(httpRepo.getPassword());

        try {
            String endpoint = S3_ENDPOINT;

            String protocol = url.startsWith("https:") ? "https://" : "http://";
            String bucketName = getBucketName(url);
            String hostname = buildBucketHostname(bucketName, endpoint);
            long expires = expiration / 1000;

            String bucketPath = buildBucketPath(endpoint, hostname);
            String prefixPath = buildPrefixPath(hostname, bucketName, prefix);
            String signature = encodeUrl(signWithHmacSha1(awsSecretKey,
                    "GET\n" +
                            "\n\n" + //content-type,content-md5
                            String.valueOf(expires) + "\n" +
                            "/" + bucketPath + prefixPath
            ));

            String uriPath = prefixPath +
                    "?AWSAccessKeyId=" + awsAccessKey +
                    "&Expires=" + expires +
                    "&Signature=" + signature;

            return protocol + hostname + "/" + uriPath;

        } catch (Exception e) {
            throw new RuntimeException("Could not get signed url for " + url + " and bucket " + getBucketName(url), e);
        }
    }

    public static String getPrefix(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        String str = S3_ENDPOINT + "/";
        int i = url.indexOf(str);
        String substring = url.substring(i + str.length());
        int i1 = substring.indexOf("/");
        if (i1 != -1) {
            return substring.substring(i1 + 1);
        } else {
            return "";
        }
    }

    private static String getBucketName(String url) {
        String endpoint = S3_ENDPOINT;
        String substring = url.substring(url.indexOf(endpoint) + endpoint.length());
        if (substring.startsWith("/")) {
            substring = substring.substring(1);
        }
        int i = substring.indexOf("/");
        return i != -1 ? substring.substring(0, i) : "";
    }

    private static String buildPrefixPath(String hostname, String bucketName, String prefix) throws Exception {
        String uriPath = "";
        String encodedPrefix = encodePath(prefix, "/");
        if (!S3_ENDPOINT.equals(hostname)) {
            uriPath = prefix != null ? encodedPrefix : "";
        } else {
            uriPath = bucketName + (prefix != null ? "/" + encodedPrefix : "");
        }
        return uriPath;
    }

    private static String buildBucketPath(String endpoint, String hostname) {
        String path = "";
        if (!endpoint.equals(hostname)) {
            int i = hostname.lastIndexOf("." + endpoint);
            path = i > 0 ? hostname.substring(0, i) + "/" : hostname + "/";
        }
        return path;
    }

    private static String buildBucketHostname(String bucketName, String s3Endpoint) {
        return bucketName + "." + s3Endpoint;
    }

    private static String signWithHmacSha1(String awsSecretKey, String canonicalString) throws Exception {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(awsSecretKey.getBytes(CharsetNames.UTF_8), HMAC_SHA1);
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(signingKey);
            byte[] b64 = Base64.encodeBase64(mac.doFinal(canonicalString.getBytes(CharsetNames.UTF_8)));
            return new String(b64, CharsetNames.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Could not sign with " + HMAC_SHA1, e);
        }
    }

    private static String encodeUrl(String string) {
        try {
            return URLEncoder.encode(string, CharsetNames.UTF_8).replace("+", "%20").replace("%40", "@");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not encode string " + string, e);
        }
    }

    private static String encodePath(String path, String delimiter) throws Exception {
        StringBuilder sb = new StringBuilder();
        String split[] = path.split(delimiter);
        for (int i = 0; i < split.length; i++) {
            sb.append(encodeUrl(split[i]));
            if (i < split.length - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }
}
