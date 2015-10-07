export class NativeBrowser {
    isAllowed(node) {
        if (node.isInsideArchive()) return false;
        return node.isFolder() || node.isRepo();
    }
    pathFor(node) {
    	let path = '../list/' + node.fullpath;
    	if (!_.endsWith(path, '/')) path = path + '/'; // add '/' in the end
      return path;
    }    
}