package module.siadap.domain;

import java.util.MissingResourceException;

import myorg.domain.exceptions.DomainException;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.rendererExtensions.util.IPresentableEnum;

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
