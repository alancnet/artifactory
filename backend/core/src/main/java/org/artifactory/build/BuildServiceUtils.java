package org.artifactory.build;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.fs.FileInfo;
import org.jfrog.build.api.Artifact;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.BuildFileBean;
import org.jfrog.build.api.Dependency;
import org.jfrog.build.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dan Feldman
 */
public class BuildServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(BuildServiceUtils.class);

    public static Iterable<FileInfo> filterOutNullFileInfos(Iterable<FileInfo> rawInfos) {
        return Iterables.filter(rawInfos, filterNullFileInfos);
    }

    private static Predicate<FileInfo> filterNullFileInfos = new Predicate<FileInfo>() {
        @Override
        public boolean apply(@Nullable FileInfo input) {
            return input != null;
        }
    };

    public static Iterable<FileInfo> toFileInfoList(Set<ArtifactoryBuildArtifact> artifacts) {
        return Iterables.transform(artifacts, new Function<ArtifactoryBuildArtifact, FileInfo>() {
            @Nullable
            @Override
            public FileInfo apply(@Nullable ArtifactoryBuildArtifact input) {
                return input != null ? input.getFileInfo() : null;
            }
        });
    }

    /**
     * Makes a best effort to match an artifact exactly by name and also by the path's filename.
     * If not exact use artifact name match as next best bet.
     * Optionally removes matched artifacts from the map to help next searches.
     */
    public static FileInfo findArtifactFileInfoInSet(Set<ArtifactoryBuildArtifact> artifactsToSearch,
            final Artifact artifactToFind, boolean eliminateMatches) {

        ArtifactoryBuildArtifact match = Iterables.find(artifactsToSearch,
                new Predicate<ArtifactoryBuildArtifact>() {
                    @Override
                    public boolean apply(@Nullable ArtifactoryBuildArtifact input) {
                        return artifactToFind != null && input != null
                                && artifactToFind.getName().equals(input.getArtifact().getName())
                                && artifactToFind.getSha1().equals(input.getArtifact().getSha1());
                    }
                }, null);
        if (eliminateMatches && match != null) {
            artifactsToSearch.remove(match);
        }
        return match != null ? match.getFileInfo() : null;
    }

    /**
     * Map all build dependencies to checksum, held in a multimap for occurrences of duplicate checksum for different
     * dependencies --> although we cannot be 100% positive which dependency took part in the build with the current
     * BuildInfo implementation.
     */
    public static Multimap<String, org.jfrog.build.api.Dependency> getBuildDependencies(Build build) {
        Multimap<String, org.jfrog.build.api.Dependency> beansMap = HashMultimap.create();
        List<org.jfrog.build.api.Module> modules = build.getModules();
        if (modules == null) {
            return beansMap;
        }
        for (org.jfrog.build.api.Module module : modules) {
            if (module.getDependencies() != null) {
                for (org.jfrog.build.api.Dependency dependency : module.getDependencies()) {
                    if (dependency.getSha1() != null) {
                        beansMap.put(dependency.getSha1(), dependency);
                    } else {
                        log.warn("Dependency: " + dependency.getId() + " is missing SHA1," + " under build: "
                                + build.getName());
                    }
                }
            }
        }
        return beansMap;
    }

    /**
     * Map all build artifacts to checksum, held in a multimap for occurrences of duplicate checksum for different
     * artifacts so that the search results return all
     */
    public static Multimap<String, org.jfrog.build.api.Artifact> getBuildArtifacts(Build build) {
        //ListMultiMap to hold possible duplicate artifacts coming from BuildInfo
        Multimap<String, org.jfrog.build.api.Artifact> beansMap = ArrayListMultimap.create();

        List<org.jfrog.build.api.Module> modules = build.getModules();
        if (modules == null) {
            return beansMap;
        }
        for (Module module : modules) {
            if (module.getArtifacts() != null) {
                for (Artifact artifact : module.getArtifacts()) {
                    if (artifact.getSha1() != null) {
                        beansMap.put(artifact.getSha1(), artifact);
                    } else {
                        log.warn("Artifact: " + artifact.getName() + " is missing SHA1," + " under build: "
                                + build.getName());
                    }
                }
            }
        }
        return beansMap;
    }

    public static void verifyAllArtifactInfosExistInSet(Build build, boolean cleanNullEntries,
            BasicStatusHolder statusHolder, Set<ArtifactoryBuildArtifact> buildArtifactsInfos,
            VerifierLogLevel logLevel) {
        for (Iterator<ArtifactoryBuildArtifact> iter = buildArtifactsInfos.iterator(); iter.hasNext(); ) {
            ArtifactoryBuildArtifact artifact = iter.next();
            if (artifact.getFileInfo() == null) {
                String errorMsg = "Unable to find artifact '" + artifact.getArtifact().getName() + "' of build '" + build.getName()
                        + "' #" + build.getNumber();
                logToStatus(statusHolder, errorMsg, logLevel);
                if (cleanNullEntries) {
                    iter.remove();
                }
            }
        }
    }

    /**
     * Verifies all dependencies from the build exist in the map and writes appropriate entries to the StatusHolder
     * based on the chosen log level.
     * NOTE: Relies on missing dependency to have a null mapping (as returned by {@link getBuildDependenciesFileInfos}
     *
     * @param build                 Build to verify
     * @param statusHolder          StatusHolder that entries will be written into
     * @param buildDependenciesInfo Mapping of Dependencies to FileInfos
     */
    public static void verifyAllDependencyInfosExistInMap(Build build, boolean cleanNullEntries,
            BasicStatusHolder statusHolder, Map<org.jfrog.build.api.Dependency, FileInfo> buildDependenciesInfo,
            VerifierLogLevel logLevel) {

        List<BuildFileBean> keysToRemove = Lists.newArrayList();
        for (Map.Entry<Dependency, FileInfo> entry : buildDependenciesInfo.entrySet()) {
            if (entry.getValue() == null) {
                String errorMsg = "Unable to find dependency '" + entry.getKey().getId() + "' of build '"
                        + build.getName() + "' #" + build.getNumber();
                keysToRemove.add(entry.getKey());
                logToStatus(statusHolder, errorMsg, logLevel);
            }
        }
        if (cleanNullEntries) {
            for (BuildFileBean keyToRemove : keysToRemove) {
                buildDependenciesInfo.remove(keyToRemove);
            }
        }
    }

    private static void logToStatus(BasicStatusHolder statusHolder, String errorMsg, VerifierLogLevel logLevel) {
        switch (logLevel) {
            case err:
                statusHolder.error(errorMsg, log);
                break;
            case warn:
                statusHolder.warn(errorMsg, log);
                break;
            case debug:
                statusHolder.debug(errorMsg, log);
        }
    }

    public enum VerifierLogLevel {
        err, warn, debug
    }
}