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

dojo.require("dojo.parser");
dojo.require("dijit.form.ComboBox");

dojo.declare('artifactory.HistoryComboBox', dijit.form.ComboBox, {
    CLEAR:'Clear History',

    /* @Override */
    postMixInProperties:function () {
        this._restoreHistory();
        this.inherited(arguments);
    },

    /* @Override */
    _startSearch:function () {
        var _this = this;

        //-- copy & paste from ComboBox.js
        var popupId = this.id + "_popup";
        if (!this._popupWidget) {
            this._popupWidget = new dijit.form._ComboBoxMenu({
                onChange:dojo.hitch(this, this._selectOption),
                id:popupId
            });
            dijit.removeWaiState(this.focusNode, "activedescendant");
            dijit.setWaiState(this.textbox, "owns", popupId); // associate popup with textbox

            //-- override _popupWidget.createOptions
            //   to add "Clear History" link set items CSS class
            var superCreate = this._popupWidget.createOptions;
            this._popupWidget.createOptions = function () {
                // call super
                var result = superCreate.apply(_this._popupWidget, arguments);

                // add css class to items
                dojo.forEach(this.domNode.childNodes, function (item) {
                    var className = _this.css[item.innerHTML];
                    if (className) {
                        item.className += ' ' + className;
                    }
                });

                // add "Clear History" item
                var clearItem = _this._createClearHistoryItem();
                this.domNode.insertBefore(clearItem, this.nextButton);

                return result;
            }
        }

        this.inherited(arguments);
    },


    /* @Override */
    destroy:function () {
        delete this.css;
        this.inherited(arguments);
    },

    clearHistory:function () {
        // delete cookie
        dojo.cookie(this.cookie, null, {expires:-1});
        // remove from <select>
        var options = this.store.root.options;
        var l = options.length;
        for (var i = 0; i < l; i++) {
            var option = options[i];
            if (option.hist) {
                option.parentNode.removeChild(option);
                l--;
                i--;
            } else {
                return;
            }
        }
    },

    /**
     * Add current value to history list.
     */
    addHistory:function (value) {
        if (value && !this._optionExist(value)) {
            // add value to <select> field
            var option = document.createElement('option');
            option.innerHTML = value;
            option.value = value;
            this.store.root.appendChild(option);

            // add value to cookie
            var history = dojo.cookie(this.cookie);
            if (history) {
                history += '|' + value;
            } else {
                history = value;
            }
            dojo.cookie(this.cookie, history, {expires:3650})
        }
    },

    _createClearHistoryItem:function () {
        var _this = this;
        var clearItem = dojo.doc.createElement("li");
        clearItem.className = 'dijitReset dijitMenuItem sep clear-history';
        clearItem.innerHTML = _this.CLEAR;
        clearItem.item = {};
        dojo.connect(clearItem, 'onclick', function (e) {
            _this.clearHistory();
            e.stopPropagation();
            e.preventDefault();
        });
        return clearItem;
    },

    _optionExist:function (value) {
        var options = this.store.root.options;
        var l = options.length;
        for (var i = 0; i < l; i++) {
            if (options[i].innerHTML == value) {
                return true;
            }
        }
        return false;
    },

    _restoreHistory:function () {
        var node = this.srcNodeRef;

        // setup css
        this.css = {};

        // read cookie
        var cookie = node.getAttribute('cookie');
        this.cookie = cookie;
        var history = dojo.cookie(cookie);
        if (history) {
            // define utils
            function toOption(value) {
                var option = document.createElement('option');
                option.innerHTML = value;
                option.value = value;
                option.hist = true;
                return option;
            }

            var add = function (option) {
                node.appendChild(option);
            };

            var firstOption = node.options[0];
            if (firstOption) {
                this.css[firstOption.value] = 'sep';
                add = function (option) {
                    node.insertBefore(option, node.firstChild);
                };
            }

            // add history options
            dojo.forEach(history.split('|'), function (value) {
                add(toOption(value));
            });
        }
    }
});
