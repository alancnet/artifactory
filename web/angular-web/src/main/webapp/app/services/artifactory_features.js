
// For debugging only:
window._aolSimulate = function (value) {
    localStorage._aol = value ? "true" : "false";
}
window._aolOff = function () {
    delete localStorage._aol;
}
window._licenseSimulate = function (value) {
    localStorage._license = value;
}
window._licenseOff = function () {
    delete localStorage._license;
}

// Order of license levels
const LICENSES_LEVELS = {
    'OSS': 1,
    'PRO': 2,
    'ENT': 3
}

// Minimum license needed per feature
export const FEATURES = {
    // This is the default for all other features:
    'default': {
        license: 'OSS'
    },

    // features:
    'stash': {
        license: 'PRO',
        label: 'Smart search',
        path: 'search'
    },

    'properties': {
        license: 'PRO',
        label: 'Properties',
        path: 'properties'
    },
    'builds': {
        license: 'PRO',
        label: 'Builds',
        path: 'build'
    },

    'watches': {
        license: 'PRO',
        label: 'Watches',
        path: 'watches'
    },
    'diff': {
        license: 'PRO',
        label: 'Build Diff',
        path: 'build'
    },

    'licenses': {
        license: 'PRO',
        label: 'Licenses',
        path: 'license'
    },
    'blackduck': {
        license: 'PRO',
        label: 'governance',
        path: 'blackduck'
    },
    'publishedmodule': {
        license: 'PRO',
        label: 'Published Module',
        path: 'build'
    },

    'highavailability': {
        license: 'ENT',
        label: 'High Availability',
        path: 'ha'
    },
    'crowd': {
        license: 'PRO',
        label: 'Crowd',
        path: 'sso'
    },
    'samlsso': {
        license: 'PRO',
        label: 'Saml & SSO',
        path: 'sso'
    },
    'httpsso': {
        license: 'PRO',
        label: 'Http SSO',
        path: 'sso'
    },
    'signingkeys': {
        license: 'PRO',
        label: 'Signing Keys & WebStart',
        path: 'webstart'
    },

    'replications': {
        license: 'PRO',
        label: 'Replications',
        path: 'replications'
    },

    // repo types:
    'nuget': {
        license: 'PRO',
        label: 'NuGet',
        path: 'nuget'
    },
    'gems': {
        license: 'PRO',
        label: 'Gems',
        path: 'gems'
    },
    'ldap': {
        license: 'PRO',
        label: 'LDAP Groups',
        path: 'ldap'
    },
    'npm': {
        license: 'PRO',
        label: 'Npm',
        path: 'npm'
    },
    'bower': {
        license: 'PRO',
        label: 'Bower',
        path: 'bower'
    },
    'debian': {
        license: 'PRO',
        label: 'Debian',
        path: 'debian'
    },
    'pypi': {
        license: 'PRO',
        label: 'pypi',
        path: 'pypi'
    },
    'docker': {
        license: 'PRO',
        label: 'Docker',
        path: 'docker'
    },
    'vagrant': {
        license: 'PRO',
        label: 'Vagrant',
        path: 'vagrant'
    },
    'gitlfs': {
        license: 'PRO',
        label: 'GitLfs',
        path: 'gitlfs'
    },
    'yum': {
        license: 'PRO',
        label: 'Yum',
        path: 'yum'
    },
    'vcs': {
        license: 'PRO',
        label: 'VCS',
        path: 'vcs'
    },
    'register_pro': {
        license: 'PRO',
        label: 'Register Pro',
        path: 'register pro'
    },
    'p2': {
        license: 'PRO',
        label: 'P2',
        path: 'p2'
    }
};

// Features that are hidden for AOL
export const HIDDEN_AOL_FEATURES = [
    'backups',
    'highavailability',
    'httpsso',
    'proxies',
    'register_pro',
    'indexer',
    'services',
    'systeminfo',
    'maintenance',
    'configdescriptor',
    'securitydescriptor',
    'system',
    'mail'
];

// Features that are hidden for OSS
export const HIDDEN_OSS_FEATURES = [
    'register_pro'
];

// Service for accessing allowed features and licenses
export class ArtifactoryFeatures {
    constructor(FooterDao) {
        this.footerDao = FooterDao;
    }

    getAllowedLicense(featureName) {
        featureName = featureName && featureName.toLowerCase();
        let feature = FEATURES[featureName] || FEATURES['default'];
        return feature.license;
    }

    isEnabled(feature) {
        if (!feature) {
            return true;
        }
        let allowedLicense = this.getAllowedLicense(feature);
        let currentLicense = this.getCurrentLicense();
        return LICENSES_LEVELS[currentLicense] >= LICENSES_LEVELS[allowedLicense];
    }

    isDisabled(feature) {
        return !this.isEnabled(feature);
    }

    isHidden(feature) {
        if (!feature) {
            return false;
        }
        feature = feature.toLowerCase();
        return (this.isAol() && _.contains(HIDDEN_AOL_FEATURES, feature)) ||
               (this.isOss() && _.contains(HIDDEN_OSS_FEATURES, feature));
    }


    isVisible(feature) {
        return !this.isHidden(feature);
    }

    isAol() {
        if (localStorage._aol != undefined) {
            return localStorage._aol === "true";
        } // For debugging only
        return this.footerDao.getInfo().isAol;
    }

    isGlobalRepoEnabled() {
        return this.footerDao.getInfo().globalRepoEnabled;
    }

    getCurrentLicense() {
        if (localStorage._license != undefined) {
            return localStorage._license;
        } // For debugging only
        return this.footerDao.getInfo().versionID;
    }

    isOss() {
        return this.getCurrentLicense() == 'OSS';
    }

    getFeatureName(feature) {
        feature = feature && feature.toLowerCase();
        return FEATURES[feature].label;
    }

    getFeatureLink(feature) {
        feature = feature && feature.toLowerCase();
        if (FEATURES[feature] && FEATURES[feature].path) {
            return `http://service.jfrog.org/artifactory/addons/info/${ FEATURES[feature].path }`;
        }
    }
}
