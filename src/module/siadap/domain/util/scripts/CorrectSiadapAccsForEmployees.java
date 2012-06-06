/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 18 de Abr de 2012
 *         Detect employees whose Accs are wrong and correct it
 * 
 */
public class CorrectSiadapAccsForEmployees extends WriteCustomTask {

    class PartyAccBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Party childParty;
	private Accountability accountability;

	public PartyAccBean(Party child, Accountability accountability) {
	    this.childParty = child;
	    this.accountability = accountability;
	}

	public Party getChildParty() {
	    return childParty;
	}

	public void setChildParty(Party childParty) {
	    this.childParty = childParty;
	}

	public Accountability getAccountability() {
	    return accountability;
	}

	public void setAccountability(Accountability accountability) {
	    this.accountability = accountability;
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	final LocalDate dateToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(2011);
	//all of the relevant acc types
	List<AccountabilityType> relevantAccountabilityTypes = new ArrayList<AccountabilityType>();
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(2011);
	relevantAccountabilityTypes.add(siadapYearConfiguration.getEvaluationRelation());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getUnitRelations());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getWorkingRelationWithNoQuota());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getWorkingRelation());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getSiadap2HarmonizationRelation());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getSiadap3HarmonizationRelation());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getHarmonizationUnitRelations());
	relevantAccountabilityTypes.add(siadapYearConfiguration.getHarmonizationResponsibleRelation());

	//list of person's that aren't on the SIADAP model
	List<PersonSiadapWrapper> personsWithoutFunctionalWorkingUnit = new ArrayList<PersonSiadapWrapper>();

	List<Person> personsWithDuplicatedWorkAccs = new ArrayList<Person>();

	//get all of the SIADAPs and accountabilities
	Map<Person, List<Accountability>> persons = new HashMap<Person, List<Accountability>>();
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
	    persons.put(siadap.getEvaluated(), new ArrayList<Accountability>());
	    PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(siadap.getEvaluated(), siadap.getYear());
	    UnitSiadapWrapper workingUnit = personSiadapWrapper.getWorkingUnit();
	    if (workingUnit != null && !workingUnit.isValidSIADAPUnit()) {
		personsWithoutFunctionalWorkingUnit.add(personSiadapWrapper);
	    }

	}

	Map<Person, Map<AccountabilityType, List<Accountability>>> duplicatedAccsForPerson = new HashMap<Person, Map<AccountabilityType, List<Accountability>>>();

	for (Person person : persons.keySet()) {
	    List<Accountability> list = persons.get(person);

	    Accountability workingRelationAcc = null;
	    Accountability workingRelationWithoutQuotasAcc = null;

	    for (AccountabilityType type : relevantAccountabilityTypes) {
		Collection<Accountability> accountabilities = person.getParentAccountabilities(dateToUse, null, type);
		accountabilities.addAll(person.getChildrenAccountabilities(dateToUse, null, type));

		Set<Accountability> duplicatedAccs = new HashSet<Accountability>();

		Map<Party, PartyAccBean> parentChildrenRelation = new HashMap<Party, PartyAccBean>();

		for (Accountability accountability : accountabilities) {
		    if (type.equals(siadapYearConfiguration.getWorkingRelation())) {
			workingRelationAcc = accountability;
		    }
		    if (type.equals(siadapYearConfiguration.getWorkingRelationWithNoQuota())) {
			workingRelationWithoutQuotasAcc = accountability;
		    }
		    PartyAccBean childAccBean = parentChildrenRelation.get(accountability.getParent());
		    if (childAccBean != null && childAccBean.getChildParty().equals(accountability.getChild())) {
			duplicatedAccs.add(accountability);
			duplicatedAccs.add(childAccBean.getAccountability());
		    } else if (childAccBean == null) {
			childAccBean = new PartyAccBean(accountability.getChild(), accountability);
			parentChildrenRelation.put(accountability.getParent(), childAccBean);
		    }
		}

		if (workingRelationAcc != null && workingRelationWithoutQuotasAcc != null) {
		    duplicatedAccs.add(workingRelationWithoutQuotasAcc);
		    duplicatedAccs.add(workingRelationAcc);
		    workingRelationAcc = null;
		    workingRelationWithoutQuotasAcc = null;

		    personsWithDuplicatedWorkAccs.add(person);

		}

		if (duplicatedAccs.size() >= 2) {
		    //let's add this Person and accs to the list of the duplicated ones
		    Map<AccountabilityType, List<Accountability>> map = duplicatedAccsForPerson.get(person);
		    if (map == null) {
			map = new HashMap<AccountabilityType, List<Accountability>>();
		    }
		    List<Accountability> listAccs = map.get(person);
		    if (listAccs == null) {
			listAccs = new ArrayList<Accountability>();
		    }
		    listAccs.addAll(duplicatedAccs);
		    map.put(type, listAccs);
		    duplicatedAccsForPerson.put(person, map);
		}
	    }
	}

	//now let's print all of the entries

	for (Person person : duplicatedAccsForPerson.keySet()) {
	    out.println("Person '" + person.getPresentationName() + "' has duplicated Accs");
	    Map<AccountabilityType, List<Accountability>> map = duplicatedAccsForPerson.get(person);
	    for (AccountabilityType type : map.keySet()) {
		out.println("Acc Type: " + type.getName() + " Accs: ");
		for (Accountability acc : map.get(type)) {
		    String username = (acc.getCreatorUser() != null) ? acc.getCreatorUser().getUsername() : "-";
		    out.println("Acc " + acc.getDetailsString() + " creation date: " + acc.getCreationDate()
			    + " creation user: '" + username + "'");
		}

	    }
	}

	out.println("Persons with 'invalid' SIADAP unit: " + personsWithoutFunctionalWorkingUnit.size() + " listing: ");
	for (PersonSiadapWrapper personSiadapWrapper : personsWithoutFunctionalWorkingUnit) {
	    out.println(personSiadapWrapper.getPerson().getPresentationName() + " "
		    + personSiadapWrapper.getWorkingUnit().getUnit().getPresentationName());
	}

	out.println("\n\n\nTaking care of the duplicated work accs");

	for (final Person person : personsWithDuplicatedWorkAccs) {
	    out.println("Processing " + person.getPresentationName());
	    //let's get the quota working relation and delete it
	    final Collection<Accountability> parentAccountabilities = person.getParentAccountabilities(
		    siadapYearConfiguration.getWorkingRelation(), siadapYearConfiguration.getWorkingRelationWithNoQuota());
	    HashSet<Accountability> filteredSet = Sets.newHashSet(Iterables.filter(parentAccountabilities,
		    new Predicate<Accountability>() {

			@Override
			public boolean apply(Accountability acc) {
			    if (!acc.isActive(dateToUse))
				return false;
			    for (Accountability accountability : parentAccountabilities) {
				if (accountability.equals(acc) || !accountability.isActive(dateToUse))
				    continue;
				if (acc.overlaps(accountability.getBeginDate(), accountability.getEndDate()))
				    return true;
			    }
			    return false;
			}
		    }));
	    for (Accountability acc : filteredSet) {
		if (acc.hasAccountabilityType(siadapYearConfiguration.getWorkingRelation())) {
		    //let's remove it
		    String username = (acc.getCreatorUser() != null) ? acc.getCreatorUser().getUsername() : "-";
		    out.println("deleting Acc " + acc.getDetailsString() + " creation date: " + acc.getCreationDate()
			    + " creation user: '" + username + "'");
		    acc.delete();
		}
	    }
	}

    }
}
