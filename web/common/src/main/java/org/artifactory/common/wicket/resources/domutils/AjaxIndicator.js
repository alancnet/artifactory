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

/*-- AjaxIndicator --*/

function AjaxIndicator(divId) {
    this._divId = divId;
    this._counter = 0;
    this.enabled = true;
    this.delayed = true;
}

AjaxIndicator.waiting = false;

AjaxIndicator.prototype.getDiv = function () {
    return document.getElementById(this._divId);
};


AjaxIndicator.prototype.disableDelay = function () {
    this.delayed = false;
};


AjaxIndicator.prototype.disableOnce = function () {
    this.enabled = false;
};

AjaxIndicator.prototype.show = function () {
    if (!this.enabled) {
        this.enabled = true;
        return;
    }

    this._counter++;
    if (this._counter != 1) {
        return;
    }

    AjaxIndicator.waiting = true;

    var myDiv = this.getDiv();
    if (myDiv) {
        if (dojo.isIE == 6) {
            window.onscroll = AjaxIndicator.positionMessage;
            AjaxIndicator.positionMessage();
        }
        myDiv.style.display = 'block';
        if (this.delayed) {
            setTimeout(this.showIndicator, 500);
        } else {
            this.showIndicator();
        }
    }

    this.delayed = true;
};

AjaxIndicator.prototype.showIndicator = function () {
    if (ajaxIndicator._counter) {
        var doc = document.documentElement;
        var msgDiv = document.getElementById('ajaxIndicatorMessage');
        msgDiv.style.top = (doc.clientHeight - msgDiv.clientHeight) / 2 + 'px';
        msgDiv.style.left = (doc.clientWidth - msgDiv.clientWidth) / 2 + 'px';

        document.getElementById('ajaxIndicator').style.visibility = 'visible';
    }
};

AjaxIndicator.positionMessage = function () {
    var doc = document.documentElement;
    var myDiv = ajaxIndicator.getDiv();
    myDiv.style.top = doc.scrollTop + 'px';
    myDiv.style.left = doc.scrollLeft + 'px';
};

AjaxIndicator.prototype.hide = function () {
    this._counter = this._counter ? this._counter - 1 : 0;
    if (this._counter > 0) {
        return;
    }

    AjaxIndicator.waiting = false;

    var myDiv = this.getDiv();
    if (myDiv) {
        window.onscroll = null;
        myDiv.style.display = 'none';
        document.getElementById('ajaxIndicator').style.visibility = 'hidden';
    }
};

/*-- AjaxIndicator Globals --*/

var ajaxIndicator = new AjaxIndicator('ajaxIndicatorContainer');

/**
 * Global Ajax CallHandler
 */
window.wicketGlobalPreCallHandler = function () {
    ajaxIndicator.show();
};

/**
 * Global Ajax CallHandler
 */
window.wicketGlobalPostCallHandler = function () {
    ajaxIndicator.hide();
    DojoUtils.dijitCleanup();
};