/**
 * 
 */
package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;

import org.apache.commons.collections.Predicate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 28 de Dez de 2011
 * 
 *         Class used to represent (interface wise) the SIADAP universe, which
 *         is a combination of two variables: the SIADAP2 or SIADAP3 and if the
 *         person counts for the main organization quota or not - thus making up
 *         for four possible universes. This class is used both for the
 *         harmonization purposes and for the exceeding quota suggestion
 *         purposes.
 * 
 *         TODO probably also for validation purposes in the near future (and
 *         everywhere else where it makes sense to separate everything with
 *         universes)
 * 
 */
public class SiadapUniverseWrapper implements Serializable {

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;
    private final Set<PersonSiadapWrapper> siadapUniverse;

    private final List<SiadapSuggestionBean> siadapUniverseForSuggestions;
    private final String universeDescription;

    private final int numberRelevantPeopleInUniverse;
    private final int excellencyQuota;
    private final int currentEvaluationExcellents;
    private final int currentHarmonizedExcellents;
    private final int relevantQuota;
    private final int currentEvaluationRelevants;
    private final int currentHarmonizedRelevants;

    public static final String SIADAP2_WITH_QUOTAS = "siadap2WithQuotas";
    public static final String SIADAP3_WITH_QUOTAS = "siadap3WithQuotas";
    public static final String SIADAP2_WITHOUT_QUOTAS = "siadap2WithoutQuotas";
    public static final String SIADAP3_WITHOUT_QUOTAS = "siadap3WithoutQuotas";

    private final SiadapUniverse siadapUniverseEnum;

    /**
     * Constructor used to make a wrapper suitable for harmonization purposes;
     * 
     * @param siadapUniverseOfPeople
     * @param universeDescription
     * @param universeToConsider
     * @param excellencyQuotaPercentagePoints
     * @param relevantQuotaPercentagePoints
     */
    public SiadapUniverseWrapper(Set<PersonSiadapWrapper> siadapUniverseOfPeople, String universeDescription,
	    SiadapUniverse universeToConsider, int excellencyQuotaPercentagePoints, int relevantQuotaPercentagePoints) {
	this.siadapUniverse = siadapUniverseOfPeople;
	this.universeDescription = universeDescription;

	this.siadapUniverseEnum = universeToConsider;

	int relevantPeople = 0;
	for (PersonSiadapWrapper personSiadapWrapper : siadapUniverseOfPeople) {
	    if (!personSiadapWrapper.isWithSkippedEval(siadapUniverseEnum)) {
		relevantPeople++;
	    }
	}

	this.numberRelevantPeopleInUniverse = relevantPeople;
	this.excellencyQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), excellencyQuotaPercentagePoints);
	this.relevantQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), relevantQuotaPercentagePoints);

	this.currentEvaluationExcellents = getCurrentExcellents(universeToConsider, false);
	this.currentEvaluationRelevants = getCurrentRelevants(universeToConsider, false);

	this.currentHarmonizedExcellents = getCurrentExcellents(universeToConsider, true);
	this.currentHarmonizedRelevants = getCurrentRelevants(universeToConsider, true);

	this.siadapUniverseForSuggestions = null;

    }

    public SiadapUniverseWrapper(Set<PersonSiadapWrapper> siadapUniverseOfPeople, String universeDescription,
	    SiadapUniverse universeToConsider, UnitSiadapWrapper unitBeingSuggestionsMadeFor, boolean quotasUniverse) {

	//a null simple universe and everything else intended for the harmonization purposes
	this.siadapUniverse = null;
	this.excellencyQuota = 0;
	this.relevantQuota = 0;
	this.currentEvaluationExcellents = 0;
	this.currentEvaluationRelevants = 0;
	this.currentHarmonizedExcellents = 0;
	this.currentHarmonizedRelevants = 0;

	HashSet<SiadapSuggestionBean> siadapUniverseForSuggestionsSet = new HashSet<SiadapSuggestionBean>();
	for (PersonSiadapWrapper personSiadapWrapper : siadapUniverseOfPeople) {
	    siadapUniverseForSuggestionsSet.add(new SiadapSuggestionBean(personSiadapWrapper, unitBeingSuggestionsMadeFor,
		    quotasUniverse, universeToConsider));
	}
	this.siadapUniverseForSuggestions = new ArrayList<SiadapSuggestionBean>(siadapUniverseForSuggestionsSet);

	Collections.sort(this.getSiadapUniverseForSuggestions(), SiadapSuggestionBean.COMPARATOR_BY_PRIORITY_NUMBER);

	this.siadapUniverseEnum = universeToConsider;

	this.numberRelevantPeopleInUniverse = getSiadapUniverseForSuggestions().size();

	this.universeDescription = universeDescription;

    }

    public boolean isAboveQuotas() {
	return (currentHarmonizedExcellents > excellencyQuota || currentHarmonizedRelevants > relevantQuota);
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
	Predicate predicateToUse = new SiadapGradePredicate(considerHarmonizedOnly, siadapUniverse,
		SiadapGlobalEvaluation.EXCELLENCY);
	return getNrEvaluationsBasedOnPredicate(this.siadapUniverse, predicateToUse);

    }

    private int getCurrentRelevants(SiadapUniverse siadapUniverse, boolean considerHarmonizedOnly) {
	Predicate predicateToUse = new SiadapGradePredicate(considerHarmonizedOnly, siadapUniverse, SiadapGlobalEvaluation.HIGH);
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

    /**
     * 
     * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Jan de 2012
     * 
     *         Predicate used to test against a certain grade for a given
     *         universe and also consider the different stages of the grade
     * 
     */
    private class SiadapGradePredicate implements Predicate {
	private final boolean considerHarmonizedOnly;
	private final SiadapUniverse siadapUniverseToConsider;
	private final SiadapGlobalEvaluation siadapGlobalEvaluation;

	SiadapGradePredicate(boolean considerHarmonizedOnly, SiadapUniverse siadapUniverseToConsider,
		SiadapGlobalEvaluation siadapGlobalEvaluation) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	    this.siadapUniverseToConsider = siadapUniverseToConsider;
	    this.siadapGlobalEvaluation = siadapGlobalEvaluation;
	}

	@Override
	public boolean evaluate(Object arg0) {
	    PersonSiadapWrapper personSiadapWrapper = (PersonSiadapWrapper) arg0;
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    if (siadap != null
		    && hasGradeWithinRange(personSiadapWrapper)
		    && (!considerHarmonizedOnly || (siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider) != null
			    && siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider)
				    .getHarmonizationAssessment() != null
			    && siadap.getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider)
				    .getHarmonizationAssessment() && checkForPositivelyHarmonizedExcellent(personSiadapWrapper)))) {
		return true;
	    }
	    return false;
	}

	private boolean hasGradeWithinRange(PersonSiadapWrapper personSiadapWrapper) {
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    SiadapGlobalEvaluation globalEvaluationEnum = siadap.getSiadapGlobalEvaluationEnum(siadapUniverseToConsider);
	    if (globalEvaluationEnum.equals(SiadapGlobalEvaluation.EXCELLENCY)) {
		return (siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.HIGH) || siadapGlobalEvaluation
			.equals(SiadapGlobalEvaluation.EXCELLENCY));
	    }
	    return siadap.hasGivenSiadapGlobalEvaluation(siadapGlobalEvaluation, siadapUniverseToConsider);

	}

	/**
	 * 
	 * @param personSiadapWrapper
	 *            the person to consider
	 * @return true if we are not checking for an excellent i.e.
	 *         siadapGlobalEvaluation is not an excellent. If it is, check
	 *         if we have a positive assessment for the excellent
	 */
	private boolean checkForPositivelyHarmonizedExcellent(PersonSiadapWrapper personSiadapWrapper) {
	    if (!siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.EXCELLENCY))
		return true;
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    SiadapEvaluationUniverse siadapEvaluationUniverse = siadap
		    .getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider);
	    return (siadapEvaluationUniverse.getHarmonizationAssessmentForExcellencyAward() != null && siadapEvaluationUniverse
		    .getHarmonizationAssessmentForExcellencyAward());
	}

    }

    public Set<PersonSiadapWrapper> getSiadapUniverse() {
	return siadapUniverse;
    }

    public String getUniverseDescription() {
	return universeDescription;
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

    public SiadapUniverse getSiadapUniverseEnum() {
	return siadapUniverseEnum;
    }

    public String getUniverseTitleQuotaSuggestionKey() {
	return "label.harmonization.QuotaSuggestionInterface." + universeDescription;
    }

    public List<SiadapSuggestionBean> getSiadapUniverseForSuggestions() {
	return siadapUniverseForSuggestions;
    }

    public int getNumberRelevantPeopleInUniverse() {
	return numberRelevantPeopleInUniverse;
    }
}
