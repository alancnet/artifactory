package org.artifactory.rest.common.service;

import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.artifactory.addon.AddonsManager;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.api.rest.search.result.VersionRestResult;
import org.artifactory.util.HttpClientConfigurator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Service used to discover capabilities of another resources
 *
 * @author michaelp
 */
@Component
public class ResearchService {

    private static final Logger log = LoggerFactory.getLogger(ResearchService.class);

    private static final String VERSION_FOUR_ONE = "4.1";
    private static final String ARTIFACTORY_SYSTEM_VERSION_PATH = "/api/system/version";
    private static final String ARTIFACTORY_APP_PATH = "/artifactory";
    private static final String SNAPSHOT = "-SNAPSHOT";

    /**
     * Checks whether given target is another artifactory instance,
     * logic to achieve positive answer is:
     *
     * 1. path starts ARTIFACTORY_APP_PATH + 200-OK on GET ARTIFACTORY_APP_PATH + ARTIFACTORY_SYSTEM_VERSION_PATH
     *
     * or
     *
     * 2. 200-OK on GET ARTIFACTORY_SYSTEM_VERSION_PATH
     *
     * @return boolean
     */
    public boolean isArtifactory(String url) {
        if(!Strings.isNullOrEmpty(url)) {
            URI uri = URI.create(url);
            if (uri != null) {
                if (uri.getPath().startsWith(ARTIFACTORY_APP_PATH))
                    return doCheck(getDefaultHttpClient(), uri, true);
                return doCheck(getDefaultHttpClient(), uri, false);
            } else {
                log.debug("Url is malformed.");
            }
        } else {
            log.debug("Url is a mandatory (query) parameter.");
        }
        return false;
    }

    /**
     * Checks whether given target is another artifactory instance,
     * logic to achieve positive answer is:
     *
     * 1. path starts ARTIFACTORY_APP_PATH + 200-OK on GET ARTIFACTORY_APP_PATH + ARTIFACTORY_SYSTEM_VERSION_PATH
     *
     * or
     *
     * 2. 200-OK on GET ARTIFACTORY_SYSTEM_VERSION_PATH
     *
     *
     * @param url url to test against
     * @param client http client to be used
     *
     * @return boolean
     */
    public boolean isArtifactory(String url, CloseableHttpClient client) {
        assert client != null : "HttpClient cannot be empty";

        if(!Strings.isNullOrEmpty(url)) {
            URI uri = URI.create(url);
            if (uri != null) {
                if (uri.getPath().startsWith(ARTIFACTORY_APP_PATH))
                    return doCheck(client, uri, true);
                return doCheck(client, uri, false);
            } else {
                log.debug("Url is malformed.");
            }
        } else {
            log.debug("Url is a mandatory (query) parameter.");
        }
        return false;
    }

    /**
     * Performs actual check
     *
     * @param client http client to use
     * @param uri remote uri to test against
     * @param inArtifactoryContext whether given app deployed
     *                             in /artifactory context or not
     *
     * @return boolean
     */
    private boolean doCheck(CloseableHttpClient client, URI uri, boolean inArtifactoryContext) {
        assert client != null : "HttpClient cannot be empty";

        CloseableHttpResponse response;
        if (uri.getPath().startsWith(ARTIFACTORY_APP_PATH)) {
            String newUri = produceTestUrl(uri, inArtifactoryContext);
            HttpGet getMethod = new HttpGet(newUri);

            try {
                response = client.execute(getMethod);
                String returnedInfo = null;
                AddonsManager addonsManager = ContextHelper.get().beanForType(AddonsManager.class);
                if (response != null && response.getStatusLine().getStatusCode()
                        == HttpStatus.SC_OK) {
                    returnedInfo = EntityUtils.toString(response.getEntity());
                    if (!Strings.isNullOrEmpty(returnedInfo)) {
                        VersionRestResult vrr = parseVersionRestResult(returnedInfo);
                        if (vrr != null && !Strings.isNullOrEmpty(vrr.version) &&
                                (compareVersion(vrr.version, VERSION_FOUR_ONE) >= 0) && addonsManager.isProLicensed(
                                vrr.license)) {
                            return true;
                        } else {
                            log.debug("Unsupported version: {}.", vrr);
                        }
                    }
                }
            } catch (IOException | IllegalArgumentException e) {
                log.debug("Checking whether remote instance is an artifactory, has failed: {}.",
                        e.getMessage()
                );
            }
        }
        return false;
    }

    /**
     * Performs version compare
     *
     * @param version1
     * @param version2
     *
     * @exception  NumberFormatException if the {@code Version}
     *             does not contain a parsable {@code int}.
     *
     * @return int (-1/0/1)
     */
    private int compareVersion(String version1, String version2) {

        // used for development env. only!
        if (version1.endsWith(SNAPSHOT)) {
            log.warn("Found development version, assuming equal versions");
            return 0;
        }

        String[] arrLeft = version1.split("\\.");
        String[] arrRight = version2.split("\\.");

        int i=0;
        while(i<arrLeft.length || i<arrRight.length){
            if(i<arrLeft.length && i<arrRight.length){
                if(Integer.parseInt(arrLeft[i]) < Integer.parseInt(arrRight[i])){
                    return -1;
                }else if(Integer.parseInt(arrLeft[i]) > Integer.parseInt(arrRight[i])){
                    return 1;
                }
            } else if(i<arrLeft.length){
                if(Integer.parseInt(arrLeft[i]) != 0){
                    return 1;
                }
            } else if(i<arrRight.length){
                if(Integer.parseInt(arrRight[i]) != 0){
                    return -1;
                }
            }

            i++;
        }

        return 0;
    }

    /**
     * Produces url to be used against target host
     * @param uri original URI
     * @param inArtifactoryContext if application resides under
     *                             /artifactory path
     *
     * @return url to be used
     */
    private String produceTestUrl(URI uri, boolean inArtifactoryContext) {
        return new StringBuilder()
                .append(uri.getScheme())
                .append("://")
                .append(uri.getHost())
                .append(uri.getPort() != -1 ?
                        ":" + uri.getPort()
                        :
                        ""
                )
                .append(inArtifactoryContext ? ARTIFACTORY_APP_PATH : "")
                .append(ARTIFACTORY_SYSTEM_VERSION_PATH)
                .toString();
    }

    /**
     * Produces CloseableHttpClient
     *
     * @return CloseableHttpClient
     */
    private CloseableHttpClient getDefaultHttpClient() {
        CloseableHttpClient client;

        //ProxyDescriptor proxy =
        //        InternalContextHelper.get().getCentralConfig()
        //                .getDescriptor().getDefaultProxy();

        // TODO: [MP] generate client outside and pass it here rather than
        //            creating it manually as it requires adding artifactory-core
        //            dependency (for InternalContextHelper) to ../open/web/rest-common/pom.xml

        client = new HttpClientConfigurator()
                .soTimeout(15000)
                .connectionTimeout(1500)
                .retry(0, false)
                //.proxy(proxy)
                .getClient();

        return client;
    }

    /**
     * Creates a JSON parser for the given bytes to parse
     *
     * @param bytesToParse Byte[] to parse
     * @return JSON Parser
     * @throws java.io.IOException
     */
    private JsonParser getJsonParser(byte[] bytesToParse) throws IOException {
        return JacksonFactory.createJsonParser(bytesToParse);
    }

    /**
     * Creates a Jackson object mapper
     *
     * @return Object mapper
     */
    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Unmarshals VersionRestResult from string response
     *
     * @param versionRestResult
     * @return
     * @throws IOException
     */
    private VersionRestResult parseVersionRestResult(String versionRestResult) throws IOException {
        return getObjectMapper().readValue(getJsonParser(versionRestResult.getBytes()), new TypeReference<VersionRestResult>() {
        });
    }
}
