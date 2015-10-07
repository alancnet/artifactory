package org.artifactory.ui.utils;

import java.util.regex.Pattern;

/**
 * @author Chen Keinan
 */
public class RegExUtils {
    private static final Pattern GIT_URL = Pattern.compile(
            "(?:https?:\\/\\/|git(?::\\/\\/|@))(gist.github.com|github.com)[:\\/](.*?)(?:.git)?$");

    public static final Pattern LOCAL_REPO_REINDEX_PATTERN = Pattern.compile("Gems|Npm|Bower|NuGet|Debian|YUM|Pypi");
    public static final Pattern REMOTE_REPO_REINDEX_PATTERN = Pattern.compile("Bower|NuGet");
    public static final Pattern VIRTUAL_REPO_REINDEX_PATTERN = Pattern.compile("Gems|Npm|Bower|NuGet");

}
