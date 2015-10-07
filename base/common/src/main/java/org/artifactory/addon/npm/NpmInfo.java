package org.artifactory.addon.npm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chen Keinan
 */
public class NpmInfo {

    private static final Pattern GIT_URL = Pattern.compile(
            "(?:https?:\\/\\/|git(?::\\/\\/|@))(gist.github.com|github.com)[:\\/](.*?)(?:.git)?$");

    private String name;
    private String version;
    private String license;
    private String keywords;
    private String description;
    private String repository;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        if (repository != null) {
            Matcher matcher = GIT_URL.matcher(repository);
            if (matcher.matches()) {
                repository = "https://" + matcher.group(1) + "/" + matcher.group(2);
            }
            this.repository = repository;
        }
    }
}
