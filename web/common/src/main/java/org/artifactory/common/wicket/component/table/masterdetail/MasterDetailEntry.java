package org.artifactory.common.wicket.component.table.masterdetail;

import org.artifactory.common.wicket.WicketProperty;

import java.io.Serializable;

/**
 * @author Yoav Aharoni
 */
public class MasterDetailEntry<M extends Serializable, D extends Serializable> implements Serializable {
    @WicketProperty
    private M master;

    @WicketProperty
    private D detail;

    MasterDetailEntry(M master, D detail) {
        this.master = master;
        this.detail = detail;
    }

    public M getMaster() {
        return master;
    }

    public D getDetail() {
        return detail;
    }
}
