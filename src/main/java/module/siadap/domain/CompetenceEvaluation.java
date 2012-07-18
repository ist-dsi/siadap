/*
 * @(#)CompetenceEvaluation.java
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

import module.siadap.domain.scoring.IScoring;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class CompetenceEvaluation extends CompetenceEvaluation_Base {

    public CompetenceEvaluation(Siadap siadap, Competence competence) {
	super();
	SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	setSiadapEvaluationUniverse(defaultSiadapEvaluationUniverse);
	setCompetence(competence);
    }

    @Override
    public IScoring getItemAutoEvaluation() {
	return getAutoEvaluation();
    }

    @Override
    public IScoring getItemEvaluation() {
	return getEvaluation();
    }

    public void delete()
    {
	if (getAutoEvaluation() != null || getEvaluation() != null) {
	    // TODO ist154457: improve the error, assert what kind of exception
	    // should be thrown here
	    throw new Error("Error while trying to delete a competence that has evaluation data assigned");
	}
	removeSiadapEvaluationUniverse();
	removeSiadapRootModule();
	removeCompetence();
	deleteDomainObject();
    }

	@Override
	public boolean isValid() {
	// a competence is always valid by definition
	return true;
	}

}
