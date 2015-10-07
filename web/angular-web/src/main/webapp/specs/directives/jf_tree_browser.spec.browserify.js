'use strict';
var UserMock = require('../../mocks/user_mock.browserify.js');
var TreeNodeMock = require('../../mocks/tree_node_mock.browserify.js');
var JsTreeObject = require('../page_objects/js_tree_object.browserify.js');
var mockStorage = require('../../mocks/artifactory_storage_mock.browserify.js');
describe('unit test:jf_tree_browser directive', function () {
  var element,
    $scope,
    httpBackend,
    RESOURCE,
    repo1,
    repo2,
    child,
    jsTreeObject,
    ArtifactoryEventBus;

  mockStorage();

  function setup(TreeBrowserDao, TreeNode, $q, $httpBackend, _RESOURCE_, _ArtifactoryEventBus_) {
      httpBackend = $httpBackend;
      RESOURCE = _RESOURCE_;
      repo1 = new TreeNode(TreeNodeMock.repo('repo1'));
      repo2 = new TreeNode(TreeNodeMock.repo('repo2'));
      child = new TreeNode(TreeNodeMock.file({text: 'file'}));
      ArtifactoryEventBus = _ArtifactoryEventBus_;
      spyOn(ArtifactoryEventBus, 'dispatch').and.callThrough();
      UserMock.mockCurrentUser();
  }

  function compileDirective() {
    $scope = compileHtml('<jf-tree-browser></jf-tree-browser>');
    flush();
    element = angular.element(document.body).find('jf-tree-browser')[0];
    jsTreeObject = new JsTreeObject();
  }

  function repo1Item() {
    return jsTreeObject.getNode(jsTreeObject.getRootItem().children[0]);
  }
  function childItem() {
    return jsTreeObject.getNode(repo1Item().children[0]);
  }

  function flush() {
      httpBackend.flush();
  }

  beforeEach(m('artifactory.templates', 'artifactory.states'));
  beforeEach(inject(setup));
  beforeEach(function() {
    TreeNodeMock.expectGetRoots();
  });
  beforeEach(compileDirective);

  it('should show tree', function() {
    expect(element).toBeDefined();
    expect(jsTreeObject.getNodeWithText('repo1')).toBeDefined();
  });
  it('should allow to expand a repo', function() {
    repo1.expectGetChildren([child]);
    jsTreeObject.expandFirstItem();
    flush();
    expect(jsTreeObject.getNodeWithText('file')).toBeDefined();
  });
  it('should allow to load a repo and its children on click', function() {
    repo1.expectGetChildren([child]);
    jsTreeObject.loadNodeItem('repo1');
    $scope.$digest();
    expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', repo1Item());
  });
  it('should allow to load a node', function() {
    repo1.expectGetChildren([child]);
    jsTreeObject.expandFirstItem();
    flush();
    child.expectLoad(TreeNodeMock.data());
    jsTreeObject.loadNodeItem('file');
    flush();
    expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', childItem());
  });

});
