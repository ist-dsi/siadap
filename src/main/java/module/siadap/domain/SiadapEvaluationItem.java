/*
 * @(#)SiadapEvaluationItem.java
 *
 * Copyright 2010 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.domain;

import java.util.Comparator;

import module.siadap.domain.scoring.IScoring;

import org.joda.time.DateTime;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public abstract class SiadapEvaluationItem extends SiadapEvaluationItem_Base {

    public static Comparator<SiadapEvaluationItem> COMPARATOR_BY_DATE = new Comparator<SiadapEvaluationItem>() {

        @Override
        public int compare(SiadapEvaluationItem o1, SiadapEvaluationItem o2) {
            return o1.getWhenCreated().compareTo(o2.getWhenCreated());
        }

    };

    public SiadapEvaluationItem() {
        super();
        setWhenCreated(new DateTime());
        setSiadapRootModule(SiadapRootModule.getInstance());
    }

    /**
     * @return true if the SiadapEvaluationItem is valid, false otherwise
     */
    public abstract boolean isValid();

    public abstract IScoring getItemEvaluation();

    public abstract IScoring getItemAutoEvaluation();

    protected void delete() {
        removeSiadapRootModule();
        deleteDomainObject();

    }
}
