export function RepositoriesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.REPOSITORIES)
            .setCustomActions({
                getRepositories: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/:type/info',
                    isArray: true,
                    params: {type: '@repoType'}
                },
                getRepository: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/:type/:repoKey',
                    params: {type: '@repoType', repoKey: '@repoKey'}
                },
                deleteRepository: {
                    method: 'DELETE',
                    path: RESOURCE.REPOSITORIES + '/:repoKey', params: {repoKey: '@repoKey'},
                    notifications: true
                },
                getAvailableChoicesOptions: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/availablechoices'
                },
                getDefaultValues: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/defaultvalues'
                },
                repoKeyValidator: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/validatereponame'
                },
                testRemoteUrl: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testremote',
                    notifications: true
                },
                detectSmartRepository: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/isartifactory',
                },
                testLocalReplication: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testlocalreplication',
                    notifications: true
                },
                testRemoteReplication: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/testremotereplication',
                    notifications: true
                },
                executeReplicationNow: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/executereplicationnow',
                    params: {replicationUrl: '@replicationUrl', repoKey: '@repoKey'},
                    notifications: true
                },
                executeRemoteReplicationNow: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/exeucteremotereplication',
                    params: {replicationUrl: '@replicationUrl', repoKey: '@repoKey'},
                    notifications: true
                },
                runNowReplications: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/executeall', params: {repoKey: '@repoKey'},
                    notifications: true
                },
                remoteUrlToRepoMap: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/remoteUrlMap'
                },
                availableRepositoriesByType: {
                    method: 'GET',
                    params: {type: '@type', repoKey: '@repoKey'},
                    path: RESOURCE.REPOSITORIES + '/availablerepositories'
                },
                indexerAvailableRepositories: {
                    method: 'GET',
                    params: {type: '@type', layout: '@layout'},
                    path: RESOURCE.REPOSITORIES + '/indexeravailablerepositories'
                },
                getResolvedRepositories: {
                    method: 'POST',
                    isArray: true,
                    params: {type: '@repoType', repoKey: '@repoKey'},
                    path: RESOURCE.REPOSITORIES + '/resolvedrepositories'
                },
                isReplicationValid: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/validatelocalreplication',
                    notifications: true
                },
                reorderRepositories: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/:repoType/reorderrepositories',
                    params: {
                        repoType: '@repoType',
                        $no_spinner: true
                    }
                },
                createDefaultJcenterRepo: {
                    method: 'POST',
                    path: RESOURCE.REPOSITORIES + '/createdefaultjcenterrepo',
                    notifications: true,
                    params: {
                        $no_spinner: false
                    }
                },
                isJcenterRepoConfigured: {
                    method: 'GET',
                    path: RESOURCE.REPOSITORIES + '/isjcenterconfigured',
                    notifications: false,
                    params: {
                        $no_spinner: true
                    }
                }
            })
            .extendPrototype({
                isType: function (...types) {
                    return this.typeSpecific && this.typeSpecific.repoType && _.contains(types,
                                    this.typeSpecific.repoType.toLowerCase());
                },
                isGitProvider: function (gitProvider) {
                    return this.typeSpecific && this.typeSpecific.gitProvider && gitProvider == this.typeSpecific.gitProvider.toLowerCase();
                }
            })
            .getInstance();
}
