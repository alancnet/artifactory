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

package org.artifactory.common.wicket.component.panel.shortcutkey;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.artifactory.common.wicket.component.template.HtmlTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * ShortcutKeyHandler model holds the clicked key.
 *
 * @author Yoav Aharoni
 */
public class KeyEventHandler extends Panel {
    private Map<Integer, KeyListener> listenerMap = new HashMap<>();

    public KeyEventHandler(String id) {
        super(id, new Model());

        HiddenField keyCodeField = new HiddenField<>("keyCodeField", (IModel<Integer>) getDefaultModel(),
                Integer.class);
        keyCodeField.setOutputMarkupId(true);
        keyCodeField.add(new AjaxFormComponentUpdatingBehavior("onkeyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onKeyUp(getKeyCode(), target);
            }
        });
        add(keyCodeField);

        HtmlTemplate template = new HtmlTemplate("initScript");
        template.setParameter("keyCodeField", new PropertyModel(keyCodeField, "markupId"));
        template.setParameter("keys", new KeysArrayModel());
        add(template);
    }

    protected void onKeyUp(Integer keyCode, AjaxRequestTarget target) {
        KeyListener keyListener = listenerMap.get(keyCode);
        if (keyListener != null) {
            KeyReleasedEvent event = new KeyReleasedEvent(keyCode, target);
            keyListener.keyReleased(event);
        }

    }

    public Integer getKeyCode() {
        return (Integer) getDefaultModelObject();
    }

    public void addKeyListener(KeyListener listener, Integer... keyCodes) {
        if (ArrayUtils.isEmpty(keyCodes)) {
            throw new IllegalArgumentException("got empty array of keyCodes");
        }

        for (Integer keyCode : keyCodes) {
            listenerMap.put(keyCode, listener);
        }
    }

    private class KeysArrayModel extends AbstractReadOnlyModel<String> {
        @Override
        public String getObject() {
            StringBuilder buf = new StringBuilder();
            for (Integer keyCode : listenerMap.keySet()) {
                buf.append(',').append(keyCode);
            }
            return buf.substring(1);
        }
    }
}
