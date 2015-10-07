export const AdminMenuItems = [
    {
        "label": "Repositories",
        "state": "admin.repositories",
        "subItems": [
            {"label": "Local", "state": "admin.repositories.list", "stateParams": {"repoType": "local"}},
            {"label": "Remote", "state": "admin.repositories.list", "stateParams": {"repoType": "remote"}},
            {"label": "Virtual", "state": "admin.repositories.list", "stateParams": {"repoType": "virtual"}},
            {"label": "Layouts", "state": "admin.repositories.repo_layouts"}
        ]
    },

    {
        "label": "Configuration",
        "state": "admin.configuration",
        "subItems": [
            {"label": "General", "state": "admin.configuration.general"},
            {"label": "Licenses", "state": "admin.configuration.licenses", "feature": "licenses"},
            {"label": "Black Duck", "state": "admin.configuration.black_duck", "feature": "blackduck"},
            {"label": "Property Sets", "state": "admin.configuration.property_sets", "feature": "properties"},
            {"label": "Proxies", "state": "admin.configuration.proxies", "feature": "proxies"},
            {"label": "Mail", "state": "admin.configuration.mail", "feature": "mail"},
            {"label": "High Availability", "state": "admin.configuration.ha", "feature": "highavailability"},
            //{"label": "Bintray", "state": "admin.configuration.bintray"},
            {"label": "Register License", "state": "admin.configuration.register_pro", "feature": "register_pro"}
        ]
    },

    {
        "label": "Security",
        "state": "admin.security",
        "subItems": [
            {"label": "General", "state": "admin.security.general"},
            {"label": "Users", "state": "admin.security.users"},
            {"label": "Groups", "state": "admin.security.groups"},
            {"label": "Permissions", "state": "admin.security.permissions"},
            {"label": "LDAP", "state": "admin.security.ldap_settings"},
            {"label": "Crowd", "state": "admin.security.crowd_integration", "feature": "crowd"},
            {"label": "SAML SSO", "state": "admin.security.saml_integration", "feature": "samlsso"},
            {"label": "HTTP SSO", "state": "admin.security.http_sso", "feature": "httpsso"},
            {"label": "Signing Keys", "state": "admin.security.signing_keys", "feature": "signingkeys"}
        ]
    },

    {
        "label": "Services",
        "state": "admin.services",
        "subItems": [
            {"label": "Backups", "state": "admin.services.backups", "feature": "backups"},
            {"label": "Maven Indexer", "state": "admin.services.indexer", "feature": "indexer"}
        ]

    },

    {
        "label": "Import & Export",
        "state": "admin.import_export",
        "subItems": [
            {"label": "Repositories", "state": "admin.import_export.repositories", "feature": "repositories"},
            {"label": "System", "state": "admin.import_export.system", "feature": "system"}

        ]

    },

    {
        "label": "Advanced",
        "state": "admin.advanced",
        "subItems": [
            {"label": "System Info", "state": "admin.advanced.system_info", "feature":"systeminfo"},
            {"label": "System Logs", "state": "admin.advanced.system_logs"},
            {"label": "Maintenance", "state": "admin.advanced.maintenance", "feature":"maintenance"},
            {"label": "Storage", "state": "admin.advanced.storage_summary"},
            {"label": "Config Descriptor", "state": "admin.advanced.config_descriptor", "feature":"configdescriptor"},
            {"label": "Security Descriptor", "state": "admin.advanced.security_descriptor", "feature":"securitydescriptor"}

        ]
    }

];