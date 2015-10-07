package org.artifactory.ui.rest.service.admin.configuration.bintray;

import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.BintrayUser;
import org.artifactory.api.bintray.exception.BintrayException;
import org.artifactory.descriptor.bintray.BintrayConfigDescriptor;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TestBintrayUIService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(TestBintrayUIService.class);


    @Autowired
    private BintrayService bintrayService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BintrayConfigDescriptor bintrayConfigDescriptor = (BintrayConfigDescriptor) request.getImodel();
        Map<String, String> headersMap = getHeadersMap(request);
        // test connection to bintray
        testConnectionToBintray(response, bintrayConfigDescriptor, headersMap);
    }

    /**
     * test connection to bintray and update feedback msg
     *
     * @param artifactoryResponse     - encapsulate data require for response
     * @param bintrayConfigDescriptor - bintray config descriptor
     * @param headersMap              - header Map
     */
    private void testConnectionToBintray(RestResponse artifactoryResponse, BintrayConfigDescriptor bintrayConfigDescriptor,
                                         Map<String, String> headersMap) {
        BintrayUser bintrayUser;
        try {
            String apiKey = CryptoHelper.decryptIfNeeded(bintrayConfigDescriptor.getApiKey());
            bintrayUser = bintrayService.getBintrayUser(bintrayConfigDescriptor.getUserName(),
                    apiKey, headersMap);
            artifactoryResponse.info("Successfully authenticated '" + bintrayUser.getFullName() + "'");
        } catch (IOException e) {
            artifactoryResponse.error("Authentication failed");
        } catch (BintrayException e) {
            artifactoryResponse.error("Authentication failed");
        } catch (IllegalArgumentException e) {
            artifactoryResponse.error(e.getMessage());
        }
    }

    /**
     * get request header map
     *
     * @param artifactoryRequest - encapsulate data related to request
     * @return - map of http request header
     */
    public static Map<String, String> getHeadersMap(ArtifactoryRestRequest artifactoryRequest) {
        Map<String, String> map = new HashMap<>();
        HttpServletRequest request = artifactoryRequest.getServletRequest();
        if (request != null) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                map.put(headerName.toUpperCase(), request.getHeader(headerName));
            }
        }
        return map;
    }
}
