import EVENTS     from '../../constants/common_events.constants';

class jfBrowseFilesController {

    constructor($scope, ArtifactoryModal, ArtifactoryEventBus, $timeout) {
        this.root = '/';
        this.browseFilesDao = this.browserResource;
        this.modal = ArtifactoryModal;
        this.browseFilesScope = $scope.$new();
        this.initBrowseFileScope();
        this.selectedFile = false;
        this.artifactoryEventBus = ArtifactoryEventBus;

        this.$timeout = $timeout;
    }

    initBrowseFileScope() {
        let browseFilesScope = this.browseFilesScope;
        browseFilesScope.folderList = [];
        browseFilesScope.rootsList = [];
        browseFilesScope.fileList = [];
        browseFilesScope.folder = {};
        this.isWindows = false;
        this.browseFilesScope.getDataList = (path)=>this.getDataList(path);
        this.browseFilesScope.setFilePath = path => this.setFilePath(path);
        this.browseFilesScope.setSelectedItem = item => this.setSelectedItem(item);
        this.browseFilesScope.onSelectionChange = item => this.onSelectionChange();
        browseFilesScope.upperFolder = ()=>this.upperFolder();
        browseFilesScope.save = ()=>this.save();
        browseFilesScope.closeModal = ()=>this.closeModal();
        browseFilesScope.onChangeFolder = ()=>this.onChangeFolder();
        browseFilesScope.onChangeRoot = ()=>this.onChangeRoot();
        browseFilesScope.onPathKeyPress = (e)=>this.onPathKeyPress(e);
        browseFilesScope.onPathAutoCompleteSelect = (selection)=>this.onPathAutoCompleteSelect(selection);
        browseFilesScope.onKeyPress = (e)=>this.onKeyPress(e);
        browseFilesScope.selectedItem = null;
        browseFilesScope.baseDirectory = null;

        if (!this.browserOptions)
            this.browserOptions = {};

        browseFilesScope.modalTitle = this.browserOptions.modalTitle || 'Server File System Browser';
        browseFilesScope.selectionLabel = this.browserOptions.selectionLabel || 'Selected Folder:';
        browseFilesScope.pathLabel = this.browserOptions.pathLabel || 'Path:';
        browseFilesScope.canSelectFiles = this.browserOptions.canSelectFiles === true;
        browseFilesScope.showSelectedItem = this.browserOptions.showSelectedItem !== false;
        browseFilesScope.enableSelectedItem = this.browserOptions.enableSelectedItem !== false;
        browseFilesScope.confirmButtonLabel = this.browserOptions.confirmButtonLabel || 'Select';
        browseFilesScope.createDirHelp = 'To create a new directory,\nEnter it\'s name here.';

        if (this.isWindows)
            browseFilesScope.mountLabel = this.browserOptions.windowsDriveLabel || 'Drive:';
        else
            browseFilesScope.mountLabel = this.browserOptions.nonWindowsMountLabel || 'Mount Point:';
    }

    onPathKeyPress(e) {
        if ((!this.isWindows && e.charCode == 47) || (this.isWindows && e.charCode == 92)) {
            this.$timeout(()=>{
                this._gotoPath(this.browseFilesScope.folder.selectedFolder);
            });
        }
    }

    _gotoPath(path) {
        let current = path.endsWith(!this.isWindows ? '/' : '\\') ? path.substr(0,path.length-1) : path;

        if (this.browseFilesScope.pathAutoComplete.indexOf(current)>=0) {
            let parts = current.split(!this.isWindows ? '/' : '\\');
            let lastPart = parts[parts.length-1].trim() || parts[parts.length-2].trim();
            this.getDataList(lastPart);
            this.browseFilesScope.folder.selectedFolder += !this.isWindows ? '/' : '\\';
        }
    }

    openBrowser() {
        if (!this.startFolder) this.initBrowseFileScope();
        else {
            if (this.startFolder.indexOf('\\')>=0 && this.startFolder.indexOf('/')<0) this.isWindows = true;
            this.browseFilesScope.folder.selectedFolder = this.startFolder;
            let parts = this.startFolder.split(!this.isWindows ? '/' : '\\');
            let lastPart = parts[parts.length-1].trim() || parts[parts.length-2].trim();
//            this.browseFilesScope.baseDirectory = '/'+_.filter(parts,(p)=>{return p}).join(!this.isWindows ? '/' : '\\');
            this.browseFilesScope.folderList = _.filter(parts,(p)=>{return p.trim()});
            this.browseFilesScope.folder.currentRoot = parts[0]+(!this.isWindows ? '/' : '\\');
            if (this.isWindows) this.browseFilesScope.folderList.shift();
            this.browseFilesScope.folder.currentFolder = lastPart;
        }
        this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        this._getFileList(this.browseFilesScope.folder.selectedFolder || this.root);
        this.modalInstance = this.modal.launchModal("browse_files_modal", this.browseFilesScope, 'sm');
    }

    upperFolder() {
        let folderList = this.browseFilesScope.folderList;

        if (folderList.length) {
            folderList.pop();

            let currentFolder = folderList.pop();
            if (!currentFolder || (this.isWindows && currentFolder === '/')) {
                currentFolder = this.browseFilesScope.folder.currentRoot;
                this.browseFilesScope.folder.selectedFolder = this.browseFilesScope.folder.currentRoot;
            }
            this.getDataList(currentFolder);

            this.browseFilesScope.folder.currentFolder = currentFolder;
            this.browseFilesScope.baseDirectory = this.browseFilesScope.folder.selectedFolder;
        }
    }

    setSelectedItem(item) {
        this.browseFilesScope.selectedItem = item;
        this.browseFilesScope.folder.currentFolder = item.fileSystemItemName;
        this.browseFilesScope.folder.selectedFolder = (this.browseFilesScope.baseDirectory || '') + (!this.isWindows ? (this.browseFilesScope.baseDirectory == '/' ? '' : '/') : (this.browseFilesScope.baseDirectory ? (!this.browseFilesScope.baseDirectory.endsWith('\\') ? '\\' : '') : this.browseFilesScope.folder.currentRoot)) + item.fileSystemItemName;
    }

    _clearData() {
        this.browseFilesScope.folder.currentFolder = !this.isWindows ? '/' : this.browseFilesScope.folder.currentRoot;
        this.browseFilesScope.folderList = [];
    }

    onChangeRoot() {
        this.$timeout(()=>{
            this.onChangeFolder(true);
        });
    }

    onChangeFolder(newRoot) {
        if (newRoot) {
            this._clearData();
        }
        let folderLabel = '';
        if (this.browseFilesScope.folder.currentFolder == '/') {
            folderLabel = this.browseFilesScope.folder.currentRoot;
        }
        else {
            folderLabel = this.browseFilesScope.folder.currentFolder;
        }
        let index = 0;
        for (index; this.browseFilesScope.folderList.length > index; index++) {
            if (this.browseFilesScope.folderList[index] == folderLabel) {
                break;
            }
        }
        let indexToCut = this.browseFilesScope.folderList.length - index;
        this.browseFilesScope.folderList = _.dropRight(this.browseFilesScope.folderList, indexToCut);
        this.getDataList(folderLabel);
    }

    getDataList(path) {
        let PathSend = '';
        if (path == '/') {
            PathSend = this.browseFilesScope.folder.currentRoot;
        }
        else {
            PathSend = this._selectPath(path);
        }
        this._getFileList(PathSend);
    }

    _getFileList(path) {
        path = path.replace('\\\/\\','\\');
        this.browseFilesDao.query({path: path, includeZip: this.browseFilesScope.canSelectFiles}).$promise.then((result) => {
            if (result) {
                this.browseFilesScope.rootsList = result.roots;
                this.isWindows = result.windows;
                if (this.isWindows && path==='/') {
                    this._getFileList(result.roots[0]);
                    return;
                }

                this.browseFilesScope.fileList = result.fileSystemItems;
                if (!this.browseFilesScope.folder.currentRoot) {
                    this.browseFilesScope.folder.currentRoot = result.roots[0];
                    this.onChangeRoot();
                }
                if (!this.browseFilesScope.folder.selectedFolder) {
                    this.browseFilesScope.folder.selectedFolder = result.roots[0];
                }

                if (this.isWindows)
                    this.browseFilesScope.mountLabel = this.browserOptions.windowsDriveLabel || 'Drive:';
                else
                    this.browseFilesScope.mountLabel = this.browserOptions.nonWindowsMountLabel || 'Mount Point:';

                let onlyFolders = _.filter(result.fileSystemItems,(item)=>{return item.folder});
                this.browseFilesScope.pathAutoComplete = _.map(onlyFolders,(item)=>{
                    return (this.browseFilesScope.baseDirectory||(!this.isWindows ? '' : this.browseFilesScope.folder.currentRoot)) + (!this.isWindows ? (!this.browseFilesScope.baseDirectory || !this.browseFilesScope.baseDirectory.endsWith('/') ? '/' :'') : ((this.browseFilesScope.baseDirectory && !this.browseFilesScope.baseDirectory.endsWith('\\'))?'\\':'')) + item.fileSystemItemName;
                });

                let filteredSelectionList = this.browseFilesScope.canSelectFiles ? result.fileSystemItems : _.filter(result.fileSystemItems,(item)=>{return item.folder});
                this.browseFilesScope.selectionAutoComplete = _.map(filteredSelectionList,(item)=>{
                    return item.fileSystemItemName;
                });
            }
        },
        function (result) {
        });
    }

    _selectPath(path, isFile = false) {
        let backslash = '/';
        let windowSlash = "\\";
        let _path = this.browseFilesScope.folder.currentRoot;
        let PathSend;

        if (isFile && this.selectedFile || this.selectedFile)
            this.browseFilesScope.folderList.pop();

        if (this.browseFilesScope.folderList.length > 0) {
            this.browseFilesScope.folderList.forEach((pathEntry, index) => {
                if (pathEntry != windowSlash && this.isWindows) {
                    _path += pathEntry + windowSlash;
                }
                if (pathEntry != backslash && !this.isWindows) {
                    _path += pathEntry + backslash;
                }
            });
        }

        if (this.browseFilesScope.folder.currentRoot == path) {
            _path = this.browseFilesScope.folder.currentRoot;
            this.browseFilesScope.folder.selectedFolder = this.browseFilesScope.folder.currentRoot;
            this.browseFilesScope.folderList = [];
            this.browseFilesScope.folderList.push(backslash);
        }
        else {
            if (!isFile) {
                this.browseFilesScope.folderList.push(path);
                this.browseFilesScope.folder.currentFolder = path;
            }
            _path += path;
            this.browseFilesScope.folder.selectedFolder = _path;
            this.browseFilesScope.folder.selectedFolder = this.browseFilesScope.folder.selectedFolder.replace('\\\/\\','\\');
            this.browseFilesScope.baseDirectory = this.browseFilesScope.folder.selectedFolder;
            this.selectedFile = isFile;
        }

        return _path;
    }

    setFilePath(path) {
        if (this.browseFilesScope.canSelectFiles)
            this._selectPath(path, true);
    }

    save() {
        this.browserUpdateFolder({directory: this.browseFilesScope.folder.selectedFolder});
        this.closeModal();
    }

    closeModal() {
        this.modalInstance.close();
    }

    onKeyPress(e) {
        if (e.charCode == 13 && this.browseFilesScope.selectedItem)
            this.getDataList(this.browseFilesScope.selectedItem.fileSystemItemName);
    }

    onSelectionChange() {
        this.browseFilesScope.folder.selectedFolder = (this.browseFilesScope.baseDirectory || '') + (!this.isWindows ? (this.browseFilesScope.baseDirectory ? '' : '/') : (this.browseFilesScope.baseDirectory ? (!this.browseFilesScope.baseDirectory.endsWith('\\')?'\\':'') : this.browseFilesScope.folder.currentRoot)) + this.browseFilesScope.folder.currentFolder;
        if (this.isWindows && this.browseFilesScope.folder.selectedFolder.toLowerCase().startsWith(this.browseFilesScope.folder.currentRoot.toLowerCase()+this.browseFilesScope.folder.currentRoot.toLowerCase()))
            this.browseFilesScope.folder.selectedFolder = this.browseFilesScope.folder.selectedFolder.toLowerCase().replace(this.browseFilesScope.folder.currentRoot.toLowerCase()+this.browseFilesScope.folder.currentRoot.toLowerCase(),this.browseFilesScope.folder.currentRoot);
    }

    onPathAutoCompleteSelect(selection) {
        this._gotoPath(selection);
    }
}

export function jfBrowseFiles() {
    return {
        restrict: 'EA',
        scope: {
            browserUpdateFolder: '&',
            browserResource: '=',
            browserOptions: '=',
            startFolder: '='
        },
        controller: jfBrowseFilesController,
        controllerAs: 'jfBrowseFiles',
        templateUrl: 'directives/jf_browse_files/jf_browse_files.html',
        bindToController: true
    };
}