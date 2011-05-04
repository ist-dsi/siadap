package module.siadap.domain.util;

import java.io.Serializable;
import java.util.HashMap;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;

import org.joda.time.LocalDate;

public class SiadapProcessCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int counts[] = new int[SiadapProcessStateEnum.values().length];

    private final HashMap<Boolean, HashMap<String, int[]>> countsByQuotaAndCategories = new HashMap<Boolean, HashMap<String, int[]>>();

    private final LocalDate today;
    private final transient SiadapYearConfiguration configuration;
    private final transient AccountabilityType unitRelations;
    private final transient AccountabilityType evaluationRelation;
    private final transient AccountabilityType workingUnitWithQuotaRelation;
    private final transient AccountabilityType workingUnitWithoutQuotaRelation;

    public SiadapProcessCounter(final Unit unit, boolean distinguishBetweenUniverses) {
	today = new LocalDate();
	configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	unitRelations = configuration.getUnitRelations();
	evaluationRelation = configuration.getEvaluationRelation();
	workingUnitWithQuotaRelation = configuration.getWorkingRelation();
	workingUnitWithoutQuotaRelation = configuration.getWorkingRelationWithNoQuota();
	if (distinguishBetweenUniverses) {
	count(unit, distinguishBetweenUniverses);
	} else
	    count(unit);
    }

    private void count(Unit unit, boolean distinguishBetweenUniverses) {
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (accountability.isActive(today)) {
		final AccountabilityType accountabilityType = accountability.getAccountabilityType();
		if (accountabilityType == unitRelations) {
		    final Unit child = (Unit) accountability.getChild();
		    count(child, distinguishBetweenUniverses);
		} else if (accountabilityType == workingUnitWithQuotaRelation) {
		    final Person person = (Person) accountability.getChild();
		    count(person, true);

		} else if (accountabilityType == workingUnitWithoutQuotaRelation) {
		    final Person person = (Person) accountability.getChild();
		    count(person, false);

		}

	    }
	}

    }

    private void count(final Unit unit) {
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (accountability.isActive(today)) {
		final AccountabilityType accountabilityType = accountability.getAccountabilityType();
		if (accountabilityType == unitRelations) {
		    final Unit child = (Unit) accountability.getChild();
		    count(child);
		} else if (accountabilityType == workingUnitWithQuotaRelation
			|| accountabilityType == workingUnitWithoutQuotaRelation) {
		    final Person person = (Person) accountability.getChild();
		    count(person);
		}
	    }
	}
    }

    private void count(final Person person) {
	final Siadap siadap = configuration.getSiadapFor(person);
	final SiadapProcessStateEnum state = siadap == null ? SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum
		.getState(siadap);
	counts[state.ordinal()]++;
    }

    private void count(final Person person, boolean withQuota) {
	final Siadap siadap = configuration.getSiadapFor(person);
	final SiadapProcessStateEnum state = siadap == null ? SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum
		.getState(siadap);
	
	//let's fill the complicated hashmap
	HashMap<String, int[]> categoryHashMap = getCountsByQuotaAndCategories().get(Boolean.valueOf(withQuota));
	if ( categoryHashMap == null)
	//if it doesn't exist for this quotaaware/noquotaaware universe, let's create it
	{
	    categoryHashMap = new HashMap<String, int[]>();
	    getCountsByQuotaAndCategories().put(Boolean.valueOf(withQuota), categoryHashMap);
	}
	
	
	SiadapStatisticsSummaryBoardUniversesEnum universesEnum = SiadapStatisticsSummaryBoardUniversesEnum.getStatisticsUniverse(state);
	int[] categoryCounter = categoryHashMap.get(universesEnum.getCategoryString(siadap));
	if (categoryCounter == null) {
	    //do we already have a counter for this category?!, if not, we create it
	    categoryCounter = new int[universesEnum.getNrOfSubCategories()];
	    categoryHashMap.put(universesEnum.getCategoryString(siadap), categoryCounter);
	}
	categoryCounter[universesEnum.getSubCategoryIndex(state)]++;
    }

    public int[] getCounts() {
	return counts;
    }

    public boolean hasAnyPendingProcesses() {
	for (int i = 0; i < counts.length; i++) {
	    if (i != 6 && counts[i] > 0) {
		return true;
	    }
	}
	return false;
    }

    public HashMap<Boolean, HashMap<String, int[]>> getCountsByQuotaAndCategories() {
	return countsByQuotaAndCategories;
    }

}
