export default {
    localChecksumPolicy:{
        CLIENT:'Verify against client checksums',
        SERVER:'Trust server generated checksums'
    },
    remoteChecksumPolicy:{
        GEN_IF_ABSENT:'Generate if absent',
        FAIL:'Fail',
        IGNORE_AND_GEN:'Ignore and generate',
        PASS_THRU:'Ignore and pass-through'
    },
    snapshotRepositoryBehavior:{
        UNIQUE:'Unique',
        NONUNIQUE:'Non-unique',
        DEPLOYER:'Deployer'
    },
    pomCleanupPolicy:{
        discard_active_reference:'Discard active references',
        discard_any_reference:'Discard any reference',
        nothing:'Nothing'
    },
    dockerApiVersion:{
        V1:'V1',
        V2:'V2'
    },
    vcsGitProvider:{
        GITHUB:'GitHub',
        BITBUCKET:'BitBucket',
        STASH:'Stash',
        ARTIFACTORY:'Artifactory',
        CUSTOM:'Custom'
    },
    defaultLayouts:{
        maven:'maven-2-default',
        ivy:'ivy-default',
        gradle:'gradle-default',
        nuget:'nuget-default',
        npm:'npm-default',
        bower:'bower-default',
        vcs:'vcs-default',
        sbt:'sbt-default'
    },
    REPO_TYPE:{
        LOCAL:'local',
        REMOTE:'remote',
        VIRTUAL:'virtual'
    },
    repoPackageTypes:[
        {"serverEnumName": "Bower", "value": "bower", "text": "Bower", "icon": 'bower', "repoType": ['local', 'remote', 'virtual'], "description": 'Bower package manager is optimized for front-end development. A Bower repository will allow you to easily manage your Bower packages and proxy remote Bower repositories.'},
        {"serverEnumName": "Debian", "value": "debian", "text": "Debian", "icon": 'deb', "repoType": ['local', 'remote'], "description": 'A Debian repository will allow you to host, cache and distribute your Debian packages for Debian based OSs such as Ubuntu.'},
        {"serverEnumName": "Docker", "value": "docker", "text": "Docker", "icon": 'docker', "repoType": ['local', 'remote', 'virtual'], "description": 'Docker allows you to package an application with all of its dependencies into a standardized unit for software development. A Docker repository will allow you to easily and securely manage your Docker images.'},
        {"serverEnumName": "Gems", "value": "gems", "text": "Gems", "icon": 'ruby-gems', "repoType": ['local', 'remote', 'virtual'], "description": 'A RubyGems repository allows you to easily download, install, and use ruby software packages in your system. Gems can be used to extend or modify functionality in Ruby applications.'},
        {"serverEnumName": "GitLfs", "value": "gitlfs", "text": "Git LFS", "icon": 'git-lfs', "repoType": ['local'], "description": 'Git LFS replaces large files such as audio samples, videos, datasets, and graphics with text pointers inside Git, while storing the file contents in an Artifactory repository. This allows you to work with the same Git workflow, but with better access control, faster download and more repository space.'},
        {"serverEnumName": "Gradle", "value": "gradle", "text": "Gradle", "icon": 'gradle', "repoType": ['local', 'remote', 'virtual'], "description": 'Gradle is a build automation tool, which lets model your problem domain declaratively using a powerful and expressive domain-specific language (DSL) implemented in Groovy.'},
        {"serverEnumName": "Ivy", "value": "ivy", "text": "Ivy", "icon": 'ivy', "repoType": ['local', 'remote', 'virtual'], "description": 'Apache Ivy is a popular dependency manager focusing on flexibility and simplicity. Ivy offers full integration with ant, and a strong transitive dependencies management engine.'},
        {"serverEnumName": "Maven", "value": "maven", "text": "Maven", "icon": 'pom', "repoType": ['local', 'remote', 'virtual'], "description": 'Apache Maven is a build automation tool, and provides useful project information from your project’s sources.'},
        {"serverEnumName": "Npm", "value": "npm", "text": "npm", "icon": 'npm', "repoType": ['local', 'remote', 'virtual'], "description": 'npm package manager makes it easy for JavaScript developers to share and reuse code, and it makes it easy to update the code that you\'re sharing. Host your own node.js packages in Artifactory and proxy remote npm repositories. Use npm against a single in-house repository under your control for your all npm needs.'},
        {"serverEnumName": "NuGet", "value": "nuget", "text": "NuGet", "icon": 'nuget', "repoType": ['local', 'remote', 'virtual'], "description": 'NuGet is the package manager for the Microsoft development platforms including .NET. Host and proxy NuGet packages in Artifactory, and pull libraries from Artifactory into your various Visual Studio .NET applications.'},
        {"serverEnumName": "P2", "value": "p2", "text": "P2", "icon": 'p2', "repoType": ['remote', 'virtual'], "description": 'P2 provides a provisioning platform for Eclipse and Equinox-based applications.'},
        {"serverEnumName": "Pypi", "value": "pypi", "text": "PyPI", "icon": 'pypi', "repoType": ['local', 'remote', 'virtual'], "description": 'The Python Package Index for the Python programming language. Transparently resolve PyPI distribution locations, whether local or remote. Exercise fine-grained access control to all PyPI resources with comprehensive security measures, and full support for pip.'},
        {"serverEnumName": "SBT", "value": "sbt", "text": "SBT", "icon": 'sbt', "repoType": ['local', 'remote', 'virtual'], "description": 'Sbt is a build tool for the Scala community and Java projects. Sbt uses advanced concepts to provide flexible and powerful build definitions.'},
        {"serverEnumName": "Vagrant", "value": "vagrant", "text": "Vagrant", "icon": 'vagrant', "repoType": ['local'], "description": 'Vagrant provides easy-to-configure, reproducible, and portable work environments built on top of industry-standard technology and controlled by a single consistent workflow.'},
        {"serverEnumName": "VCS", "value": "vcs", "text": "VCS", "icon": 'vcs', "repoType": ['remote'], "description": 'A VCS remote repository gives you stable and reliable access to your source code with security and access control, along with smart search capabilities for any of the supported version control systems.'},
        {"serverEnumName": "YUM", "value": "yum", "text": "YUM", "icon": 'yum', "repoType": ['local', 'remote'], "description": 'YUM allows automatic updates with package and dependency management on RPM-based distributions. A YUM repository will allow you to host, cache and distribute your RPM packages.'},
        {"serverEnumName": "Generic", "value": "generic", "text": "Generic", "icon": 'generic', "repoType": ['local', 'remote', 'virtual'], "description": 'A generic repository can be used to host and proxy any type of file.'}
    ]
}