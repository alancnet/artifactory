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

package org.artifactory.common.wicket.component.navigation;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.component.links.TitledAjaxLink;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * User: yevgenys Date: Oct 16, 2008 Time: 4:22:45 PM
 */

public class PagingNavigatorWithDropDown extends Panel {
    private IPageable pageable;

    private Integer currentPageIndex = 0;
    private static final String PREV_PAGE = "prevPage";
    private static final String NEXT_PAGE = "nextPage";
    private DropDownChoice<Integer> pageableDropDown;

    public PagingNavigatorWithDropDown(String id, IPageable pageable) {
        super(id);
        this.pageable = pageable;
        setOutputMarkupId(true);
    }

    @Override
    protected void onBeforeRender() {
        // already added ?
        if (pageableDropDown != null) {
            // make sure the pages count and current index are in sync
            // this is necessary in case the number of results or current page were changed
            // by someone else (for example new search results)
            pageableDropDown.setChoices(getPagesNumbers());
            currentPageIndex = pageable.getCurrentPage();
            updateButtons();
            super.onBeforeRender();
            return;
        }

        TitledAjaxLink link;

        // prev link
        link = new TitledAjaxLink(PREV_PAGE) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                // no more prev pages ?
                if (shouldDisablePrevButton()) {
                    // update the buttons state
                    updateButtons(target);
                    return;
                }

                currentPageIndex--;

                // changing the page index
                pageable.setCurrentPage(pageable.getCurrentPage() - 1);

                // render it
                target.add((Component) pageable);

                // update the buttons state
                updateButtons(target);
            }
        };
        link.setEnabled(!shouldDisablePrevButton());
        add(link);

        // pageable drop down
        pageableDropDown = new DropDownChoice<>("pageableDropDown", new Model<Integer>() {
            @Override
            public Integer getObject() {
                return currentPageIndex;
            }

            @Override
            public void setObject(Integer object) {
                if (object == null) {
                    return;
                }
                currentPageIndex = object;
            }
        }, getPagesNumbers());
        pageableDropDown.setOutputMarkupId(true);
        pageableDropDown.setChoiceRenderer(new ChoiceRenderer<Integer>() {
            @Override
            public Object getDisplayValue(Integer object) {
                if (object == null) {
                    return null;
                }

                // displaying the "Page X of Y"
                return format("Page %d of %d", object + 1, pageable.getPageCount());
            }

        });

        // adding ajax
        pageableDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (currentPageIndex == null) {
                    return;
                }

                // changing the page
                pageable.setCurrentPage(currentPageIndex);

                // render it
                target.add((Component) pageable);

                updateButtons(target);

            }
        });
        add(pageableDropDown);

        // next link
        link = new TitledAjaxLink(NEXT_PAGE) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                // no more next pages ?
                if (shouldDisableNextButton()) {
                    // update the buttons state
                    updateButtons(target);
                    return;
                }

                // inc the index
                currentPageIndex++;

                // changing the page index
                pageable.setCurrentPage(pageable.getCurrentPage() + 1);

                // render it !!!
                target.add((Component) pageable);

                // update the buttons state
                updateButtons(target);
            }
        };
        link.setEnabled(!shouldDisableNextButton());
        add(link);

        super.onBeforeRender();

    }


    private void updateButtons() {
        updateButton(null, PREV_PAGE, !shouldDisablePrevButton());
        updateButton(null, NEXT_PAGE, !shouldDisableNextButton());
    }

    private void updateButtons(AjaxRequestTarget target) {
        updateButton(target, PREV_PAGE, !shouldDisablePrevButton());
        updateButton(target, NEXT_PAGE, !shouldDisableNextButton());
    }

    private boolean shouldDisableNextButton() {
        return pageable.getCurrentPage() > pageable.getPageCount() - 2;
    }

    private boolean shouldDisablePrevButton() {
        return pageable.getCurrentPage() < 1;
    }

    private void updateButton(AjaxRequestTarget target, String buttonId, boolean state) {
        Component c = get(buttonId);

        // rendering the button ONLY if the state was changed
        if (c.isEnabled() != state) {
            c.setEnabled(state);
            if (target != null) {
                target.add(c);
            }
        }
    }

    private List<Integer> getPagesNumbers() {
        List<Integer> pages = new ArrayList<>(pageable.getPageCount());

        // filling the list
        for (int i = 0; i < pageable.getPageCount(); i++) {
            pages.add(i);
        }

        return pages;
    }
}
