'use strict';
var TreeNodeMock = require('../../../mocks/tree_node_mock.browserify.js');
var mockStorage = require('../../../mocks/artifactory_storage_mock.browserify.js');
describe('unit test:tree browser dao', function () {

    var treeBrowserDao;
    var RESOURCE;
    var httpBackend;
    var children;
    var subchildren;
    var archive;
    mockStorage();

    // inject the main module
    beforeEach(m('artifactory.dao'));
 
    beforeEach(inject(function (TreeBrowserDao, _RESOURCE_, $httpBackend) {
        treeBrowserDao = TreeBrowserDao;
        RESOURCE = _RESOURCE_;
        httpBackend = $httpBackend;
        children = [
                TreeNodeMock.folder({text: 'folder', repoKey: 'repo1', path: 'folder'}),
                TreeNodeMock.file({text: 'file', repoKey: 'repo1', path: 'file'}),
                TreeNodeMock.archive({text: 'archive', repoKey: 'repo1', path: 'archive'})
            ];
        subchildren = [
                TreeNodeMock.file({text: 'file1', repoKey: 'repo1', path: 'folder/file1'}),
                TreeNodeMock.file({text: 'file2', repoKey: 'repo1', path: 'folder/file2'})
            ];
        archive = [
                TreeNodeMock.folder({text: 'folder', repoKey: 'repo1', archivePath: 'archive!/folder', path: 'archive/folder', children:[
                    TreeNodeMock.file({text: 'file1', repoKey: 'repo1', archivePath: 'archive!/folder/file1', path: 'archive/folder/file1'}),
                    TreeNodeMock.file({text: 'file2', repoKey: 'repo1', archivePath: 'archive!/folder/file2', path: 'archive/folder/file2'})
                ]})
            ];
    }));

    function flush() {
        httpBackend.flush();
    }

    describe('compactFolders', function() {
        it('should allow to get compactFolders', function() {
            expect(treeBrowserDao.getCompactFolders()).toBe(true);
        });
        it('should allow to set compactFolders', function() {
            treeBrowserDao.setCompactFolders(false);
            expect(treeBrowserDao.getCompactFolders()).toBe(false);
        });
    });

    describe('getRoots', function() {
        it('should send a POST request and return an array of TreeNodes', function (done) {
            TreeNodeMock.expectGetRoots();
            treeBrowserDao.getRoots()
            .then(function(roots) {
                expect(roots.length).toBe(2);
                expect(roots[0].constructor.name).toBe('TreeNode');
                done();
            });
            flush();
        });
        it('should send a POST request with compacted = true', function (done) {
            TreeNodeMock.expectGetRoots(false);
            treeBrowserDao.setCompactFolders(false);
            treeBrowserDao.getRoots()
            .then(function(roots) {
                expect(roots.length).toBe(2);
                expect(roots[0].constructor.name).toBe('TreeNode');
                done();
            });
            flush();
        });
        it('should return the cached promise if called twice', function () {
            TreeNodeMock.expectGetRoots();
            var first = treeBrowserDao.getRoots();
            var second = treeBrowserDao.getRoots();
            expect(second).toBe(first);
        });

        it('should make another POST request if force = true', function () {
            TreeNodeMock.expectGetRoots();
            TreeNodeMock.expectGetRoots();
            var first = treeBrowserDao.getRoots();
            var second = treeBrowserDao.getRoots(true);
            expect(second).not.toBe(first);
        });

        it('should make another POST request if invalidating roots', function () {
            TreeNodeMock.expectGetRoots();
            TreeNodeMock.expectGetRoots();
            var first = treeBrowserDao.getRoots();
            treeBrowserDao.invalidateRoots();
            var second = treeBrowserDao.getRoots();
            expect(second).not.toBe(first);
        });
    });

    describe('findNodeByFullPath', function() {
        it('should allow to load a path to a regular file / folder', function(done) {
            TreeNodeMock.expectGetRoots();
            TreeNodeMock.expectGetChildren(children);
            TreeNodeMock.expectGetChildren(subchildren);
            treeBrowserDao.findNodeByFullPath('repo1/folder/file1')
                .then((node) => {
                    expect(node.type).toEqual('file');
                    expect(node.text).toEqual('file1');
                    expect(node.parent.text).toEqual('folder');
                    expect(node.parent.parent.text).toEqual('repo1');
                    done();
                });
            flush();
        });
        it('should allow to load a path to a file inside an archive', function(done) {
            TreeNodeMock.expectGetRoots();
            TreeNodeMock.expectGetChildren(children);
            TreeNodeMock.expectGetChildren(archive);
            treeBrowserDao.findNodeByFullPath('repo1/archive!/folder/file1')
                .then((node) => {                    
                    expect(node.type).toEqual('file');
                    expect(node.text).toEqual('file1');
                    expect(node.parent.text).toEqual('folder');
                    expect(node.parent.parent.text).toEqual('archive');
                    expect(node.parent.parent.parent.text).toEqual('repo1');
                    done();
                });
            flush();
        });
    });
});