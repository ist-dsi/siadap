package module.siadap.domain.wrappers;

import java.io.Serializable;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.applicationTier.Authenticate.UserView;

public class PersonSiadapWrapper implements Serializable {

    private Person person;
    private int year;
    private SiadapYearConfiguration configuration;

    public PersonSiadapWrapper(Person person, int year) {
	this.person = person;
	this.year = year;
	this.configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
    }

    public Person getPerson() {
	return person;
    }

    public void setPerson(Person person) {
	this.person = person;
    }

    public int getYear() {
	return year;
    }

    public void setYear(int year) {
	this.year = year;
    }

    public SiadapYearConfiguration getConfiguration() {
	return configuration;
    }

    public void setConfiguration(SiadapYearConfiguration configuration) {
	this.configuration = configuration;
    }

    public boolean isEvaluationStarted() {
	return getSiadap() != null;
    }

    public Siadap getSiadap() {
	return this.configuration.getSiadapFor(getPerson(), getYear());
    }

    public Person getEvaluator() {
	return this.configuration.getEvaluatorFor(getPerson());
    }

    public boolean isAccessibleToCurrentUser() {
	Siadap siadap = getSiadap();
	if (siadap == null) {
	    return false;
	}
	return siadap.getProcess().isAccessibleToCurrentUser();
    }

    public boolean isCurrentUserAbleToEvaluate() {
	return this.configuration.getEvaluatorFor(getPerson()) == UserView.getCurrentUser().getPerson();
    }

    public boolean isCurrentUserAbleToCreateProcess() {
	return getSiadap() == null && isCurrentUserAbleToEvaluate();
    }
}