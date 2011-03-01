package module.siadap.domain.util;

import java.io.Serializable;

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

    private final LocalDate today;
    private final transient SiadapYearConfiguration configuration;
    private final transient AccountabilityType unitRelations;
    private final transient AccountabilityType evaluationRelation;
    private final transient AccountabilityType workingUnitWithQuotaRelation;
    private final transient AccountabilityType workingUnitWithoutQuotaRelation;

    public SiadapProcessCounter(final Unit unit) {
	today = new LocalDate();
	configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	unitRelations = configuration.getUnitRelations();
	evaluationRelation = configuration.getEvaluationRelation();
	workingUnitWithQuotaRelation = configuration.getWorkingRelation();
	workingUnitWithoutQuotaRelation = configuration.getWorkingRelationWithNoQuota();

	count(unit);
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
	final SiadapProcessStateEnum state = siadap == null ?
		SiadapProcessStateEnum.NOT_CREATED : SiadapProcessStateEnum.getState(siadap);
	counts[state.ordinal()]++;
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

}
