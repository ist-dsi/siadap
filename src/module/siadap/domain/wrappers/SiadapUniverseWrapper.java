/**
 * 
 */
package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapUniverse;

import org.apache.commons.collections.Predicate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 28 de Dez de 2011
 * 
 * 
 * 
 */
public class SiadapUniverseWrapper implements Serializable {

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;
    private final Set<PersonSiadapWrapper> siadapUniverse;
    private final String universeDescription;

    private final int numberPeopleInUniverse;
    private final int excellencyQuota;
    private final int currentEvaluationExcellents;
    private final int currentHarmonizedExcellents;
    private final int relevantQuota;
    private final int currentEvaluationRelevants;
    private final int currentHarmonizedRelevants;

    public SiadapUniverseWrapper(Set<PersonSiadapWrapper> siadapUniverseOfPeople, String universeDescription,
	    SiadapUniverse universeToConsider, int excellencyQuotaPercentagePoints, int relevantQuotaPercentagePoints) {
	this.siadapUniverse = siadapUniverseOfPeople;
	this.universeDescription = universeDescription;

	this.numberPeopleInUniverse = siadapUniverseOfPeople.size();
	this.excellencyQuota = calculateQuota(this.numberPeopleInUniverse, excellencyQuotaPercentagePoints);
	this.relevantQuota = calculateQuota(this.numberPeopleInUniverse, relevantQuotaPercentagePoints);

	this.currentEvaluationExcellents = getCurrentExcellents(universeToConsider, false);
	this.currentEvaluationRelevants = getCurrentRelevants(universeToConsider, false);
	
	this.currentHarmonizedExcellents = getCurrentExcellents(universeToConsider, true);
	this.currentHarmonizedRelevants = getCurrentExcellents(universeToConsider, true);
    }

    public String getCurrentHarmonizedRelevantsHTMLClass() {
	return "current-harmonized-relevants-" + universeDescription;
    }

    public String getCurrentHarmonizedExcellentsHTMLClass() {
	return "current-harmonized-excellents-" + universeDescription;
    }

    public boolean isSiadapUniverseWithQuotasAboveQuota() {
	return (currentHarmonizedRelevants > relevantQuota || currentHarmonizedExcellents > excellencyQuota);
    }
    private int getCurrentExcellents(SiadapUniverse siadapUniverse, boolean considerHarmonizedOnly) {
	Predicate predicateToUse = null;
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    predicateToUse = new Siadap2ExcellentPredicate(considerHarmonizedOnly);
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    predicateToUse = new Siadap3ExcellentPredicate(considerHarmonizedOnly);
	}
	return getNrEvaluationsBasedOnPredicate(this.siadapUniverse, predicateToUse);

    }

    private int getCurrentRelevants(SiadapUniverse siadapUniverse, boolean considerHarmonizedOnly) {
	Predicate predicateToUse = null;
	if (siadapUniverse.equals(SiadapUniverse.SIADAP2)) {
	    predicateToUse = new Siadap2RelevantPredicate(considerHarmonizedOnly);
	} else if (siadapUniverse.equals(SiadapUniverse.SIADAP3)) {
	    predicateToUse = new Siadap3RelevantPredicate(considerHarmonizedOnly);
	}
	return getNrEvaluationsBasedOnPredicate(this.siadapUniverse, predicateToUse);

    }

    private int getNrEvaluationsBasedOnPredicate(Collection<PersonSiadapWrapper> personsToEvaluatePredicateOn, Predicate predicate) {
	int counter = 0;
	for (PersonSiadapWrapper siadapWrapper : personsToEvaluatePredicateOn) {
	    if (predicate.evaluate(siadapWrapper))
		counter++;
	}
	return counter;
    }
    
    /**
     * @param totalPeople
     *            the number of people to calculate the quota on
     * @param quota
     *            the percentage points of quota
     * @return how many people it represents, it is never 0 due to SIADAPs rules
     */
    private int calculateQuota(int totalPeople, Integer quota) {
	BigDecimal result = new BigDecimal(totalPeople).multiply(new BigDecimal(quota)).divide(new BigDecimal(100));

	int value = result.intValue();
	return value > 0 ? value : 1; //if the quota is 0 the the quota shifts to 1

    }


    private class Siadap2RelevantPredicate implements Predicate {
	private final boolean considerHarmonizedOnly;

	Siadap2RelevantPredicate(boolean considerHarmonizedOnly) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    if (siadap != null
		    && siadap.hasRelevantSiadap2Evaluation()
		    && (!considerHarmonizedOnly || (siadap.getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2) != null && siadap
			    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2).getHarmonizationAssessment()))) {
		return true;
	    }
	    return false;
	}

    }

    private class Siadap3RelevantPredicate implements Predicate {
	private final boolean considerHarmonizedOnly;

	Siadap3RelevantPredicate(boolean considerHarmonizedOnly) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	}

   	@Override
   	public boolean evaluate(Object arg0) {
   	    PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
   	    Siadap siadap = personSiadapWrapper.getSiadap();
	    if (siadap != null
		    && siadap.hasRelevantSiadap3Evaluation()
		    && (!considerHarmonizedOnly || (siadap.getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2) != null && siadap
			    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2).getHarmonizationAssessment()))) {
   		return true;
   	    }
   	    return false;
   	}

       }

    private class Siadap3ExcellentPredicate implements Predicate {

	private final boolean considerHarmonizedOnly;

	Siadap3ExcellentPredicate(boolean considerHarmonizedOnly) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    if (siadap != null
		    && siadap.hasExcellentSiadap3Evaluation()
		    && (!considerHarmonizedOnly || (siadap.getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2) != null && siadap
			    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2).getHarmonizationAssessment()))) {
		return true;
	    }
	    return false;
	}

    }

    private class Siadap2ExcellentPredicate implements Predicate {

	private final boolean considerHarmonizedOnly;

	Siadap2ExcellentPredicate(boolean considerHarmonizedOnly) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    if (siadap != null
		    && siadap.hasExcellentSiadap2Evaluation()
		    && (!considerHarmonizedOnly || (siadap.getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2) != null && siadap
			    .getSiadapEvaluationUniverseForSiadapUniverse(SiadapUniverse.SIADAP2).getHarmonizationAssessment()))) {
		return true;
	    }
	    return false;
	}

    }

    public Set<PersonSiadapWrapper> getSiadapUniverse() {
	return siadapUniverse;
    }

    public String getUniverseDescription() {
	return universeDescription;
    }

    public int getNumberPeopleInUniverse() {
	return numberPeopleInUniverse;
    }

    public int getExcellencyQuota() {
	return excellencyQuota;
    }

    public int getCurrentEvaluationExcellents() {
	return currentEvaluationExcellents;
    }

    public int getCurrentHarmonizedExcellents() {
	return currentHarmonizedExcellents;
    }

    public int getRelevantQuota() {
	return relevantQuota;
    }

    public int getCurrentEvaluationRelevants() {
	return currentEvaluationRelevants;
    }

    public int getCurrentHarmonizedRelevants() {
	return currentHarmonizedRelevants;
    }
}
