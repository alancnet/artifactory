package org.artifactory.version.converter.v160;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class SuppressConsitencyConverter implements XmlConverter {
    private static final Logger log = LoggerFactory.getLogger(SingleRepoTypeConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Finished Converting repositories to a suppress consistency");

        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        Element localRepos = rootElement.getChild("localRepositories", namespace);
        if (localRepos != null) {
            convertLocalRepos(localRepos.getChildren());
        }

        Element remoteRepos = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepos != null) {
            convertRemoteRepos(remoteRepos.getChildren());
        }


        log.info("Finished Converting repositories to a suppress consistency");
    }

    private void convertLocalRepos(List<Element> repos) {
        if (repos == null || repos.isEmpty()) {
            return;
        }

        for (Element repo : repos) {
            convertSuppressConsistency(repo);
        }
    }

    private void convertRemoteRepos(List<Element> repos) {
        if (repos == null || repos.isEmpty()) {
            return;
        }

        for (Element repo : repos) {
            convertSuppressConsistency(repo);
        }
    }

    private void convertSuppressConsistency(Element repo) {
        Element type = repo.getChild("type", repo.getNamespace());
        RepoType repoType = RepoType.fromType(type.getText());
        if (repoType.isMavenGroup()) {
            Element suppress= repo.getChild("suppressPomConsistencyChecks", repo.getNamespace());
            String pomConsistencyValue = repo.getChildText("suppressPomConsistencyChecks", repo.getNamespace());
            if (suppress==null ){
                suppress=new Element("suppressPomConsistencyChecks", repo.getNamespace());
                int lastLocation = findSuppressLocation(repo);
                repo.addContent(lastLocation + 1, new Text("\n            "));
                repo.addContent(lastLocation + 2, suppress);
            }
            if(StringUtils.isBlank(suppress.getText())) {
                pomConsistencyValue = "false";
            }
            suppress.setText(pomConsistencyValue);
        }
    }

    private int findSuppressLocation(Element repo) {
        return findLastLocation(repo, "maxUniqueSnapshots",
                        "handleSnapshots",
                        "handleReleases",
                        "blackedOut",
                        "dockerApiVersion",
                        "repoLayoutRef",
                        "excludesPattern",
                        "includesPattern",
                        "notes",
                        "description",
                        "type");
    }

    private int findLastLocation(Element parent, String... elements) {
        for (int i = 0; i < elements.length; i++) {
            Element child = parent.getChild(elements[i], parent.getNamespace());
            if (child != null) {
                return parent.indexOf(child);
            }
        }
        return -1;
    }

}
