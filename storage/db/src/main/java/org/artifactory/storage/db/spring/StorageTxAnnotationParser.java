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

package org.artifactory.storage.db.spring;

import org.artifactory.storage.fs.StorageTx;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;

/**
 * @author Yossi Shaul
 */
public class StorageTxAnnotationParser implements TransactionAnnotationParser, Serializable {

    @Override
    public TransactionAttribute parseTransactionAnnotation(AnnotatedElement ae) {
        StorageTx ann = AnnotationUtils.getAnnotation(ae, StorageTx.class);
        if (ann != null) {
            RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
            return rbta;
        } else {
            return null;
        }
    }

}
