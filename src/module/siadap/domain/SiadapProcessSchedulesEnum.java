/*
 * @(#)SiadapProcessSchedulesEnum.java
 *
 * Copyright 2011 Instituto Superior Tecnico
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

import java.util.MissingResourceException;

import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

/**
 * 
 * @author João Neves
 * @author João Antunes
 * 
 */
public enum SiadapProcessSchedulesEnum implements IPresentableEnum {
    OBJECTIVES_SPECIFICATION_BEGIN_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (!siadap.getObjectiveSpecificationEndDate().isAfter(newDate))
		triggerInvalidDateException();

	}

    },
    OBJECTIVES_SPECIFICATION_END_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (!siadap.getObjectiveSpecificationBeginDate().isBefore(newDate))
		triggerInvalidDateException();
	}
    },

    AUTOEVALUATION_BEGIN_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (!siadap.getAutoEvaluationEndDate().isAfter(newDate))
		triggerInvalidDateException();
	}
    },
    AUTOEVALUATION_END_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (!siadap.getAutoEvaluationBeginDate().isBefore(newDate))
		triggerInvalidDateException();
	}
    },

    EVALUATION_BEGIN_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (!siadap.getEvaluationEndDate().isAfter(newDate))
		triggerInvalidDateException();
	}

    },
    EVALUATION_END_DATE {
	@Override
	public void validateDate(LocalDate newDate, Siadap siadap) throws DomainException {
	    if (siadap.getEvaluationBeginDate().isBefore(newDate))
		triggerInvalidDateException();
	}
    };

    private SiadapProcessSchedulesEnum() {
    }

    private static void triggerInvalidDateException() {
	throw new DomainException("invalid.date.begin.must.be.before.end.date");

    }

    /**
     * Method that validates a new date to be set, it should be invoked before
     * adding the new date Basicly it checks that if it's an end date is not
     * before the begin date and the other way around
     * 
     * @param newDate
     *            the new date to make sure is a valid one.
     * @param siadap
     *            the siadap instance for which we should check if the given
     *            date is valid
     * @throws DomainException
     *             if the date isn't valid
     */
    public abstract void validateDate(LocalDate newDate, Siadap siadap) throws DomainException;

    @Override
    public String getLocalizedName() {
	try {
	    return BundleUtil.getStringFromResourceBundle("resources/SiadapResources",
		    SiadapProcessSchedulesEnum.class.getSimpleName() + "." + name());
	} catch (MissingResourceException ex) {
	    return name();
	}

    }

}
