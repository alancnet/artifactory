package org.artifactory.security;

import org.apache.commons.io.FileUtils;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.security.interceptor.StoragePropertiesEncryptInterceptor;
import org.artifactory.storage.StorageProperties;
import org.artifactory.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Gidi Shabat
 */
@Test
public class StoragePropertiesEncryptInterceptorTest {

    /**
     * Emulates Artifactory environment
     */
    @BeforeClass
    public void init() throws IOException {
        //TODO: [by YS] We need to add functionality to the ArtifactoryHomeBoundTest to support storage.properties
        // Create Artifactory home and bind it to the thread
        File home = new File("target/test/StoragePropertiesEncryptInterceptorTest");
        ArtifactoryHome.bind(new ArtifactoryHome(home));
        // Create mock ArtifactoryContext using Proxy and invocation handler
        TestInvocationHandler handler = new TestInvocationHandler();
        ArtifactoryContext context = proxy(ArtifactoryContext.class, handler);
        ArtifactoryContextThreadBinder.bind(context);
        File storageProperties = ResourceUtils.getResourceAsFile("/org/artifactory/security/storage.properties");
        FileUtils.copyFile(storageProperties, ArtifactoryHome.get().getStoragePropertiesFile());
        String keyFileLocation = ConstantValues.securityMasterKeyLocation.getString();
        File keyFile = new File(ArtifactoryHome.get().getEtcDir(), keyFileLocation);
        //noinspection ResultOfMethodCallIgnored
        keyFile.delete();
        CryptoHelper.createMasterKeyFile();
    }

    @Test()
    public void encryptTest() throws IOException {
        StoragePropertiesEncryptInterceptor interceptor = new StoragePropertiesEncryptInterceptor();
        interceptor.encryptOrDecryptStoragePropertiesFile(true);
        StorageProperties storageProperties = new StorageProperties(ArtifactoryHome.get().getStoragePropertiesFile());
        // Check the password
        String password = storageProperties.getProperty("password", null);
        Assert.assertNotEquals(password, "test1");
        password = storageProperties.getPassword();
        Assert.assertEquals(password, "test1");
        // Check the credentials
        String credentials = storageProperties.getProperty("binary.provider.s3.credential", null);
        Assert.assertNotEquals(credentials, "test2");
        credentials = storageProperties.getS3Credential();
        Assert.assertEquals(credentials, "test2");
        // Check proxy credential
        String proxyCredentials = storageProperties.getProperty("binary.provider.s3.proxy.credential", null);
        Assert.assertNotEquals(proxyCredentials, "test3");
        proxyCredentials = storageProperties.getS3ProxyCredential();
        Assert.assertEquals(proxyCredentials, "test3");
        // Check the url
        String url = storageProperties.getConnectionUrl();
        Assert.assertEquals(url, "jdbc:derby:{db.home};create=true");
    }

    @Test(dependsOnMethods = "encryptTest")
    public void decryptTest() throws IOException {
        StoragePropertiesEncryptInterceptor interceptor = new StoragePropertiesEncryptInterceptor();
        interceptor.encryptOrDecryptStoragePropertiesFile(false);
        StorageProperties storageProperties = new StorageProperties(ArtifactoryHome.get().getStoragePropertiesFile());
        // Check the password
        String password = storageProperties.getProperty("password", null);
        Assert.assertEquals(password, "test1");
        // Check the credentials
        String credentials = storageProperties.getProperty("binary.provider.s3.credential", null);
        Assert.assertEquals(credentials, "test2");
        // Check the proxy credentials
        String proxyCredentials = storageProperties.getProperty("binary.provider.s3.proxy.credential", null);
        Assert.assertEquals(proxyCredentials, "test3");
        // Check the url
        String url = storageProperties.getConnectionUrl();
        Assert.assertEquals(url, "jdbc:derby:{db.home};create=true");
    }

    public class TestInvocationHandler implements InvocationHandler {
        public int count = 0;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getArtifactoryHome".equals(method.getName())) {
                return ArtifactoryHome.get();
            }
            if ("beanForType".equals(method.getName()) && ((Class) args[0]).getName().equals(
                    StorageProperties.class.getName())) {
                return new StorageProperties(ArtifactoryHome.get().getStoragePropertiesFile());
            }
            throw new IllegalStateException("The state is not expected in this test: " + method.getName());
        }
    }

    public static <T> T proxy(Class<T> interfaceClass, InvocationHandler handler) {
        Class[] interfaces = {interfaceClass};
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
    }
}
