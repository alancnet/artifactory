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

var IOUtils = {
    post:function (url, params) {
        // create form
        var form = document.createElement('form');
        form.method = 'post';
        form.action = url;
        form.style.display = 'none';

        // add fields
        for (var name in params) {
            var value = params[name];
            var field = document.createElement('input');
            field.type = 'hidden';
            field.name = name;
            field.value = value;
            form.appendChild(field);
        }

        // submit form
        document.body.appendChild(form);
        form.submit();
    }
};