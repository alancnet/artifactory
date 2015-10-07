import TreeNodeMock from '../../mocks/tree_node_mock.browserify';

describe('native browser', () => {
	let nativeBrowser, TreeNode;
	function setup(NativeBrowser, _TreeNode_) {
		nativeBrowser = NativeBrowser;
		TreeNode = _TreeNode_;
	}
	beforeEach(m('artifactory.services', 'artifactory.dao'));
	beforeEach(inject(setup));
	describe('isAllowed', () => {
		it('should be true for repo', () => {
			let node = new TreeNode(TreeNodeMock.repo());
			expect(nativeBrowser.isAllowed(node)).toBe(true);
		});
		it('should be true for folder', () => {
			let node = new TreeNode(TreeNodeMock.folder());
			expect(nativeBrowser.isAllowed(node)).toBe(true);
		});
		it('should be false for file', () => {
			let node = new TreeNode(TreeNodeMock.file());
			expect(nativeBrowser.isAllowed(node)).toBe(false);
		});
		it('should be false for folder inside archive', () => {
			let node = new TreeNode(TreeNodeMock.folder({archivePath: 'archive'}));
			expect(nativeBrowser.isAllowed(node)).toBe(false);
		});
	});
	describe('pathFor', () => {
		it('should return the path of the native browser', () => {
			let node = new TreeNode(TreeNodeMock.file({repoKey: 'repo1', path: 'file'}));
			expect(nativeBrowser.pathFor(node)).toBe('../list/repo1/file/');
		});
		it('should not add a / in the end if it already exists', () => {
			let node = new TreeNode(TreeNodeMock.file({repoKey: 'repo1', path: 'file'}));
			expect(nativeBrowser.pathFor(node)).toBe('../list/repo1/file/');
		});
	});
});