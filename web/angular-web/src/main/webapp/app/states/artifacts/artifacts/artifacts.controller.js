import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIPS from '../../../constants/artifact_tooltip.constant';
import ICONS from '../constants/artifact_browser_icons.constant';
import FIELD_OPTIONS from '../../../constants/field_options.constats';

export class ArtifactsController {
    constructor($scope, $stateParams, $state, $sce, ArtifactoryEventBus, ArtifactoryModal, ArtifactoryState, User, ArtifactDeployDao,
            SetMeUpDao, ArtifactoryFeatures, parseUrl, ArtifactoryDeployModal) {

        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$stateParams = $stateParams;
        this.setMeUpDao = SetMeUpDao;
        this.$state = $state;
        this.$scope = $scope;
        this.modal = ArtifactoryModal;
        this.parseUrl = parseUrl;
        this.artifactoryFeatures = ArtifactoryFeatures;
        this.node = null;
        this.selectedNode = {};
        this.modal = ArtifactoryModal;
        this.user = User.getCurrent();
        this.deployModal = ArtifactoryDeployModal;
        this.setMeUpScope = $scope.$new();
        this.artifactoryState = ArtifactoryState;
        this.tooltips = TOOLTIPS;
        this.icons = ICONS;
        this.$sce = $sce;
        this.initEvent();
        this.repoPackageTypes = FIELD_OPTIONS.repoPackageTypes.slice(0);//make a copy

        this.removeP2();
        this.removeDisabledFeatures();
    }

    removeP2(){
        for (var i = 0; i < this.repoPackageTypes.length; i++) {
            if (this.repoPackageTypes[i].value.toLowerCase() == "p2") {
                this.repoPackageTypes.splice(i, 1);
            }
        }
    }

    removeDisabledFeatures() {
        this.repoPackageTypes = _.filter(this.repoPackageTypes,
                (item) => !this.artifactoryFeatures.isDisabled(item.value));
    }

    getNodeIcon() {
        if (this.node && this.node.data) {
            let type = this.icons[this.node.data.iconType];
            if (!type) type = this.icons['default'];
            return type && type.icon;
        }
    }

    getSetMeUpData(callback) {
        this.setMeUpDao.get().$promise.then((result)=> {
            //console.log(result);
            callback(result);
        })
    }

    openSetMeUp() {
        this.initSetMeUpScope();
        this.modalInstance = this.modal.launchModal('set_me_up_modal', this.setMeUpScope);
    }

    openDeploy() {
        this.deployModal.launch(this.node);
    }

    initEvent() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_NODE_SELECT, node => this.selectNode(node));

    }

    initSetMeUpScope() {
        var setMeUpDao = this.setMeUpDao;
        this.setMeUpScope = this.$scope.$new();

        this.setMeUpScope.settingPage = false;

        this.setMeUpScope.id = this.setMeUpScope.$id
        this.setMeUpScope.$sce = this.$sce
        this.setMeUpScope.settings = {}
        this.setMeUpScope.selection = {}
        this.setMeUpScope.close = ()=>this.modalInstance.close();
        this.setMeUpScope.title = "Set Me Up";
        this.setMeUpScope.shownRepos = []
        this.setMeUpScope.deploySnippets = []
        this.setMeUpScope.readSnippets = []
        this.setMeUpScope.generalSnippets = []

        this.setMeUpScope.me = function () {
            var scope = this
            while (scope.$id != this.id && scope.$parent) {
                scope = scope.$parent
            }
            return scope
        }

        var node = this.node.data.getRoot();

        this.setMeUpScope.node = node;

        this.setMeUpScope.snippets = {
            debian: {
                read: [{
                    before: "To use Artifactory repository to install Debian package you need to add it to your <i>source.list</i> file. You can do that using the following command:",
                    snippet: "sudo sh -c \"echo 'deb $2/$1 <DISTRIBUTION> <COMPONENT>' >> /etc/apt/sources.list\""
                }, {
                    before: "For accessing Artifactory using credentials you can specify it in the <i>source.list</i> file like so:",
                    snippet: "http://<USERNAME>:<API_KEY>@$4/artifactory/$1 <DISTRIBUTION> <COMPONENTS>"
                }, {
                    before: "Your apt-get client will use the specified Artifactory repositories to install the package",
                    snippet: "apt-get install <PACKAGE>"
                }],
                deploy: [{
                    before: "To deploy a Debian package into Artifactory you can either use the deploy option in the Artifact’s module or upload with cURL using matrix parameters. The required parameters are package name, distribution, component, and architecture in the following way:",
                    snippet: "curl -u<USERNAME>:<API_KEY> -XPUT \"$2/$1/pool/<DEBIAN_PACKAGE_NAME>;deb.distribution=<DISTRIBUTION>;deb.component=<COMPONENT>;deb.architecture=<ARCHITECTURE>\" -T <PATH_TO_FILE>"
                }, {
                    before: "You can specify multiple layouts by adding semicolon separated multiple parameters, like so:",
                    snippet: "curl -u<USERNAME>:<API_KEY> -XPUT \"$2/$1/pool/<DEBIAN_PACKAGE_NAME>;deb.distribution=<DISTRIBUTION>;deb.distribution=<DISTRIBUTION>;deb.component=<COMPONENT>;deb.component=<COMPONENT>;deb.architecture=<ARCHITECTURE>;deb.architecture=<ARCHITECTURE>\" -T <PATH_TO_FILE>",
                    after: "To add an architecture independent layout use deb.architecture=all. This will cause your package to appear in the Packages index of all the architectures under the same Distribution and Component, as well as under a new index branch called binary-all which holds all Debian packages that are marked as \"all\"."
                }]
            },
            pypi: {
                read: [{
                    before: "To resolve packages using pip, add the following to ~/.pip/pip.conf:",
                    snippet: "[global]\n\tindex-url = $2/api/pypi/$1/simple"
                }, {
                    before: "If credentials are required they should be embedded in the URL. To resolve packages using pip, run:",
                    snippet: "pip install <PACKAGE>"
                }],
                deploy: [{
                    before: "To deploy packages using setuptools you need to add an Artifactory repository to the <i>.pypirc</i> file (usually located in your home directory):",
                    snippet: "[distutils]\n" + "\tindex-servers = local\n" + "[local]\n" + "\trepository: $2/api/pypi/$1\n" + "\tusername: <USERNAME>\n" + "\tpassword: <PASSWORD>"
                }, {
                    before: "To deploy a python egg to Artifactory, after changing the <i>.pypirc</i> file, run the following command:",
                    snippet: "python setup.py sdist upload -r <LOCAL>",
                    after: "where &lt;LOCAL&gt; is the index server you defined in <i>.pypirc</i>."
                }]
            },
            bower: {
                general: [{
                    before: "The following instructions apply to <b>Bower version 1.5</b> and above. For older versions, please refer to <a href=\"http://www.jfrog.com/confluence/display/RTF/Bower+Repositories#BowerRepositories-UsingOlderVersionsofBower\">these instructions</a>."
                }, {
                    before: "In order to use Bower with Artifactory you will need to add 'bower-art-resolver' as one of the resolvers in your .bowerrc file. To install <a href=\"https://www.google.com/url?q=https%3A%2F%2Fwww.npmjs.com%2Fpackage%2Fbower-art-resolver&sa=D&sntz=1&usg=AFQjCNH5pnW2E2ETaXtmJL33xBhGkxKPag\" target=\"_blank\">bower-art-resolver</a> (custom Bower resolver dedicated to integrate with Artifactory), run the following command:",
                    snippet: "npm install -g bower-art-resolver"
                }, {
                    before: "And add the bower-art-resolver as one of the resolvers in your <i>.bowerrc</i> file:",
                    snippet: "{\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
                },{
                    before: "Now replace the default Bower registry with the following in your <i>.bowerrc</i> file:",
                    snippet: "{\n\t\"registry\" : \"$2/api/bower/$1\",\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
                }, {
                    before: "If authentication is required use:",
                    snippet: "{\n\t\"registry\" : \"http://<USERNAME>:<API_KEY>@$4/artifactory/api/bower/$1\",\n\t\"resolvers\" : [\n\t\t\"bower-art-resolver\"\n\t]\n}"
                }],
                read: {
                    before: "To install bower packages execute the following command:",
                    snippet: "bower install <PACKAGE>"
                },
                deploy: {
                    before: "To deploy a Bower package into an Artifactory repository you need to use Artifactory's REST API or the Web UI.<br/>For example, to deploy a Bower package into this repository using the REST API, use the following command:",
                    snippet: "curl -u<USERNAME>:<API_KEY> -XPUT $2/$1/<TARGET_FILE_PATH> -T <PATH_TO_FILE>"
                }
            },
            docker: {
                general: {
                    title: "Using Docker with Artifactory requires a reverse proxy such as Nginx or Apache. For more details please visit our <a href=\"http://www.jfrog.com/confluence/display/RTF/Docker+Repositories#DockerRepositories-RequirementforaReverseProxy(Nginx/Apache)\" target=\"_blank\">documentation</a>."
                },
                read: [{
                    before: "Execute docker pull from the endpoint URL.<br/>For example, to pull from server named <b>artprod.company.com</b> that is behind Nginx or Apache:",
                    snippet: "docker pull artprod.company.com/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
                }],
                deploy: [{
                    before: "Execute \"docker push\" from the endpoint URL.<br/>For example, to push to a server named <b>artprod.company.com</b> that is behind Nginx or Apache:",
                    snippet: "docker tag ubuntu artprod.company.com/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
                }, {
                    before: " ",
                    snippet: "docker push artprod.company.com/<DOCKER_REPOSITORY>:<DOCKER_TAG>"
                }]
            },
            gitlfs: {
                read: [{
                    before: "In order for your client to upload and download LFS blobs from artifactory, the [lfs] clause should be added to the <i>.gitconfig</i> file of your Git repository in the following format:",
                    snippet: "[lfs]\n" + "url = \"$2/api/lfs/$1\""
                }, {
                    before: "You can also set LFS endpoints for different remotes on your repo (as supported by the Git LFS client). For example:",
                    snippet: "[remote \"origin\"]\n" + "url = <URL>\n" + "fetch = +refs/heads/*:refs/remotes/origin/*\n" + "lfsurl = \"$2/api/lfs/$1\""
                }]
            },
            nuget: {
                general: [{
                    before: "When using Artifactory as a NuGet repository you can wither work with the NuGet client directly or with Visual Studio"
                },{
                    before: "<b>NuGet Client Configuration</b><br/>To configure NuGet client to work with Artifactoy you will need to add Artifactroy to the list of sources. To add this repository use the following command",
                    snippet: "nuget sources Add -Name Artifactory -Source $2/api/nuget/$1"
                },{
                    before: "To enable the use of NuGet API key, run the following command",
                    snippet: "nuget setapikey <USER_NAME>:<PASSWORD> -Source Artifactory"
                },{
                    before: "<b>Visual Studio Configuration</b><br/>To configure the NuGet Visual Studio Extension to use Artifactory, you need to add Artifactory as another Package Source under NuGet Package Manager.<ol><li>Access the corresponding repositories in the \"Options\" window (Options | Tools) and select to add another Package Source.<br />Name: ENTER_RESOUCE_NAME (e.g. Artifactory NuGet repository)</li><li>Paste the snippet below in the URL field</li></ol>",
                    snippet: "$2/api/nuget/$1",
                    after: "<ol start=\"3\"><li>Make sure it is checked.</li></ol>"
                }],
                read:[{
                    before: "<b>NuGet Client Resolve</b><br/>To resove using the NuGet client, run the following command",
                    snippet: "nuget install <PACKAGE_NAME>"
                },{
                    before: "To make sure your client will resolve from Artifactory verify it is the first in the list of sources, or run the following command",
                    snippet: "nuget install <PACKAGE_NAME> -Source Artifactory"
                }],
                deploy: [{
                    before: "Uploading packages to Artifactory can be done by running the following command:",
                    snippet: "nuget push <PACKAGE_NAME> -Source Artifactory"
                },{
                    before: "To support more manageable layouts and additional features such as cleanup, NuGet repositories support custom layouts. When pushing a package, you need to ensure that its layout matches the target repository’s layout:",
                    snippet: "nuget push <PACKAGE> -Source $2/api/nuget/$1/<PATH_TO_FOLDER>"
                }]
            },
            ivy: {
                general: {
                    title: "Click on \"Generate Ivy Settings\" in order to use Virtual or Remote repositories for resolution."
                }
            },
            maven: {
                general: {
                    title: "Click on \"Generate Maven Settings\" in order to resolve artifacts through Virtual or Remote repositories."
                },
                deploy: {
                    before: "To deploy build artifacts through Artifactory you need to add a deployment element with the URL of a target local repository to which you want to deploy your artifacts. For example:"
                }
            },
            npm: {
                general: [{
                    title: "In order for your npm command line client to work with Artifactory you will firstly need to set the relevant authentication. For getting authentication details run the following command:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> $2/api/npm/auth"
                }, {
                    before: "The response should be pasted in the <i>~/.npmrc</i> (in Windows %USERPROFILE%/<i>.npmrc</i>) file. Here is an example of the content of the file:",
                    snippet: "_auth = <USERNAME>:<API_KEY> (converted to base 64)\n" + "email = youremail@email.com\n" + "always-auth = true"
                }, {
                    before: "Artifactory also support scoped packages. For getting authentication details run the following command:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> \"$2/api/npm/$1/auth/<SCOPE>\""
                }, {
                    before: "The response should be pasted in the <i>~/.npmrc</i> (in Windows %USERPROFILE%/<i>.npmrc</i>) file. Here is an example of the content of the file:",
                    snippet: "@<SCOPE>:registry=$2/api/npm/$1/\n" + "//$4/api/npm/$1/:_password=<API_KEY>\n" + "//$4/api/npm/$1/:username=<USER_NAME>\n" + "//$4/api/npm/$1/:email=email@domain.com\n" + "//$4api/npm/$1/:always-auth=true\n"
                }, {
                    before: "Run the following command to replace the default npm registry with an Artifactory repository:",
                    snippet: "npm config set registry $2/api/npm/$1"
                }, {
                    before: "For scoped package run the following command:",
                    snippet: "npm config set @<SCOPE>:regitrsy $2/api/npm/$1"
                }],
                read: [{
                    before: "After adding Artifactory as the default repository you can install a package using the npm install command:",
                    snippet: "npm install <PACKAGE_NAME>"
                }, {
                    before: "To install a package by specifying Artifactory repository use the following npm command:",
                    snippet: "npm install <PACKAGE_NAME> --registry $2/api/npm/$1"
                }],
                deploy: [{
                    before: "To deploy your package to an Artifactory repository you can either add the following to the <i>package.json</i> file:",
                    snippet: "\"publishConfig\":{\"registry\":\"$2/api/npm/$1\"}"
                }, {
                    before: "And then you can simply run the default npm publish command:",
                    snippet: "npm publish"
                }, {
                    before: "Or provide the local repository to the npm publish command:",
                    snippet: "npm publish --registry $2/api/npm/$1" }]
            },
            gems: {
                general: [{
                    title: "For your gem client to upload and download Gems from this repository you need to add it to your <i>~/.gemrc</i> file using the following command:",
                    snippet: "gem source -a http://<USERNAME>:<API_KEY>@$4/artifactory/api/gems/$1/"
                }, {
                    before: "If anonymous access is enabled you can also use the following:",
                    snippet: "gem source -a $2/api/gems/$1/"
                }, {
                    before: "To view a list of your effective sources and their order of resolution, run the following command:",
                    snippet: "gem source --list",
                    after: "Make sure that this repository is at the top of the list."
                }, {
                    before: "If you want to setup the credentials for your gem tool either include your API_KEY in the <i>~/.gem/credentials</i> file, or run the following command:",
                    snippet: "curl -u<USERNAME>:<API_KEY> $2/api/gems/$1/api/v1/api_key.yaml > ~/.gem/credentials"
                }, {
                    before: "<b>Running on Linux</b><br/>On Linux you may need to change the permissions of the credentials file to 600 by navigating to <i>~/.gem</i> directory and running:",
                    snippet: "chmod 600 credentials"
                }, {
                    before: "<b>Running on Windows</b><br/>On Windows, the credentials file is located at <i>%USERPROFILE%/.gem/credentials</i>. Note that you also need to set the API key encoding to be \"ASCII\".<br/> To generate the creadentials file run the following command from PowerShell:",
                    snippet: "curl -u<USERNAME>:<API_KEY> $2/api/gems/$1/api/v1/api_key.yaml | Out-File ~/.gem/credentials -Encoding \"ASCII\""
                }, {
                    before: "<b>API keys</b><br/>You can modify the credentials file manually and add different API keys. You can then use the following command to choose the relevant API key:",
                    snippet: "gem push -k <KEY>"
                }],
                deploy: [{
                    before: "In order to push gems to this repository, you can set the global variable $RUBYGEMS_HOST to point to it as follows:",
                    snippet: "export RUBYGEMS_HOST=$2/api/gems/$1"
                }, {
                    before: "You can also specify the target repository when pushing the gem by using the --host option:",
                    snippet: "gem push <PACKAGE> --host $2/api/gems/$1"
                }],
                read: [{
                    before: "After completing the configuration under General section above, simply execute the following command:",
                    snippet: "gem install <PACKAGE>"
                }, {
                    before: "The package will be resolved from the repository configured in your <i>~/.gemrc</i> file. You can also specify a source with the following command:",
                    snippet: "gem install <PACKAGE> --source $2/api/gems/$1"
                }]
            },
            generic: {
                read: {
                    before: "You can download a file directly using the following command:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -O \"$2/$1/<TARGET_FILE_PATH>\""
                },
                deploy: {
                    before: "You can upload any file using the following command:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -T <PATH_TO_FILE> \"$2/$1/<TARGET_FILE_PATH>\""
                }
            },
            vagrant: {
                read: {
                    before: "To provision a Vagrant box, all you need is to construct it's name in the following manner.",
                    snippet: "vagrant box add \"$2/api/vagrant/$1/{boxName}\""
                },
                deploy: {
                    before: "To deploy Vagrant boxes to this Artifactory repository using an explicit URL with Matrix Parameters use:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -T <PATH_TO_FILE> \"$2/$1/{vagrantBoxName.box};box_name={name};box_provider={provider};box_version={version}\""
                }
            },
            vcs: {
                general: {
                    title: "Artifactory supports downloading tags or branches using a simple GET request. You can also specify to download a specific tag or branch as a tar.gz or zip, and a specific file within a tag or branch as a zip file."
                },
                read: [{
                    before: "Use the following command to list all tags:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/tags/$1/<USERR_ORG>/<REPO>"
                }, {
                    before: "Use the following command to list all branches:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/branches/$1/<USERR_ORG>/<REPO>"
                }, {
                    before: "Use the command below to download a tag. You can specify if the package will be downloaded as a tar.gz or a zip; default is tar.gz.",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/downloadTag/$1/<USER_ORG>/<REPO>/<TAG_NAME>?ext=<tar.gz/zip>"
                }, {
                    before: "Use the following command to download a file within a tag as a zip:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/downloadTag/$1/<USER_ORG>/<REPO>/<TAG_NAME>!<PATH_TO_FILE>?ext=zip"
                }, {
                    before: "Use the command below to download a branch. You can specify a tar.gz or a zip by adding a parameter in the URL; default is tar.gz. (Downloading can be executed conditionally according to properties by specifying the properties query param. In this case only cached artifacts are searched.)",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/downloadBranch/$1/<USER_ORG>/<REPO>/<BRANCH_NAME>?ext=<tar.gz/zip>[&properties=key=value]"
                }, {
                    before: "Use the following command to download a file within a branch as a zip:",
                    snippet: "curl -i -u<USERNAME>:<API_KEY> -XGET $2/api/vcs/downloadBranch/$1/<USER_ORG>/<REPO>/<BRANCH_NAME>!<PATH_TO_FILE>?ext=zip"
                }]
            },
            yum: {
                read: [{
                    before: "To resolve <i>.rpm</i> files using the YUM client, edit or create the <i>artifactory.repo</i> file with root privileges:",
                    snippet: "sudo vi /etc/yum.repos.d/artifactory.repo"
                }, {
                    before: "Then paste the configuration:",
                    snippet: "[Artifactory]\n" + "\tname=Artifactory\n" + "\tbaseurl=$2/$1/\n" + "\tenabled=1\n" + "\tgpgcheck=0"
                }, {
                    before: "And execute:",
                    snippet: "yum install <PACKAGE>"
                }]
            },
            sbt: {
                general: [{
                    before: "You can define proxy repositories in the <i>~/.sbt/repositories</i> file in the following way:",
                    snippet: "[repositories]\n" + "local\n" + "my-ivy-proxy-releases: $2/$1/, [organization]/[module]/(scala_[scalaVersion]/)(sbt_[sbtVersion]/)[revision]/[type]s/[artifact](-[classifier]).[ext]\n" + "my-maven-proxy-releases: $2/$1/"
                }, {
                    before: "In order to specify that all resolvers added in the sbt project added should be ignored in favor of those configured in the repositories configuration, add the following configuration option to the sbt launcher script:",
                    snippet: "-Dsbt.override.build.repos=true",
                    after: "You can add this setting to the <i>/usr/local/etc/sbtopts</i> file"
                }],
                read: {
                    before: "Add the following to your <i>build.sbt</i> file:",
                    snippet: "resolvers += \n" + "\"Artifactory\" at \"$2/$1/\""
                },
                deploy: [{
                    before: "To publish <b>releases</b> add the following to your build.sbt:",
                    snippet: "publishTo := Some(\"Artifactory Realm\" at \"$2/$1\")\n" + "credentials += Credentials(\"Artifactory Realm\", \"localhost\", \"<USERNAME>\", \"<PASS>\")"
                }, {
                    before: "To publish <b>snapshots</b> add the following to your build.sbt:",
                    snippet: "publishTo := Some(\"Artifactory Realm\" at \"$2/$1;build.timestamp=\" + new java.util.Date().getTime)\n" + "credentials += Credentials(\"Artifactory Realm\", \"localhost\", \"<USERNAME>\", \"<PASS>\")"
                }]
            },
            gradle: {
                general: {
                    title: "Click on \"Generate Gradle Settings\" in order to use Virtual or Remote repositories for resolution."
                }
            }
        };

        this.setMeUpScope.checkLayoutSettings = function (settings, repoType) {
            if (this.select && this.select.selected) {
                if (repoType == 'ivy') {
                    this.selection.gradle[settings + 'UseIvy'] = true
                    this.selection.gradle[settings + 'UseMaven'] = false
                }
                else if (repoType == 'maven') {
                    this.selection.gradle[settings + 'UseMaven'] = true
                    this.selection.gradle[settings + 'UseIvy'] = false
                }
            }
            else {
                if (repoType == 'ivy') {
                    if (!this.selection.gradle[settings + 'UseMaven']) {
                        this.selection.gradle[settings + 'UseMaven'] = true
                    }
                }
                else if (repoType == 'maven') {
                    if (!this.selection.gradle[settings + 'UseIvy']) {
                        this.selection.gradle[settings + 'UseIvy'] = true
                    }
                }
            }
        }

        this.setMeUpScope.repoTypes = this.repoPackageTypes;

        // Select the repo type according to current node
        for (var i = 0; i < this.setMeUpScope.repoTypes.length; i++) {
            if (this.setMeUpScope.node.repoPkgType.toLowerCase() == this.setMeUpScope.repoTypes[i].value.toLowerCase()) {
                this.setMeUpScope.selection.repoType = this.setMeUpScope.repoTypes[i]
                break
            }
        }


        this.setMeUpScope.getMavenProps = function () {
            let scope = this.me()
            return JSON.stringify({
                release: scope.selection.maven.releases,
                snapshot: scope.selection.maven.snapshots,
                pluginRelease: scope.selection.maven.pluginReleases,
                pluginSnapshot: scope.selection.maven.pluginSnapshots,
                mirror: (scope.selection.maven.mirror) ? scope.selection.maven.mirrorAny : ''
            })
        }

        this.setMeUpScope.getGradleProps = function () {
            let scope = this.me();
            return JSON.stringify({
                pluginRepoKey: scope.selection.gradle.pluginResolver,
                libsResolverRepoKey: scope.selection.gradle.libsResolver,
                libsPublisherRepoKey: scope.selection.gradle.libsPublisher,
                pluginUseMaven: scope.selection.gradle.pluginUseMaven,
                resolverUseMaven: scope.selection.gradle.libsUseMaven,
                publisherUseMaven: scope.selection.gradle.publishUseMaven,
                pluginUseIvy: scope.selection.gradle.pluginUseIvy,
                resolverUseIvy: scope.selection.gradle.libsUseIvy,
                publisherUseIvy: scope.selection.gradle.publishUseIvy,
                pluginResolverLayout: scope.selection.gradle.pluginLayout,
                libsResolverLayout: scope.selection.gradle.libsLayout,
                libsPublisherLayouts: scope.selection.gradle.publishLayout
            })
        }

        this.setMeUpScope.getIvyProps = function () {
            let scope = this.me()
            return JSON.stringify({
                libsRepo: scope.selection.ivy.libsRepository,
                libsRepoLayout: scope.selection.ivy.libsRepositoryLayout,
                libsResolverName: scope.selection.ivy.libsResolverName,
                useIbiblioResolver: (scope.selection.ivy.ibiblio) ? true : false,
                m2Compatible: (scope.selection.ivy.maven2) ? true : false
            })
        }

        this.setMeUpScope.generateBuildSettings = function () {
            var scope = this.me()
            if (!scope.generate) return false

            if (scope.generate.maven) {
                setMeUpDao.maven_snippet({
                    release: scope.selection.maven.releases,
                    snapshot: scope.selection.maven.snapshots,
                    pluginRelease: scope.selection.maven.pluginReleases,
                    pluginSnapshot: scope.selection.maven.pluginSnapshots,
                    mirror: (scope.selection.maven.mirror) ? scope.selection.maven.mirrorAny : ''
                }).$promise.then((result)=> {
                            scope.snippet = result.mavenSnippet
                        })
            }
            else if (scope.generate.gradle) {
                setMeUpDao.gradle_snippet({
                    pluginRepoKey: scope.selection.gradle.pluginResolver,
                    libsResolverRepoKey: scope.selection.gradle.libsResolver,
                    libsPublisherRepoKey: scope.selection.gradle.libsPublisher,
                    pluginUseMaven: scope.selection.gradle.pluginUseMaven,
                    resolverUseMaven: scope.selection.gradle.libsUseMaven,
                    publisherUseMaven: scope.selection.gradle.publishUseMaven,
                    pluginUseIvy: scope.selection.gradle.pluginUseIvy,
                    resolverUseIvy: scope.selection.gradle.libsUseIvy,
                    publisherUseIvy: scope.selection.gradle.publishUseIvy,
                    pluginResolverLayout: scope.selection.gradle.pluginLayout,
                    libsResolverLayout: scope.selection.gradle.libsLayout,
                    libsPublisherLayouts: scope.selection.gradle.publishLayout
                }).$promise.then((result)=> {
                            scope.snippet = result.gradleSnippet
                        })
            }
            else if (scope.generate.ivy) {
                setMeUpDao.ivy_snippet({
                    libsRepo: scope.selection.ivy.libsRepository,
                    libsRepoLayout: scope.selection.ivy.libsRepositoryLayout,
                    libsResolverName: scope.selection.ivy.libsResolverName,
                    useIbiblioResolver: (scope.selection.ivy.ibiblio) ? true : false,
                    m2Compatible: (scope.selection.ivy.maven2) ? true : false
                }).$promise.then((result)=> {
                            scope.snippet = result.ivySnippet
                        })
            }
        }

        this.setMeUpScope.filterByType = function () {
            if (!this.reposAndTypes) return false

            let scope = this.me()
            scope.settingPage = false;
            if (scope.selection && scope.selection.repo) {
                scope.selection.repo = null
            }
            scope.snippet = scope.readSnippet = scope.deploySnippet = null
            scope.generateSettings = false
            scope.generate = {}

            scope.generalSnippets = []
            scope.readSnippets = []
            scope.deploySnippets = []

            this.setShowSettings(scope)
            this.setRepositories(scope)
            this.selectRepoByType(scope)
            let repoData = this.getRepoData(scope)
            //Populate general snippets
            this.setGeneralSnippets(repoData)
        }

        this.setMeUpScope.setShowSettings = function (scope) {
            let selection = this.selection
            if (scope.selection && selection.repoType && scope.selection.repoType.value.match('(ivy|maven|gradle)')) {
                scope.showSettings = selection.repoType.text
            }
            else {
                scope.showSettings = false
            }
        }

        this.setMeUpScope.setRepositories = function (scope) {
            scope.shownRepos = this.reposAndTypes.filter(function (d) {
                if (!this.selection || !this.selection.repoType || this.selection.repoType.value == 'generic') return d
                if (this.selection.repoType.value == 'maven' && !d.local) return false
                var isRepoMavenish = this.selection.repoType.value.match(/(maven|ivy|gradle|sbt)/gi) ? true : false
                var isSelectionMavenish = d.value.match(/(maven|ivy|gradle|sbt)/gi) ? true : false
                if (d.value == this.selection.repoType.value || d.value == this.selection.repoType.value
                        || (isRepoMavenish && isSelectionMavenish)) return d
            }.bind(this))
        }

        this.setMeUpScope.getRepoData = function (scope) {
            let repoData = this.reposAndTypes.filter(function (item) {
                if (scope.selection.repo && item.text == scope.selection.repo.text) {
                    return item
                }
            })
            repoData = (repoData.length > 0) ? repoData[0] : null

            return repoData
        }

        this.setMeUpScope.selectRepoByType = function (scope) {
            // Select the repo according to current node
            for (var i = 0; i < scope.reposAndTypes.length; i++) {
                if (scope.reposAndTypes[i].value.toLowerCase() == scope.selection.repoType.value) {
                    scope.selection.repo = scope.reposAndTypes[i]
                    scope.resolveSnippet()
                    break
                }
            }
        }

        this.setMeUpScope.getGeneratorRepos = function (type) {
            let scope = this.me()
            scope.settingPage = true;
            if (!scope.generate) scope.generate = {}

            scope.readSnippet = scope.deploySnippet = null

            switch (type) {
                case 'Maven':
                    setMeUpDao.maven().$promise.then((result)=> {
                        scope.generateSettings = true;
                        scope.generate = {maven: true};
                        scope.settings.maven = result;
                        this.selection.maven = {
                            releases: scope.settings.maven.releases[0],
                            snapshots: scope.settings.maven.snapshots[0],
                            pluginReleases: scope.settings.maven.pluginReleases[0],
                            pluginSnapshots: scope.settings.maven.pluginSnapshots[0],
                            mirrorAny: scope.settings.maven.anyMirror[0],
                            mirror: false
                        };
                    });
                    break;
                case 'Gradle':
                    setMeUpDao.gradle().$promise.then((result)=> {
                        scope.generateSettings = true;
                        scope.generate = {gradle: true};
                        scope.settings.gradle = result;
                        this.selection.gradle = {
                            pluginResolver: scope.settings.gradle.pluginResolver[0],
                            pluginUseMaven: true,
                            pluginUseIvy: false,
                            pluginLayout: scope.settings.gradle.layouts[0],
                            libsResolver: scope.settings.gradle.libsResolver[0],
                            libsUseMaven: true,
                            libsUseIvy: false,
                            libsLayout: scope.settings.gradle.layouts[0],
                            libsPublisher: scope.settings.gradle.libsPublisher[0],
                            publishUseMaven: true,
                            publishUseIvy: false,
                            publishLayout: scope.settings.gradle.layouts[0]
                        };
                    });
                    break;
                case 'Ivy':
                    setMeUpDao.ivy().$promise.then((result)=> {
                        scope.generateSettings = true;
                        scope.generate = {ivy: true};
                        scope.settings.ivy = result;
                        this.selection.ivy = {
                            libsRepository: scope.settings.ivy.libsRepository[0],
                            libsRepositoryLayout: scope.settings.ivy.libsRepositoryLayout[0],
                            ibiblio: true,
                            maven2: true
                        }
                    });
                    break;
                default:
                    scope.generateSettings = false;
                    break
            }

        };

        this.setMeUpScope.resolveSnippet = function () {
            if (!this.selection.repoType) {
                return
            }
            let scope = this.me()
            let repoData = this.getRepoData(scope)
            let repoType = this.selection.repoType.value;

            scope.deploySnippets = []
            scope.readSnippets = []
            scope.generalSnippets = []

            if (this.snippets[repoType]) {
                this.setDeploySnippets(repoData)
                this.setReadSnippets(repoData)
                this.setGeneralSnippets(repoData)
            }

            //Warn the user if he doesn't have deploy permissions
            if(!repoData.deploy && repoData.local){
                scope.generalSnippets.push({
                    title: this.$sce.trustAsHtml("<b>You don't have deploy permissions on this repository!<b/>")
                });
            }
        }

        this.setMeUpScope.setDeploySnippets = function (repoData) {
            let scope = this.me()
            let repoType = this.selection.repoType.value

            // Maven from server
            if (repoType == 'maven') {
                scope.deploySnippets = []
                setMeUpDao.maven_distribution({repoKey: repoData.text}).$promise.then((result)=> {
                    if (repoData.local) {
                        scope.deploySnippets.push({
                            before: (this.snippets[repoType]['deploy']) ? this.snippets[repoType]['deploy']['before'] : '',
                            snippet: result.distributedManagement,
                            after: (this.snippets[repoType]['deploy']) ? this.snippets[repoType]['deploy']['after'] : ''
                        })
                    }
                })
            }

            if (repoType != 'maven' && repoData.local && this.snippets[repoType]['deploy']) {
                scope.deploySnippets = []
                if (this.snippets[repoType]['deploy'] instanceof Array) {
                    for (var i = 0; i < this.snippets[repoType]['deploy'].length; i++) {
                        let tpl = (this.snippets[repoType]['deploy']) ?
                                this.snippets[repoType]['deploy'][i]['snippet'] : null
                        if (tpl) {
                            tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                    this.serverId).replace(/\$4/g, this.host)
                            scope.deploySnippets.push({
                                before: this.$sce.trustAsHtml(this.snippets[repoType]['deploy'][i]['before']),
                                snippet: tpl,
                                after: this.$sce.trustAsHtml(this.snippets[repoType]['deploy'][i]['after'])
                            })
                        }
                    }
                }
                else {
                    let tpl = (this.snippets[repoType]['deploy']) ? this.snippets[repoType]['deploy']['snippet'] :
                            null
                    if (tpl) {
                        tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                this.serverId).replace(/\$4/g, this.host)
                        scope.deploySnippets.push({
                            before: this.$sce.trustAsHtml(this.snippets[repoType]['deploy']['before']),
                            snippet: tpl,
                            after: this.$sce.trustAsHtml(this.snippets[repoType]['deploy']['after'])
                        })
                    }
                }
            }
        }

        this.setMeUpScope.setReadSnippets = function (repoData) {
            let scope = this.me()
            scope.readSnippets = []
            let repoType = this.selection.repoType.value

            if (repoData.read && this.snippets[repoType]['read']) {
                if (this.snippets[repoType]['read'] instanceof Array) {
                    for (var i = 0; i < this.snippets[repoType]['read'].length; i++) {
                        let tpl = (this.snippets[repoType]['read']) ?
                                this.snippets[repoType]['read'][i]['snippet'] : null
                        if (tpl) {
                            tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                    this.serverId).replace(/\$4/g, this.host)
                            scope.readSnippets.push({
                                before: this.$sce.trustAsHtml(this.snippets[repoType]['read'][i]['before']),
                                snippet: tpl,
                                after: this.$sce.trustAsHtml(this.snippets[repoType]['read'][i]['after'])
                            })
                        }
                    }
                }
                else {
                    let tpl = (this.snippets[repoType]['read']) ? this.snippets[repoType]['read']['snippet'] : null
                    if (tpl) {
                        tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                this.serverId).replace(/\$4/g, this.host)
                        scope.readSnippets.push({
                            before: this.$sce.trustAsHtml(this.snippets[repoType]['read']['before']),
                            snippet: tpl,
                            after: this.$sce.trustAsHtml(this.snippets[repoType]['read']['after'])
                        })
                    }
                }
            }
        }

        this.setMeUpScope.setGeneralSnippets = function (repoData) {
            if (!this.selection.repoType) {
                return
            }
            let scope = this.me()
            let repoType = this.selection.repoType.value

            scope.generalSnippets = []
            if (this.snippets[repoType]['general']) {
                if (this.snippets[repoType]['general'] instanceof Array) {
                    for (var i = 0; i < this.snippets[repoType]['general'].length; i++) {
                        let tpl = (this.snippets[repoType]['general']) ?
                                this.snippets[repoType]['general'][i]['snippet'] : null
                        if (tpl && repoData) {
                            tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                    this.serverId).replace(/\$4/g, this.host)
                        }
                        scope.generalSnippets.push({
                            title: this.$sce.trustAsHtml(this.snippets[repoType]['general'][i]['title']),
                            before: this.$sce.trustAsHtml(this.snippets[repoType]['general'][i]['before']),
                            snippet: tpl,
                            after: this.$sce.trustAsHtml(this.snippets[repoType]['general'][i]['after'])
                        })
                    }
                }
                else {
                    let tpl = (this.snippets[repoType]['general']) ? this.snippets[repoType]['general']['snippet'] : null
                    if (tpl && repoData) {
                        tpl = tpl.replace(/\$1/g, repoData.text).replace(/\$2/g, this.baseUrl).replace(/\$3/g,
                                this.serverId).replace(/\$4/g, this.host)
                    }
                    scope.generalSnippets.push({
                        title: this.$sce.trustAsHtml(this.snippets[repoType]['general']['title']),
                        before: this.$sce.trustAsHtml(this.snippets[repoType]['general']['before']),
                        snippet: tpl,
                        after: this.$sce.trustAsHtml(this.snippets[repoType]['general']['after'])
                    })
                }
            }
        }

        this.getSetMeUpData(function(data) {
//            var url = new URL(data.baseUrl) //CAUSES PROBLEM ON IE, NOT REALY NEEDED...
            let parser = this.parseUrl(data.baseUrl);
            this.setMeUpScope.baseUrl = parser.href;
            this.setMeUpScope.host = parser.host;
            this.setMeUpScope.serverId = data.serverId;

            data.repoKeyTypes.sort(function(a,b) {
                return (a.repoKey > b.repoKey)?1:-1
            })
            this.setMeUpScope.reposAndTypes = data.repoKeyTypes.map(function(item) {
                return { text : item.repoKey, value : item.repoType.toLowerCase(), read : item.canRead, deploy: item.canDeploy, local: item.isLocal }
            })
            //this.setMeUpScope.filterByType()

            // Select the repo according to current node
            for (var i = 0; i < this.setMeUpScope.reposAndTypes.length; i++) {
                if (this.setMeUpScope.reposAndTypes[i].text.toLowerCase() == this.setMeUpScope.node.text.toLowerCase() ||
                        this.setMeUpScope.reposAndTypes[i].text.concat("-cache").toLowerCase() == this.setMeUpScope.node.text.toLowerCase()) {
                    this.setMeUpScope.selection.repo = this.setMeUpScope.reposAndTypes[i]
                    this.setMeUpScope.resolveSnippet()
                    break
                }
            }

            let repoData = this.setMeUpScope.getRepoData(this.setMeUpScope)

            //Populate general snippets
            this.setMeUpScope.setGeneralSnippets(repoData)

            this.setMeUpScope.setRepositories(this.setMeUpScope)

            this.setMeUpScope.setShowSettings(this.setMeUpScope)

        }.bind(this))

        var sc = this.setMeUpScope

        this.setMeUpScope.$watch('selection', function() {
            if (sc.generateSettings && sc.snippet) sc.generateBuildSettings()
        }, true)
    }


    selectNode(node) {
        let previousNode = this.node;
        this.node = node;
        if (node.data) {        
            this.artifactoryState.setState('repoKey', this.node.data.repoKey);
            let location = true;
            if (this.$state.current.name === 'artifacts.browsers.path' && (!previousNode || (!this.$state.params.artifact && this.$state.params.tab !== 'StashInfo'))) {
                // If no artifact and selecting artifact - replace the location (fix back button bug)
                location = 'replace';
            }
            this.$state.go(this.$state.current, {artifact: node.data.fullpath}, {location: location});
        }
        else {
            this.artifactoryState.removeState('repoKey');
            this.$state.go(this.$state.current, {artifact: ''});
        }
    }

    exitStashState() {
        this.artifactoryEventBus.dispatch(EVENTS.ACTION_EXIT_STASH);
    }
}