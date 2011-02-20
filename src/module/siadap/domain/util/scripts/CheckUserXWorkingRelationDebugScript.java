package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.PartyWrapper.FilterAccountabilities;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.domain.User;
import myorg.domain.scheduler.WriteCustomTask;

import org.apache.commons.collections.Predicate;

/**
 * Debug script to be used in the production/fenix-tests version
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class CheckUserXWorkingRelationDebugScript extends WriteCustomTask {
    User user;

    @Override
    protected void doService() {
	//get the user to check
	user = User.findByUsername("ist21526");

	//this is the code that i want to debug 
	Person evaluator = null;

	Collection<Person> possibleCustomEvaluator = getParentPersons(getConfiguration().getEvaluationRelation());

	if (!possibleCustomEvaluator.isEmpty()) {
	    out.println("possibleCustomEvaluator relation is not null! * WARNING * it should be");
	    evaluator = possibleCustomEvaluator.iterator().next();
	    Collection<Accountability> accountabilities = user.getPerson().getParentAccountabilities(
		    getConfiguration().getEvaluationRelation());
	    out.println("Displaying all of the SIADAP accountabilities found");
	    for (Accountability accountability : accountabilities) {
		out.println("acc details: " + accountability.getDetailsString());
	    }

	} else {
	    out.println("possibleCustomEvaluator relation is null as expected");
	    if (getWorkingUnit() != null) {
		Collection<Unit> workingPlaces = getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
			.getWorkingRelationWithNoQuota());
		Unit workingUnit = workingPlaces.iterator().next();
		Collection<Person> childPersons = workingUnit.getChildPersons(getConfiguration().getEvaluationRelation());
		if (!childPersons.isEmpty()) {
		    evaluator = childPersons.iterator().next();
		}
	    }
	}

	if (evaluator != null)
	    out.println("Final evaluator name is: " + evaluator.getName());
	else
	    out.println("final evaluator is null!");

    }

    private SiadapYearConfiguration getConfiguration() {
	return SiadapYearConfiguration.getSiadapYearConfiguration(new Integer(2011));
    }

    protected List<Person> getParentPersons(AccountabilityType... types) {
	return getParentPersons(new FilterAccountabilities(2011, true), types);
    }

    private List<Person> getParentPersons(Predicate predicate, AccountabilityType... types) {
	List<Person> person = new ArrayList<Person>();
	for (Accountability accountability : user.getPerson().getParentAccountabilities(types)) {
	    if (predicate == null || predicate.evaluate(accountability)) {
		Party parent = accountability.getParent();
		if (parent.isPerson()) {
		    person.add(((Person) parent));
		}
	    }
	}
	return person;
    }

    public UnitSiadapWrapper getWorkingUnit() {
	Collection<Unit> parentUnits = getParentUnits(getConfiguration().getWorkingRelation(), getConfiguration()
		.getWorkingRelationWithNoQuota());
	return parentUnits.isEmpty() ? null : new UnitSiadapWrapper(parentUnits.iterator().next(), getConfiguration().getYear());
    }

    protected List<Unit> getParentUnits(AccountabilityType... types) {
	return getParentUnits(new FilterAccountabilities(2011, true), types);
    }

    private List<Unit> getParentUnits(Predicate predicate, AccountabilityType... types) {
	List<Unit> units = new ArrayList<Unit>();
	for (Accountability accountability : user.getPerson().getParentAccountabilities(types)) {
	    if (predicate == null || predicate.evaluate(accountability)) {
		Party parent = accountability.getParent();
		if (parent.isUnit()) {
		    units.add(((Unit) parent));
		}
	    }
	}
	return units;
    }

}
