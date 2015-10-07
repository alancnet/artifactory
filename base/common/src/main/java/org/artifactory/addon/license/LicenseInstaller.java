/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.addon.license;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.api.context.ContextHelper;

import static org.artifactory.addon.license.VerificationResult.valid;

/**
 * @author Yoav Luft
 */
public class LicenseInstaller {

    public static final String NOT_SUPPORT_FOR_SWITCHING_MODE_ON_RUNTIME = "Changing Artifactory mode to offline" +
            " since Artifactory doesn't allow to switch its mode during run time. Please restart the server";
    public static final String SUCCESSFULLY_INSTALL = "The license has been successfully installed.";

    private final AddonsManager addonsManager;

    public LicenseInstaller() {
        this(ContextHelper.get().beanForType(AddonsManager.class));
    }

    public LicenseInstaller(AddonsManager addonsManager) {
        this.addonsManager = addonsManager;
    }

    public interface LicenseInstallCallback {
        void handleSuccess();

        void switchOffline(String message);

        void handleError(String message);
    }

    public void install(String licenseKey, LicenseInstallCallback callback) {
        ArtifactoryRunningMode oldRunningMode = addonsManager.getArtifactoryRunningMode();
        VerificationResult result;
        try {
            result = addonsManager.installLicense(licenseKey);
        } catch (UnsupportedOperationException e) {
            callback.handleError("Cannot install license on OSS installation.");
            return;
        }
        ArtifactoryRunningMode newRunningMode = addonsManager.getArtifactoryRunningMode();
        if (result == valid) {
            boolean sameMode = ArtifactoryRunningMode.sameMode(oldRunningMode, newRunningMode);
            if (sameMode) {
                callback.handleSuccess();
            } else {
                String message = SUCCESSFULLY_INSTALL + " " + NOT_SUPPORT_FOR_SWITCHING_MODE_ON_RUNTIME;
                callback.switchOffline(message);
            }
        } else {
            callback.handleError("License could not be installed due to an error: " + result);
        }
    }
}
