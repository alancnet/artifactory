'use strict';
import StateParamsMock from '../../mocks/state_params_mock.browserify';
import UserMock from '../../mocks/user_mock.browserify';
import TreeNodeMock from '../../mocks/tree_node_mock.browserify';
import JsTreeObject from '../page_objects/js_tree_object.browserify';
import mockStorage from '../../mocks/artifactory_storage_mock.browserify';
import EVENTS     from '../../app/constants/artifacts_events.constants';
import KEYS       from '../../app/constants/keys.constants';
describe('unit test:jf_simple_browser directive', () => {
  let simpleBrowserElement,
    $scope,
    $timeout,
    httpBackend,
    RESOURCE,
    TreeBrowserDao,
    repo1,
    repo2,
    child,
    jsTreeObject,
    stateParams,
    ArtifactoryEventBus,
    ArtifactoryState;

  mockStorage();

  function setup(_TreeBrowserDao_, TreeNode, _$timeout_, $httpBackend, _RESOURCE_, _ArtifactoryEventBus_, _ArtifactoryState_) {
      httpBackend = $httpBackend;
      RESOURCE = _RESOURCE_;
      $timeout = _$timeout_;
      TreeBrowserDao = _TreeBrowserDao_;
      repo1 = new TreeNode(TreeNodeMock.repo('repo1'));
      repo2 = new TreeNode(TreeNodeMock.repo('repo2'));
      child = new TreeNode(TreeNodeMock.file({text: 'file', path: 'file'}));
      ArtifactoryEventBus = _ArtifactoryEventBus_;
      ArtifactoryState = _ArtifactoryState_;
      spyOn(ArtifactoryEventBus, 'dispatch').and.callThrough();
      UserMock.mockCurrentUser();      
  }

  function compileDirective() {
    $scope = compileHtml('<jf-simple-browser></jf-simple-browser>');
    flush();
    simpleBrowserElement = angular.element(document.body).find('jf-simple-browser')[0];
    jsTreeObject = new JsTreeObject();
  }

  function twoDotsItem() {
    return jsTreeObject.getNodeWithText(/\.\./);
  }
  function repo1Item() {
    return jsTreeObject.getNodeWithText('repo1');
  }
  function repo2Item() {
    return jsTreeObject.getNodeWithText('repo2');
  }
  function fileItem() {
    return jsTreeObject.getNodeWithText('file');
  }

  function flush() {
      httpBackend.flush();
  }

  function drillDownRepo1() {
    repo1.expectGetChildren([child]);
    repo1Item().click();
    flush();
  }

  beforeEach(m('artifactory.templates', 'artifactory.states'));
  beforeEach(() => {
    stateParams = {};
    StateParamsMock(stateParams);
  });

  beforeEach(inject(setup));

  beforeEach(() => {
    TreeNodeMock.expectGetRoots();
  });

  describe('with no artifact in stateParams', () => {
    beforeEach(compileDirective);

    it('should show tree', () => {
      expect(simpleBrowserElement).toBeDefined();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).toBeDefined();
      expect(fileItem()).not.toBeDefined();
      expect(twoDotsItem()).not.toBeDefined();
    });

    it('should allow to drill down to a repo', (done) => {
      drillDownRepo1();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      TreeBrowserDao.getRoots()
        .then((roots) => {
          expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', {data: roots[0]});
          done();
        });
      $scope.$digest();
    });

    it('should not drill down to a file', (done) => {
      drillDownRepo1();
      child.expectLoad(TreeNodeMock.data());
      fileItem().click();
      flush();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      TreeBrowserDao.getRoots()
        .then((roots) => {
          return roots[0].getChildren();
        })
        .then((children) => {
          expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', {data: children[0]});
          done();
        });
      $scope.$digest();
    });

    it('should allow to go up', () => {
      drillDownRepo1();
      twoDotsItem().click();
      $scope.$digest();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).toBeDefined();
    });
  });
  describe('with artifact in stateParams, tree untouched', () => {
    beforeEach(() => {
      stateParams.artifact = 'repo1';
      compileDirective();
    });
    it('should activate repo1 but not drill down', (done) => {
      expect(simpleBrowserElement).toBeDefined();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).toBeDefined();
      expect(fileItem()).not.toBeDefined();
      expect(twoDotsItem()).not.toBeDefined();
      expect($(repo1Item())).toHaveClass('jstree-clicked');
      TreeBrowserDao.getRoots()
        .then((roots) => {
          expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', {data: roots[0]});
          done();
        });
      $scope.$digest();
    });
  });
  describe('with artifact state, tree touched', () => {
    beforeEach(() => {
      ArtifactoryState.setState('tree_touched', true);
      stateParams.artifact = 'repo1';
      repo1.expectGetChildren([child]);
      compileDirective();
    });
    it('should activate repo1 & drill down into it', (done) => {
      expect(simpleBrowserElement).toBeDefined();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      expect(twoDotsItem()).toBeDefined();
      expect($(repo1Item())).toHaveClass('jstree-clicked');
      TreeBrowserDao.getRoots()
        .then((roots) => {
          expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', {data: roots[0]});
          done();
        });
      $scope.$digest();
    });
  });
  describe('with artifact in stateParams of file', () => {
    beforeEach(() => {
      stateParams.artifact = 'repo1/file';
      repo1.expectGetChildren([child]);
      child.expectLoad();
      compileDirective();
    });
    it('should activate repo1/file', (done) => {
      expect(simpleBrowserElement).toBeDefined();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(twoDotsItem()).toBeDefined();
      expect($(fileItem())).toHaveClass('jstree-clicked');
      TreeBrowserDao.getRoots()
        .then((roots) => {
          return roots[0].getChildren();
        })
        .then((children) => {
          expect(ArtifactoryEventBus.dispatch).toHaveBeenCalledWith('tree:node:select', {data: children[0]});
          done();
        });
      $scope.$digest();
    });
  });
  describe('events', () => {
    beforeEach(compileDirective);
    describe('search', () => {
      beforeEach(() => {
        ArtifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CHANGE, "rep");
      });
      it('should mark search results', () => {
        expect($(repo1Item())).toHaveClass('jstree-search');
        expect($(repo2Item())).toHaveClass('jstree-search');
        expect($(repo1Item())).not.toHaveClass('jstree-hovered');
        expect($(repo2Item())).toHaveClass('jstree-hovered');
      });
      it('should search next result when pressing arrow key down', () => {
        ArtifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, KEYS.DOWN_ARROW);
        expect($(repo1Item())).toHaveClass('jstree-hovered');
        expect($(repo2Item())).not.toHaveClass('jstree-hovered');
      });
      it('should search next result when pressing arrow key up', () => {
        ArtifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_KEYDOWN, KEYS.UP_ARROW);
        expect($(repo1Item())).toHaveClass('jstree-hovered');
        expect($(repo2Item())).not.toHaveClass('jstree-hovered');
      });
      it('should cancel search', () => {
        ArtifactoryEventBus.dispatch(EVENTS.TREE_SEARCH_CANCEL);
        expect($(repo1Item())).not.toHaveClass('jstree-search');
        expect($(repo2Item())).not.toHaveClass('jstree-search');
      });
    });
    it('should drill down to repo after deploy', () => {
      repo1.expectGetChildren([child]);
      ArtifactoryEventBus.dispatch(EVENTS.ACTION_DEPLOY, "repo1");
      flush();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
    });
    it('should reload node after refresh', (done) => {
      drillDownRepo1();
      repo1.expectGetChildren([child]);
      TreeBrowserDao.getRoots()
        .then((roots) => {
          ArtifactoryEventBus.dispatch(EVENTS.ACTION_REFRESH, roots[0]);
          done();
        });
      $scope.$digest();
    });
    it('should go up after delete', (done) => {
      drillDownRepo1();
      TreeNodeMock.expectGetRoots();
      TreeBrowserDao.getRoots()
        .then((roots) => {
          ArtifactoryEventBus.dispatch(EVENTS.ACTION_DELETE, roots[0]);
          setTimeout(() => { // Must put in timeout, because can't call $timeout.flush when digest is going on
            $timeout.flush();
            expect(repo1Item()).toBeDefined();
            expect(repo2Item()).toBeDefined();
            done();
          });
        });
      $scope.$digest();
    });
    let targetOptions = {
      target: {
        targetRepoKey: 'repo1',
        targetPath: '',
      },
      node: {
        data: {
          text: 'file'
        }
      }
    };
    it('should open target node after move', () => {
      ArtifactoryState.setState('tree_touched', true);
      TreeNodeMock.expectGetRoots();
      repo1.expectGetChildren([child]);
      child.expectLoad(TreeNodeMock.data());
      ArtifactoryEventBus.dispatch(EVENTS.ACTION_MOVE, targetOptions);
      $timeout.flush();
      flush();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      expect($(fileItem())).toHaveClass('jstree-clicked');
    });

    it('should open target node after copy', () => {
      ArtifactoryState.setState('tree_touched', true);
      TreeNodeMock.expectGetRoots();
      repo1.expectGetChildren([child]);
      child.expectLoad(TreeNodeMock.data());
      ArtifactoryEventBus.dispatch(EVENTS.ACTION_COPY, targetOptions);
      $timeout.flush();
      flush();
      expect(repo1Item()).toBeDefined();
      expect(fileItem()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      expect($(fileItem())).toHaveClass('jstree-clicked');
    });

    it('should reload node after refresh', () => {
      TreeNodeMock.expectGetRoots();
      ArtifactoryEventBus.dispatch(EVENTS.TREE_COMPACT);
      flush();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).toBeDefined();
      expect(fileItem()).not.toBeDefined();
    });
    it('should reload node after change url', () => {
      repo1.expectGetChildren([child]);
      child.expectLoad(TreeNodeMock.data());
      ArtifactoryEventBus.dispatch(EVENTS.ARTIFACT_URL_CHANGED, {browser: 'simple', artifact: 'repo1/file'});
      flush();
      expect(repo1Item()).toBeDefined();
      expect(repo2Item()).not.toBeDefined();
      expect(fileItem()).toBeDefined();
      expect($(fileItem())).toHaveClass('jstree-clicked');
    });
  });
});
