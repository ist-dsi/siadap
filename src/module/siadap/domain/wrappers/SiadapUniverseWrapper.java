/**
 * 
 */
package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
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

    public static final Comparator<SiadapUniverseWrapper> COMPARATOR_BY_UNIVERSE = new Comparator<SiadapUniverseWrapper>() {

	@Override
	public int compare(SiadapUniverseWrapper o1, SiadapUniverseWrapper o2) {
	    return o1.getUniverseDescription().compareTo(o2.getUniverseDescription()) == 0 ? -1 : o1.getUniverseDescription()
		    .compareTo(o2.getUniverseDescription());
	}
    };

    /**
     * Default serial version ID
     */
    private static final long serialVersionUID = 1L;
    private final Collection<PersonSiadapWrapper> siadapUniverse;

    private final List<SiadapSuggestionBean> siadapUniverseForSuggestions;
    private final Map<ExceedingQuotaSuggestionType, List<SiadapSuggestionBean>> siadapExceedingQuotaSuggestionsByTypeForUniverse;
    private final String universeDescription;

    private final int numberRelevantPeopleInUniverse;
    private final int numberTotalRelevantForQuotaPeopleInUniverse;
    private final BigDecimal excellencyQuota;
    private final int currentEvaluationExcellents;
    private final int currentHarmonizedExcellents;
    private final int currentValidatedExcellents;
    private final BigDecimal relevantQuota;
    private final int currentEvaluationRelevants;
    private final int currentHarmonizedRelevants;
    private final int currentValidatedRelevants;

    private final int globalNumberRelevantPeopleInUniverse;
    private final int globalNumberTotalRelevantForQuotaPeopleInUniverse;
    private final BigDecimal globalExcellencyQuota;
    private final int globalCurrentEvaluationExcellents;
    private final int globalCurrentHarmonizedExcellents;
    private final int globalCurrentValidatedExcellents;
    private final BigDecimal globalRelevantQuota;
    private final int globalCurrentEvaluationRelevants;
    private final int globalCurrentHarmonizedRelevants;
    private final int globalCurrentValidatedRelevants;

    public static final String SIADAP2_WITH_QUOTAS = "siadap2WithQuotas";
    public static final String SIADAP3_WITH_QUOTAS = "siadap3WithQuotas";
    public static final String SIADAP2_WITHOUT_QUOTAS = "siadap2WithoutQuotas";
    public static final String SIADAP3_WITHOUT_QUOTAS = "siadap3WithoutQuotas";

    public static final String SIADAP2_WITH_QUOTAS_EXCELLENT_SUGGESTION = "siadap2WithQuotasExcellentSugg";
    public static final String SIADAP2_WITH_QUOTAS_HIGH_SUGGESTION = "siadap2WithQuotasHighSugg";

    public static final String SIADAP3_WITH_QUOTAS_EXCELLENT_SUGGESTION = "siadap3WithQuotasExcellentSugg";
    public static final String SIADAP3_WITH_QUOTAS_HIGH_SUGGESTION = "siadap3WithQuotasHighSugg";

    public static final String SIADAP2_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION = "siadap2WithoutQuotasExcellentSugg";
    public static final String SIADAP2_WITHOUT_QUOTAS_HIGH_SUGGESTION = "siadap2WithoutQuotasHighSugg";

    public static final String SIADAP3_WITHOUT_QUOTAS_EXCELLENT_SUGGESTION = "siadap3WithoutQuotasExcellentSugg";
    public static final String SIADAP3_WITHOUT_QUOTAS_HIGH_SUGGESTION = "siadap3WithoutQuotasHighSugg";
    private final SiadapUniverse siadapUniverseEnum;

    private final static int HARMONIZATION_SCALE = 0;
    private final static RoundingMode HARMONIZATION_ROUND_MODE = RoundingMode.DOWN;

    private final static int VALIDATION_SCALE = 2;
    private final static RoundingMode VALIDATION_ROUND_MODE = RoundingMode.HALF_EVEN;

    public static enum UniverseDisplayMode {
	VALIDATION {
	    @Override
	    public int getScale() {
		return VALIDATION_SCALE;
	    }

	    @Override
	    public RoundingMode getQuotaRoundingMode() {
		return VALIDATION_ROUND_MODE;
	    }
	},
	HARMONIZATION {
	    @Override
	    public int getScale() {
		return HARMONIZATION_SCALE;
	    }

	    @Override
	    public RoundingMode getQuotaRoundingMode() {
		return HARMONIZATION_ROUND_MODE;
	    }
	};

	public abstract int getScale();

	public abstract RoundingMode getQuotaRoundingMode();
    }

    /**
     * Constructor used to make a wrapper suitable for harmonization purposes;
     * 
     * @param siadapUniverseOfPeople
     * @param universeDescription
     * @param universeToConsider
     * @param excellencyQuotaPercentagePoints
     * @param relevantQuotaPercentagePoints
     */
    public SiadapUniverseWrapper(Collection<PersonSiadapWrapper> siadapUniverseOfPeople, String universeDescription,
	    SiadapUniverse universeToConsider, int excellencyQuotaPercentagePoints, int relevantQuotaPercentagePoints,
	    UniverseDisplayMode universeDisplayMode,
	    Map<ExceedingQuotaSuggestionType, List<ExceedingQuotaProposal>> suggestionsByType, Integer numberTotalRelevant) {
	this.universeDescription = universeDescription;
	this.siadapUniverse = siadapUniverseOfPeople;

	this.siadapUniverseEnum = universeToConsider;

	int relevantPeople = 0;
	//we are assuming that if we are showing the validation, we already have only the relevant people
	if (universeDisplayMode == UniverseDisplayMode.HARMONIZATION) {
	    for (PersonSiadapWrapper personSiadapWrapper : siadapUniverseOfPeople) {
		if (!personSiadapWrapper.isWithSkippedEval(siadapUniverseEnum)) {
		    relevantPeople++;
		}
	    }
	    this.numberTotalRelevantForQuotaPeopleInUniverse = relevantPeople;

	} else {
	    relevantPeople = siadapUniverseOfPeople.size();
	    if (numberTotalRelevant != null)
		this.numberTotalRelevantForQuotaPeopleInUniverse = numberTotalRelevant.intValue();
	    else
		this.numberTotalRelevantForQuotaPeopleInUniverse = 0;
	}

	this.numberRelevantPeopleInUniverse = relevantPeople;

	if (universeDisplayMode != null) {
	    switch (universeDisplayMode) {
	    case VALIDATION:
		this.excellencyQuota = calculateQuota(this.getNumberTotalRelevantForQuotaPeopleInUniverse(),
			excellencyQuotaPercentagePoints).setScale(universeDisplayMode.getScale(),
			universeDisplayMode.getQuotaRoundingMode());
		this.relevantQuota = calculateQuota(this.getNumberTotalRelevantForQuotaPeopleInUniverse(),
			relevantQuotaPercentagePoints).setScale(universeDisplayMode.getScale(),
			universeDisplayMode.getQuotaRoundingMode());
		break;
	    case HARMONIZATION:
	    default:
		this.excellencyQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), excellencyQuotaPercentagePoints)
			.setScale(universeDisplayMode.getScale(), universeDisplayMode.getQuotaRoundingMode());
		this.relevantQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), relevantQuotaPercentagePoints)
			.setScale(universeDisplayMode.getScale(), universeDisplayMode.getQuotaRoundingMode());
		break;
	    }

	} else {

	    this.excellencyQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), excellencyQuotaPercentagePoints);
	    this.relevantQuota = calculateQuota(this.getNumberRelevantPeopleInUniverse(), relevantQuotaPercentagePoints);
	}

	this.currentEvaluationExcellents = getCurrentExcellents(this.siadapUniverse, universeToConsider, false, false);
	this.currentEvaluationRelevants = getCurrentRelevants(this.siadapUniverse, universeToConsider, false, false);

	this.currentHarmonizedExcellents = getCurrentExcellents(this.siadapUniverse, universeToConsider, true, false);
	this.currentHarmonizedRelevants = getCurrentRelevants(this.siadapUniverse, universeToConsider, true, false);

	if (universeDisplayMode == null) {
	    universeDisplayMode = UniverseDisplayMode.HARMONIZATION;
	}
	switch (universeDisplayMode) {
	case VALIDATION:
	    this.currentValidatedExcellents = getCurrentExcellents(this.siadapUniverse, universeToConsider, false, true);
	    this.currentValidatedRelevants = getCurrentRelevants(this.siadapUniverse, universeToConsider, false, true);

	    //let's use one person from the list to get the top structure unit
	    PersonSiadapWrapper personSiadapWrapper = null;
	    try {
		personSiadapWrapper = siadapUniverseOfPeople.iterator().next();
	    } catch (java.util.NoSuchElementException ex) {
	    }
	    if (personSiadapWrapper != null) {
		SiadapYearConfiguration configuration = personSiadapWrapper.getConfiguration();
		Map<Integer, Collection<PersonSiadapWrapper>> validationPersonSiadapWrappers = new UnitSiadapWrapper(
			configuration.getSiadapStructureTopUnit(), configuration.getYear()).getValidationPersonSiadapWrappers(
			universeToConsider, personSiadapWrapper.isQuotaAware());

		Collection<PersonSiadapWrapper> globalUniverse = validationPersonSiadapWrappers.values().iterator().next();
		this.globalNumberTotalRelevantForQuotaPeopleInUniverse = validationPersonSiadapWrappers.keySet().iterator()
			.next();

		this.globalCurrentEvaluationExcellents = getCurrentExcellents(globalUniverse, siadapUniverseEnum, false, false);
		this.globalCurrentEvaluationRelevants = getCurrentRelevants(globalUniverse, siadapUniverseEnum, false, false);

		this.globalCurrentHarmonizedExcellents = getCurrentExcellents(globalUniverse, siadapUniverseEnum, true, false);
		this.globalCurrentHarmonizedRelevants = getCurrentRelevants(globalUniverse, siadapUniverseEnum, true, false);

		this.globalCurrentValidatedExcellents = getCurrentExcellents(globalUniverse, siadapUniverseEnum, false, true);
		this.globalCurrentValidatedRelevants = getCurrentRelevants(globalUniverse, siadapUniverseEnum, false, true);

		this.globalNumberRelevantPeopleInUniverse = globalUniverse.size();

		this.globalExcellencyQuota = calculateQuota(globalNumberTotalRelevantForQuotaPeopleInUniverse,
			excellencyQuotaPercentagePoints).setScale(universeDisplayMode.getScale(),
			universeDisplayMode.getQuotaRoundingMode());
		this.globalRelevantQuota = calculateQuota(globalNumberTotalRelevantForQuotaPeopleInUniverse,
			relevantQuotaPercentagePoints).setScale(universeDisplayMode.getScale(),
			universeDisplayMode.getQuotaRoundingMode());
	    } else {
		this.globalCurrentEvaluationExcellents = 0;
		this.globalCurrentEvaluationRelevants = 0;
		this.globalCurrentHarmonizedExcellents = 0;
		this.globalCurrentHarmonizedRelevants = 0;
		this.globalCurrentValidatedExcellents = 0;
		this.globalCurrentValidatedRelevants = 0;
		this.globalExcellencyQuota = null;
		this.globalNumberRelevantPeopleInUniverse = 0;
		this.globalNumberTotalRelevantForQuotaPeopleInUniverse = 0;
		this.globalRelevantQuota = null;
	    }

	    break;
	case HARMONIZATION:
	default:
	    this.currentValidatedExcellents = 0;
	    this.currentValidatedRelevants = 0;

	    this.globalCurrentEvaluationExcellents = 0;
	    this.globalCurrentEvaluationRelevants = 0;
	    this.globalCurrentHarmonizedExcellents = 0;
	    this.globalCurrentHarmonizedRelevants = 0;
	    this.globalCurrentValidatedExcellents = 0;
	    this.globalCurrentValidatedRelevants = 0;
	    this.globalExcellencyQuota = null;
	    this.globalNumberRelevantPeopleInUniverse = 0;
	    this.globalRelevantQuota = null;
	    this.globalNumberTotalRelevantForQuotaPeopleInUniverse = 0;
	}

	this.siadapUniverseForSuggestions = null;

	this.siadapExceedingQuotaSuggestionsByTypeForUniverse = new HashMap<ExceedingQuotaSuggestionType, List<SiadapSuggestionBean>>();

	if (suggestionsByType != null) {
	    for (ExceedingQuotaSuggestionType suggestionType : suggestionsByType.keySet()) {
		List<SiadapSuggestionBean> suggestionTypeList = new ArrayList<SiadapSuggestionBean>();
		this.siadapExceedingQuotaSuggestionsByTypeForUniverse.put(suggestionType, suggestionTypeList);
		for (ExceedingQuotaProposal quotaProposal : suggestionsByType.get(suggestionType)) {
		    suggestionTypeList.add(new SiadapSuggestionBean(quotaProposal));
		}
		Collections.sort(suggestionTypeList, SiadapSuggestionBean.COMPARATOR_BY_PRIORITY_NUMBER);
	    }
	}

    }

    public SiadapUniverseWrapper(List<ExceedingQuotaProposal> quotaProposals, String universeDescription,
	    SiadapUniverse universeToConsider, UnitSiadapWrapper unitBeingSuggestionsMadeFor, boolean quotasUniverse) {

	//a null simple universe and everything else intended for the harmonization purposes
	this.siadapUniverse = null;
	this.excellencyQuota = BigDecimal.ZERO;
	this.relevantQuota = BigDecimal.ZERO;
	this.currentEvaluationExcellents = 0;
	this.currentEvaluationRelevants = 0;
	this.currentHarmonizedExcellents = 0;
	this.currentHarmonizedRelevants = 0;
	this.currentValidatedExcellents = 0;
	this.currentValidatedRelevants = 0;

	this.numberTotalRelevantForQuotaPeopleInUniverse = 0;
	this.globalNumberTotalRelevantForQuotaPeopleInUniverse = 0;

	this.siadapUniverseForSuggestions = new ArrayList<SiadapSuggestionBean>();
	HashSet<SiadapSuggestionBean> siadapUniverseForSuggestionsSet = new HashSet<SiadapSuggestionBean>();
	for (ExceedingQuotaProposal proposal : quotaProposals) {
	    siadapUniverseForSuggestions.add(new SiadapSuggestionBean(proposal));
	}

	Collections.sort(this.getSiadapUniverseForSuggestions(), SiadapSuggestionBean.COMPARATOR_BY_PRIORITY_NUMBER);

	this.siadapUniverseEnum = universeToConsider;

	this.numberRelevantPeopleInUniverse = getSiadapUniverseForSuggestions().size();

	this.universeDescription = universeDescription;

	//no need for the global fields here
	this.globalCurrentEvaluationExcellents = 0;
	this.globalCurrentEvaluationRelevants = 0;
	this.globalCurrentHarmonizedExcellents = 0;
	this.globalCurrentHarmonizedRelevants = 0;
	this.globalCurrentValidatedExcellents = 0;
	this.globalCurrentValidatedRelevants = 0;
	this.globalExcellencyQuota = null;
	this.globalNumberRelevantPeopleInUniverse = 0;
	this.globalRelevantQuota = null;

	this.siadapExceedingQuotaSuggestionsByTypeForUniverse = null;

    }

    /**
     * 
     * @return true if it is above quotas, rounding the quota to the integer
     *         value by truncating, false otherwise
     */
    public boolean isAboveQuotasHarmonization() {
	return (currentHarmonizedExcellents > excellencyQuota.intValue() || currentHarmonizedRelevants > relevantQuota.intValue());
    }

    public String getCurrentHarmonizedRelevantsHTMLClass() {
	return "current-harmonized-relevants-" + universeDescription;
    }

    public String getCurrentHarmonizedExcellentsHTMLClass() {
	return "current-harmonized-excellents-" + universeDescription;
    }

    public String getCurrentValidatedRelevantsHTMLClass() {
	return "current-validated-relevants-" + universeDescription;
    }

    public String getCurrentValidatedExcellentsHTMLClass() {
	return "current-validated-excellents-" + universeDescription;
    }

    /**
     * 
     * @return true if it is above quotas, rounding the quota to the integer
     *         value by truncating, false otherwise
     */
    public boolean isSiadapUniverseWithQuotasAboveQuotaHarmonization() {
	return (currentHarmonizedRelevants > relevantQuota.intValue() || currentHarmonizedExcellents > excellencyQuota.intValue());
    }

    private int getCurrentExcellents(Collection<PersonSiadapWrapper> siadapPersonWrappersUniverse, SiadapUniverse siadapUniverse,
	    boolean considerHarmonizedOnly, boolean considerValidatedOnly) {
	Predicate predicateToUse = new SiadapGradePredicate(considerHarmonizedOnly, siadapUniverse,
		SiadapGlobalEvaluation.EXCELLENCY, considerValidatedOnly);
	return getNrEvaluationsBasedOnPredicate(siadapPersonWrappersUniverse, predicateToUse);

    }

    private int getCurrentRelevants(Collection<PersonSiadapWrapper> siadapPersonWrappersUniverse, SiadapUniverse siadapUniverse,
	    boolean considerHarmonizedOnly, boolean considerValidatedOnly) {
	Predicate predicateToUse = new SiadapGradePredicate(considerHarmonizedOnly, siadapUniverse, SiadapGlobalEvaluation.HIGH,
		considerValidatedOnly);
	return getNrEvaluationsBasedOnPredicate(siadapPersonWrappersUniverse, predicateToUse);

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
    private BigDecimal calculateQuota(int totalPeople, Integer quota) {
	BigDecimal result = new BigDecimal(totalPeople).multiply(new BigDecimal(quota)).divide(new BigDecimal(100));

	int value = result.intValue();
	return value > 0 ? result : BigDecimal.ONE; //if the quota is 0 the the quota shifts to 1

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
	private final boolean considerValidatedOnly;
	private final SiadapUniverse siadapUniverseToConsider;
	private final SiadapGlobalEvaluation siadapGlobalEvaluation;

	SiadapGradePredicate(boolean considerHarmonizedOnly, SiadapUniverse siadapUniverseToConsider,
		SiadapGlobalEvaluation siadapGlobalEvaluation, boolean considerValidatedOnly) {
	    this.considerHarmonizedOnly = considerHarmonizedOnly;
	    this.considerValidatedOnly = considerValidatedOnly;
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
				    .getHarmonizationAssessment() && checkForPositivelyHarmonizedExcellent(personSiadapWrapper)))
		    && (!considerValidatedOnly || (siadap.hasCompleteValidationAssessment(siadapUniverseToConsider) && hasPositiveValidationAssessment(personSiadapWrapper)))) {
		return true;
	    }
	    return false;
	}

	private boolean hasPositiveValidationAssessment(PersonSiadapWrapper personSiadapWrapper) {
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    SiadapEvaluationUniverse evaluationUniverse = siadap
		    .getSiadapEvaluationUniverseForSiadapUniverse(siadapUniverseToConsider);
	    if (evaluationUniverse == null)
		return false;
	    if (siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.EXCELLENCY)
		    || siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.HIGH)) {
		if (siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.EXCELLENCY)) {
		    //we are just looking for an excellent, so we will only have it if we have a positive excellency assessment
		    return evaluationUniverse.getCcaClassificationExcellencyAward() != null
			    && evaluationUniverse.getCcaClassificationExcellencyAward();
		}
		return (evaluationUniverse.getCcaClassificationExcellencyAward() != null && evaluationUniverse
			.getCcaClassificationExcellencyAward())
			|| (evaluationUniverse.getCcaAssessment() != null && evaluationUniverse.getCcaAssessment());
		//we won't check the grade as we already checked it before coming here
	    } else {
		return evaluationUniverse.getCcaAssessment() != null && evaluationUniverse.getCcaAssessment();
	    }
	}

	private boolean hasGradeWithinRange(PersonSiadapWrapper personSiadapWrapper) {
	    Siadap siadap = personSiadapWrapper.getSiadap();
	    SiadapGlobalEvaluation globalEvaluationEnum = siadap.getSiadapGlobalEvaluationEnum(siadapUniverseToConsider,
		    considerValidatedOnly);
	    if (globalEvaluationEnum.equals(SiadapGlobalEvaluation.EXCELLENCY)) {
		return (siadapGlobalEvaluation.equals(SiadapGlobalEvaluation.HIGH) || siadapGlobalEvaluation
			.equals(SiadapGlobalEvaluation.EXCELLENCY));
	    }
	    return siadap.hasGivenSiadapGlobalEvaluation(siadapGlobalEvaluation, siadapUniverseToConsider, considerValidatedOnly);

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

    public Collection<PersonSiadapWrapper> getSiadapUniverse() {
	return siadapUniverse;
    }

    public String getUniverseDescription() {
	return universeDescription;
    }

    public BigDecimal getExcellencyQuota() {
	return excellencyQuota;
    }

    public int getCurrentEvaluationExcellents() {
	return currentEvaluationExcellents;
    }

    public int getCurrentHarmonizedExcellents() {
	return currentHarmonizedExcellents;
    }

    public BigDecimal getRelevantQuota() {
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

    public int getCurrentValidatedExcellents() {
	return currentValidatedExcellents;
    }

    public int getCurrentValidatedRelevants() {
	return currentValidatedRelevants;
    }

    public int getGlobalNumberRelevantPeopleInUniverse() {
	return globalNumberRelevantPeopleInUniverse;
    }

    public BigDecimal getGlobalExcellencyQuota() {
	return globalExcellencyQuota;
    }

    public int getGlobalCurrentEvaluationExcellents() {
	return globalCurrentEvaluationExcellents;
    }

    public int getGlobalCurrentHarmonizedExcellents() {
	return globalCurrentHarmonizedExcellents;
    }

    public int getGlobalCurrentValidatedExcellents() {
	return globalCurrentValidatedExcellents;
    }

    public BigDecimal getGlobalRelevantQuota() {
	return globalRelevantQuota;
    }

    public int getGlobalCurrentEvaluationRelevants() {
	return globalCurrentEvaluationRelevants;
    }

    public int getGlobalCurrentHarmonizedRelevants() {
	return globalCurrentHarmonizedRelevants;
    }

    public int getGlobalCurrentValidatedRelevants() {
	return globalCurrentValidatedRelevants;
    }

    public Map<ExceedingQuotaSuggestionType, List<SiadapSuggestionBean>> getSiadapExceedingQuotaSuggestionsByTypeForUniverse() {
	return siadapExceedingQuotaSuggestionsByTypeForUniverse;
    }

    public int getNumberTotalRelevantForQuotaPeopleInUniverse() {
	return numberTotalRelevantForQuotaPeopleInUniverse;
    }

    public int getGlobalNumberTotalRelevantForQuotaPeopleInUniverse() {
	return globalNumberTotalRelevantForQuotaPeopleInUniverse;
    }
}
