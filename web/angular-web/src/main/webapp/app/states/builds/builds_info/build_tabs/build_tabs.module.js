import {jfGeneralInfo}                    from './jf_general_info';
import {jfPublishedModules}                    from './jf_published_modules';
import {jfBuildInfoJson}                    from './jf_build_info_json';
import {jfEnvironment}                    from './jf_environment';
import {jfIssues}                    from './jf_issues';
import {jfDiff}                    from './jf_diff';
import {jfReleaseHistory}                    from './jf_release_history';
import {jfLicenses}                    from './jf_licenses';
import {jfBuildsGovernance}                    from './jf_builds_governance';

export default angular.module('buildTabs', [])
        .directive({
            'jfGeneralInfo': jfGeneralInfo,
            'jfPublishedModules': jfPublishedModules,
            'jfBuildInfoJson': jfBuildInfoJson,
            'jfEnvironment': jfEnvironment,
            'jfIssues': jfIssues,
            'jfDiff': jfDiff,
            'jfReleaseHistory': jfReleaseHistory,
            'jfLicenses': jfLicenses,
            'jfBuildsGovernance': jfBuildsGovernance
    });