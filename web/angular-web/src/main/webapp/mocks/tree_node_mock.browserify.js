var faker = require('faker');
function TreeNodeMock(data) {
  data = data || {};
  var name = data.text || faker.name.firstName();
  var treeNodeMock = angular.extend(
    {
      type: _.sample(['file', 'folder', 'archive', 'repository']),
      repoKey: faker.name.firstName(),
      path: faker.name.firstName() + '/' + name,
      archivePath: undefined,
      repoPkgType: 'Maven',
      repoType: 'local',
      local: true,
      text: name
    },
    data
  );
  treeNodeMock.withChildren = function(number) {
    this.children = TreeNodeMock.array(number);
    return this;
  };
  treeNodeMock.expectGetChildren = function(children) {
    inject(function ($httpBackend, RESOURCE) {
      $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=true',
          {type: 'junction', repoKey: treeNodeMock.repoKey, repoType: treeNodeMock.repoType, path: treeNodeMock.path, text: treeNodeMock.text})
          .respond(children);            
    });
  };
  treeNodeMock.expectLoad = function(data) {
    data = data || [{tabs: [], actions: []}];
    inject(function ($httpBackend, RESOURCE) {
      $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER,
          {type: treeNodeMock.type, repoKey: treeNodeMock.repoKey, path: treeNodeMock.path, text: treeNodeMock.text})
          .respond(data);
    });
  };
  return treeNodeMock;
}
TreeNodeMock.array = function(length) {
  var result = [];
  for (var i = 0; i < length; i++) {
    result[i] = TreeNodeMock();
  }
  return result;
};
TreeNodeMock.data = function(data) {
  return [angular.extend(
    {
      tabs: [],
      actions: []
    },
    data || {}
  )];
};
TreeNodeMock.repo = function(name) {
  name = name || 'repo';
  return TreeNodeMock({repoKey: name, path: '', text: name, type: 'repository', hasChild: true, tabs: [], actions: []});
};
TreeNodeMock.folder = function(options = {}) {
  return TreeNodeMock(angular.extend(options, {type: 'folder', hasChild: true}));
};
TreeNodeMock.file = function(options = {}) {
  return TreeNodeMock(angular.extend(options, {type: 'file', hasChild: false}));
};
TreeNodeMock.archive = function(options = {}) {
  return TreeNodeMock(angular.extend(options, {type: 'archive', hasChild: true}));
};

TreeNodeMock.expectGetRoots = function(compacted = true, repos) {
  repos = repos || [TreeNodeMock.repo('repo1'), TreeNodeMock.repo('repo2')];
  inject(function ($httpBackend, RESOURCE) {
    $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=' + compacted, {"type": "root"})
            .respond(repos);
  });
};
TreeNodeMock.expectGetChildren = function(children, compacted = true) {
  inject(function ($httpBackend, RESOURCE) {
    $httpBackend.expectPOST(RESOURCE.API_URL + RESOURCE.TREE_BROWSER + '?compacted=' + compacted)
        .respond(children);            
  });
};
module.exports = TreeNodeMock;
