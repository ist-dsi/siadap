/*
 * @(#)ObjectiveEvaluationIndicator.java
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

import java.math.BigDecimal;

import pt.ist.bennu.core.domain.exceptions.DomainException;

/**
 * 
 * @author João Antunes
 * @author Paulo Abrantes
 * 
 */
public class ObjectiveEvaluationIndicator extends ObjectiveEvaluationIndicator_Base {

	public ObjectiveEvaluationIndicator(ObjectiveEvaluation objective, String measurementIndicator, String superationCriteria,
			BigDecimal ponderationFactor) {
		super();
		if (!(ponderationFactor.compareTo(BigDecimal.ZERO) >= 0 && ponderationFactor.compareTo(BigDecimal.ONE) <= 0)) {
			throw new DomainException("error.ponderation.has.to.be.between.0.and.1",
					DomainException.getResourceFor("resources/SiadapResources"));
		}

		setObjectiveEvaluation(objective);
		setMeasurementIndicator(measurementIndicator);
		setSuperationCriteria(superationCriteria);
		setPonderationFactor(ponderationFactor);

		setSiadapRootModule(SiadapRootModule.getInstance());
	}

	public BigDecimal getAutoEvaluationPoints() {
		return getAutoEvaluation().getPoints().multiply(getPonderationFactor());
	}

	/**
	 * Disconnects itself from the world, and removes itself from the DB
	 */
	public void delete() {
		removeObjectiveEvaluation();
		removeSiadapRootModule();
		deleteDomainObject();
	}

	public BigDecimal getEvaluationPoints() {
		if (getEvaluation() == null) {
			return null;
		}
		return getEvaluation().getPoints().multiply(getPonderationFactor());
	}

}
