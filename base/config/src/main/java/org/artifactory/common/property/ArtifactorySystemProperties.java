/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.common.property;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.common.ConstantValues;
import org.artifactory.log.BootstrapLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author yoavl
 */
public class ArtifactorySystemProperties {

    private static final ImmutableMap<String, PropertyMapper> DEPRECATED =
            ImmutableMap.<String, PropertyMapper>builder()
                    .put("artifactory.authenticationCacheIdleTimeSecs",
                            new SamePropertyMapper("artifactory.authentication.cache.idleTimeSecs"))
                    .put("artifactory.maven.suppressPomConsistencyChecks", new NullPropertyMapper())
                    .put("artifactory.metadataCacheIdleTimeSecs", new NullPropertyMapper())
                    .put("artifactory.logs.refreshrate.secs",
                            new SamePropertyMapper("artifactory.logs.viewRefreshRateSecs"))
                    .put("artifactory.spring.configPath", new SamePropertyMapper("artifactory.spring.configDir"))
                    .put("artifactory.lockTimeoutSecs", new SamePropertyMapper("artifactory.locks.timeoutSecs"))
                    .put("artifactory.xmlAdditionalMimeTypeExtensions", new NullPropertyMapper())
                    .put("repo.cleanup.intervalHours", new NullPropertyMapper())
                    .put("bintray.system.user", new MovedToConfigPropertyMapper
                            ("The bintray config section in the admin page"))
                    .put("bintray.system.api.key", new MovedToConfigPropertyMapper
                            ("The bintray config section in the admin page"))
                    .build();
    /**
     * The combined properties of System, artifactory.system.properties file and artifactory.properties file. All system
     * properties starting with 'artifactory.' will be included.
     */
    private Properties nonEnumArtifactoryProperties = new Properties();
    private EnumMap<ConstantValues, String> artifactoryProperties = new EnumMap<>(ConstantValues.class);

    /**
     * Caching parseLong values from the properties above
     */
    private EnumMap<ConstantValues, Long> artifactoryLongProperties = new EnumMap<>(ConstantValues.class);
    private EnumMap<ConstantValues, Boolean> artifactoryBooleanProperties = new EnumMap<>(ConstantValues.class);
    /**
     * A map of substitute repo keys (oldKey:newKey) for supporting old repository keys (that are invalid xml ids).
     */
    private Map<String, String> substituteRepoKeys = new HashMap<>();

    private static void loadSystemProperties(Properties result) {
        Properties properties = System.getProperties();
        for (Object key : properties.keySet()) {
            String sKey = (String) key;
            if (sKey.startsWith(ConstantValues.SYS_PROP_PREFIX)) {
                result.put(sKey, properties.getProperty(sKey));
            }
        }
    }

    private static void validateConstants(Properties artProps) {
        String chrootPropertyName = ConstantValues.uiChroot.getPropertyName();
        String chroot = (String) artProps.get(chrootPropertyName);
        if (StringUtils.isNotBlank(chroot)) {
            if (!new File(chroot).exists()) {
                artProps.remove(chrootPropertyName);
                BootstrapLogger.error("Selected chroot '" + chroot + "' does not exist. Ignoring property value!");
            }
        }
    }

    private static void handleDeprecatedProps(Properties artProps) {
        //Test the deprecated props against the current props
        Properties autoReplaced = new Properties();
        for (Object key : artProps.keySet()) {
            String prop = (String) key;
            if (DEPRECATED.containsKey(prop)) {
                PropertyMapper mapper = DEPRECATED.get(prop);
                String newProp = mapper.getNewPropertyName();
                String suggestion = newProp == null ? "this property is no longer in use." :
                        "please use: '" + newProp + "' instead.";
                BootstrapLogger.warn(
                        "Usage of deprecated artifactory system property detected: '" + prop + "' - " + suggestion);
                //Check if property can be automatically replaced
                String value = (String) artProps.get(prop);
                String newValue = mapper.map(value);
                if (newProp == null) {
                    newProp = prop;
                }
                if (newValue != null) {
                    autoReplaced.put(newProp, newValue);
                    BootstrapLogger.warn(
                            "Deprecated artifactory system property '" + prop + "=" + value + "' auto-replaced with '" +
                                    newProp + "=" + newValue + "'.");
                }
            }
        }
        artProps.putAll(autoReplaced);
    }

    private static Map<String, String> fillRepoKeySubstitute(Properties artProps) {
        Map<String, String> result = new HashMap<>();
        String prefix = ConstantValues.substituteRepoKeys.getPropertyName();
        for (Object o : artProps.keySet()) {
            String key = (String) o;
            if (key.startsWith(prefix)) {
                String oldRepoKey = key.substring(prefix.length());
                String newRepoKey = (String) artProps.get(key);
                result.put(oldRepoKey, newRepoKey);
            }
        }
        // Remove the keys used
        for (String key : result.keySet()) {
            artProps.remove(prefix + key);
        }
        return result;
    }

    public String getProperty(ConstantValues key) {
        String result = artifactoryProperties.get(key);
        if (result == null) {
            return key.getDefValue();
        }
        return result;
    }

    public boolean hasProperty(ConstantValues key) {
        return artifactoryProperties.containsKey(key);
    }

    public String getProperty(String key, @Nullable String defaultValue) {
        return nonEnumArtifactoryProperties.getProperty(key, defaultValue);
    }

    public Long getLongProperty(ConstantValues key) {
        Long result = artifactoryLongProperties.get(key);
        if (result == null) {
            String strValue = getProperty(key);
            if (strValue == null) {
                result = 0l;
            } else {
                result = Long.parseLong(strValue.trim());
            }
            artifactoryLongProperties.put(key, result);
        }
        return result;
    }

    public Boolean getBooleanProperty(ConstantValues key) {
        Boolean result = artifactoryBooleanProperties.get(key);
        if (result == null) {
            String strValue = getProperty(key);
            if (strValue == null) {
                result = Boolean.FALSE;
            } else {
                result = Boolean.parseBoolean(strValue);
            }
            artifactoryBooleanProperties.put(key, result);
        }
        return result;
    }

    public Map<String, String> getSubstituteRepoKeys() {
        return substituteRepoKeys;
    }

    public void loadArtifactorySystemProperties(File systemPropertiesFile, File artifactoryPropertiesFile) {
        Properties combinedProperties = new Properties();
        if (systemPropertiesFile != null && systemPropertiesFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(systemPropertiesFile);
                combinedProperties.load(fis);
            } catch (Exception e) {
                throw new RuntimeException("Could not read default system properties from '" +
                        systemPropertiesFile.getAbsolutePath() + "'.", e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }

        // load artifactory.properties (version and revision properties)
        if (artifactoryPropertiesFile != null && artifactoryPropertiesFile.exists()) {
            FileInputStream fis = null;
            try {
                // Load from file than override from the system props
                fis = new FileInputStream(artifactoryPropertiesFile);
                combinedProperties.load(fis);
            } catch (Exception e) {
                throw new RuntimeException("Could not read artifactory.properties from '" +
                        artifactoryPropertiesFile.getAbsolutePath() + "'.", e);
            } finally {
                IOUtils.closeQuietly(fis);
            }
        }

        // Override with System properties
        loadSystemProperties(combinedProperties);

        Set<String> setAsSystemProp = new HashSet<>();
        //Cleanup all non-artifactory system properties and set them as system properties
        for (Object key : combinedProperties.keySet()) {
            String propName = (String) key;
            String propValue = combinedProperties.getProperty(propName);
            if (!propName.startsWith(ConstantValues.SYS_PROP_PREFIX)) {
                // TODO: mainly for derby db properties, find another way of doing it
                System.setProperty(propName, propValue);
                setAsSystemProp.add(propName);
            }
        }
        for (String key : setAsSystemProp) {
            combinedProperties.remove(key);
        }

        substituteRepoKeys = fillRepoKeySubstitute(combinedProperties);

        //Test for deprecated properties and warn
        handleDeprecatedProps(combinedProperties);

        validateConstants(combinedProperties);

        // Use the EnumMap as much as possible
        EnumMap<ConstantValues, String> newArtifactoryProperties = new EnumMap<>(ConstantValues.class);
        for (ConstantValues constVal : ConstantValues.values()) {
            Object val = combinedProperties.remove(constVal.getPropertyName());
            if (val != null) {
                newArtifactoryProperties.put(constVal, (String) val);
            }
        }
        artifactoryProperties = newArtifactoryProperties;
        // TODO: Print a message when combined props is not empty as this should not happen.
        // It's probably a typo! But it's used for special security access values not declared in ConstantValues
        nonEnumArtifactoryProperties = combinedProperties;
        artifactoryBooleanProperties.clear();
        artifactoryLongProperties.clear();
    }

    /**
     * Returns the propertiesCopy object
     *
     * @return Properties - The copy of the artifactoryProperties object
     */
    public Properties getPropertiesCopy() {
        Properties propertiesCopy = new Properties();
        for (Map.Entry<ConstantValues, String> entry : artifactoryProperties.entrySet()) {
            propertiesCopy.put(entry.getKey().getPropertyName(), entry.getValue());
        }
        propertiesCopy.putAll(nonEnumArtifactoryProperties);
        return propertiesCopy;
    }

    /**
     * Set the value of the given key
     *
     * @param propertyName Property key
     * @param value        Property value
     */
    public void setProperty(String propertyName, String value) {
        ConstantValues key = null;
        for (ConstantValues val : ConstantValues.values()) {
            if (val.getPropertyName().equals(propertyName)) {
                key = val;
                break;
            }
        }
        if (key != null) {
            artifactoryProperties.put(key, value);
            //Update the caches too. Only ConstantValues have a cache entry
            if (artifactoryLongProperties.containsKey(key)) {
                artifactoryLongProperties.put(key, Long.valueOf(value));
            }
            if (artifactoryBooleanProperties.containsKey(key)) {
                artifactoryBooleanProperties.put(key, Boolean.valueOf(value));
            }
        } else {
            nonEnumArtifactoryProperties.put(propertyName, value);
        }
    }

    /**
     * Removes the value of the given key
     *
     * @param key Property constant values enum entry
     * @return the prev value or null
     */
    public String removeProperty(ConstantValues key) {
        artifactoryBooleanProperties.remove(key);
        artifactoryLongProperties.remove(key);
        return artifactoryProperties.remove(key);
    }

}
