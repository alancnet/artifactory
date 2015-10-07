/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

var FileBrowser = function (panelId, inputId) {
    FileBrowser.INSTANCE = this;
    this.input = document.getElementById(inputId);
    this.lastSelection = null;
    this.panelId = panelId;
};

FileBrowser.INSTANCE = null;

FileBrowser.get = function () {
    return FileBrowser.INSTANCE;
};

FileBrowser.prototype.ok = function () {
    if (!this.input.value && this.lastSelection) {
        this.input.value = this.lastSelection.innerHTML;
    }

    var eventScript = this.input.getAttribute('onselection');
    eval(eventScript);
};

FileBrowser.prototype.onFileClick = function (element, e) {
    // set file name
    var fileName = element.innerHTML;
    this.input.value = fileName;

    // mark selected
    if (this.lastSelection) {
        this.lastSelection.className = this.lastSelection.prevClass;
    }

    element.prevClass = element.className;
    element.className += ' selected';
    this.lastSelection = element;
};

FileBrowser.prototype.cancelTextSelection = function () {
    if (window.getSelection) {
        var selection = window.getSelection();
        if (selection) {
            selection.collapseToStart()
        }
    }
};