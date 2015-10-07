package org.artifactory.version.converter.v160;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Shay Yaakov
 */
@Test
public class SingleRepoTypeConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config-1.5.13-multi_repo_type.xml", new SingleRepoTypeConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        validateLocalRepos(rootElement, namespace);
        validateRemoteRepos(rootElement, namespace);
        validateVirtualRepos(rootElement, namespace);
    }

    private void validateLocalRepos(Element rootElement, Namespace namespace) {
        Element localRepos = rootElement.getChild("localRepositories", namespace);
        for (Element repo : localRepos.getChildren()) {
            switch (repo.getChildText("key", namespace)) {
                case "maven-local":
                    assertEquals(repo.getChildText("type", namespace), "maven");
                    break;
                case "gradle-local":
                    assertEquals(repo.getChildText("type", namespace), "gradle");
                    break;
                case "ivy-local":
                    assertEquals(repo.getChildText("type", namespace), "ivy");
                    break;
                case "npm-local":
                    assertEquals(repo.getChildText("type", namespace), "npm");
                    break;
                case "bower-local":
                    assertEquals(repo.getChildText("type", namespace), "bower");
                    break;
                case "docker-local":
                    assertEquals(repo.getChildText("type", namespace), "docker");
                    break;
                case "vagrant-local":
                    assertEquals(repo.getChildText("type", namespace), "vagrant");
                    break;
                case "gems-local":
                    assertEquals(repo.getChildText("type", namespace), "gems");
                    break;
                case "debian-local":
                    assertEquals(repo.getChildText("type", namespace), "debian");
                    break;
                case "gitlfs-local":
                    assertEquals(repo.getChildText("type", namespace), "gitlfs");
                    break;
                case "nuget-local":
                    assertEquals(repo.getChildText("type", namespace), "nuget");
            }
            assertNull(repo.getChildText("enableNuGetSupport", namespace));
            assertNull(repo.getChildText("enableGemsSupport", namespace));
            assertNull(repo.getChildText("enableNpmSupport", namespace));
            assertNull(repo.getChildText("enableBowerSupport", namespace));
            assertNull(repo.getChildText("enableDebianSupport", namespace));
            assertNull(repo.getChildText("enablePypiSupport", namespace));
            assertNull(repo.getChildText("enableDockerSupport", namespace));
            assertNull(repo.getChildText("enableVagrantSupport", namespace));
            assertNull(repo.getChildText("enableGitLfsSupport", namespace));
        }
    }

    private void validateRemoteRepos(Element rootElement, Namespace namespace) {
        Element localRepos = rootElement.getChild("remoteRepositories", namespace);
        for (Element repo : localRepos.getChildren()) {
            switch (repo.getChildText("key", namespace)) {
                case "p2-remote":
                    assertEquals(repo.getChildText("type", namespace), "maven");
                    break;
                case "vcs-remote":
                    assertEquals(repo.getChildText("type", namespace), "vcs");
                    assertNull(repo.getChildText("enableVcsSupport", namespace));
                    break;
            }
        }
    }

    private void validateVirtualRepos(Element rootElement, Namespace namespace) {
        Element virtualRepos = rootElement.getChild("virtualRepositories", namespace);
        for (Element repo : virtualRepos.getChildren()) {
            switch (repo.getChildText("key", namespace)) {
                case "p2-virtual":
                    assertEquals(repo.getChildText("type", namespace), "p2");
                    break;
                case "pypi-virtual":
                    assertEquals(repo.getChildText("type", namespace), "pypi");
                    break;
            }
            assertNull(repo.getChild("p2", namespace).getChildText("enabled", namespace));
        }
    }
}