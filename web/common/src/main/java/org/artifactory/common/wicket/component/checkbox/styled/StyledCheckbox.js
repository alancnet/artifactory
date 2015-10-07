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

var StyledCheckbox = {
    onmouseover:function (button) {
        DomUtils.addHoverStyle(button);
    },

    onmouseout:function (button) {
        DomUtils.removeHoverStyle(button);
    },

    onclick:function (button) {
        var checkbox = button.parentNode.getElementsByTagName('input')[0];
        checkbox.checked = !checkbox.checked;
        StyledCheckbox.updateStyle(button, checkbox.checked);
    },

    update:function (checkbox) {
        var button = checkbox.parentNode.getElementsByTagName('button')[0];
        StyledCheckbox.updateStyle(button, checkbox.checked);
    },

    updateStyle:function (button, checked) {
        if (checked) {
            button.className = 'styled-checkbox styled-checkbox-checked';
        } else {
            button.className = 'styled-checkbox styled-checkbox-unchecked';
        }
    },

    onkeydown:function (id, e) {
        if (e.keyCode == 13) {
            document.getElementById(id).click();
            return false;
        }
    }
};
