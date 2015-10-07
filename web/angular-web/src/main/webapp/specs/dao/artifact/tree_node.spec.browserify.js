'use strict';
var TreeNodeMock = require('../../../mocks/tree_node_mock.browserify.js');
var mockStorage = require('../../../mocks/artifactory_storage_mock.browserify.js');
describe('unit test:tree node dao', function () {

    var node;
    var leafnode;
    var RESOURCE;
    var httpBackend;
    var treeBrowserDao;
    var TreeNode;
    var $rootScope;
    mockStorage();

    // inject the main module
    beforeEach(m('artifactory.dao'));
 
    beforeEach(inject(function (TreeBrowserDao, _RESOURCE_, $httpBackend, _TreeNode_, _$rootScope_) {
        RESOURCE = _RESOURCE_;
        httpBackend = $httpBackend;
        TreeNode = _TreeNode_;
        $rootScope = _$rootScope_;
        node = new TreeNode(TreeNodeMock.folder());
        leafnode = new TreeNode(TreeNodeMock.file());
        treeBrowserDao = TreeBrowserDao;
        spyOn(treeBrowserDao, 'invalidateRoots').and.callThrough();
    }));

    function flush() {
        httpBackend.flush();
    }

    describe('data transform', function() {
        it('should fill in fullpath for repo', function() {
            node = new TreeNode(TreeNodeMock.repo('repository'));
            expect(node.fullpath).toEqual('repository');
        });
        it('should fill in fullpath for file / folder', function() {
            node = new TreeNode(TreeNodeMock({repoKey: 'repo', path: 'path/c.bin'}));
            expect(node.fullpath).toEqual('repo/path/c.bin');
        });
        it('should fill in fullpath for archive', function() {
            node = new TreeNode(TreeNodeMock({repoKey: 'repo', path: 'archive.jar/c.bin', archivePath: 'archive.jar'}));
            expect(node.fullpath).toEqual('repo/archive.jar!/c.bin');
        });
        it('should fill in hasChild for a node with children', function() {
            node = new TreeNode(TreeNodeMock().withChildren(3));
            expect(node.hasChild).toBe(true);
        });
        it('should not fill in hasChild for a node with no children', function() {
            node = new TreeNode(TreeNodeMock());
            expect(node.hasChild).toBeFalsy();
        });
    });

    it('getChildren for leaf node', function(done) {
        leafnode.getChildren()
        .then((children) => {
            expect(children).toBeNull();
            done();
        });
        $rootScope.$digest();
    });
    describe('getChildren', function() {
        beforeEach(function() {
            node.expectGetChildren(TreeNodeMock.array(2));
        });
        it('should send a POST request and return an array of TreeNodes', function (done) {
            node.getChildren()
            .then(function(children) {
                expect(children.length).toBe(2);
                expect(children[0].constructor.name).toBe('TreeNode');
                expect(children[0].parent).toBe(node);
                done();
            });
            flush();
        });

        it('should return the cached promise if called twice', function () {
            var first = node.getChildren();
            var second = node.getChildren();
            expect(second).toBe(first);
        });

        it('should make another POST request if force = true', function () {
            node.expectGetChildren(TreeNodeMock.array(2));
            var first = node.getChildren();
            var second = node.getChildren(true);
            expect(second).not.toBe(first);
        });

        it('should make another POST request if invalidating children', function () {
            node.expectGetChildren(TreeNodeMock.array(2));
            var first = node.getChildren();
            node.invalidateChildren();
            var second = node.getChildren();
            expect(second).not.toBe(first);
        });
        it('should allow to invalidate parent', function (done) {
            node.getChildren()
            .then(function(children) {
                children[0].invalidateParent();
                node.expectGetChildren(TreeNodeMock.array(2));
                node.getChildren();
                done();
            });
            flush();
        });    
    });
    describe('invalidate parents', function() {
        it('should allow to invalidate parent of root', function () {
            node.invalidateParent();
            expect(treeBrowserDao.invalidateRoots).toHaveBeenCalled();
        });    
    });
    describe('load', function() {
        it('should send a POST request', function(done) {
            var loadedNodeData = TreeNodeMock.data();
            node.expectLoad(loadedNodeData);
            node.load().then(function(result) {
                expect(result).toBe(node);
                expect(node.tabs).toEqual(loadedNodeData[0].tabs);
                expect(node.actions).toEqual(loadedNodeData[0].actions);
                done();
            });
            flush();
        });
        it('should not send a POST request if tabs already exist', function() {
            node.tabs = [];
            node.load().then(function(result) {
                expect(result).toBe(node);
                expect(node.tabs).toEqual([]);
                expect(node.actions).not.toBeDefined();
            });
        });
    });

});