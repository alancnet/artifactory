package org.artifactory.aql.api;

import org.artifactory.aql.api.internal.AqlBase;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public interface AqlApiElement<T extends AqlBase> {
    List<AqlApiElement<T>> get();

    public boolean isEmpty();
}
