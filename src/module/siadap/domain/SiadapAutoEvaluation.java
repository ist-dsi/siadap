package module.siadap.domain;

public class SiadapAutoEvaluation extends SiadapAutoEvaluation_Base {

    public SiadapAutoEvaluation(Siadap siadap, String objectivesJustification, String competencesJustification,
	    String otherFactorsJustification, String extremesJustification, String commentsAndProposals,
	    Integer factorOneClassification, Integer factorTwoClassification, Integer factorThreeClassification,
	    Integer factorFourClassification, Integer factorFiveClassification, Integer factorSixClassification) {

	setSiadapRootModule(SiadapRootModule.getInstance());
	getSiadapEvaluationUniverse().setSiadap(siadap);

	setObjectivesJustification(objectivesJustification);
	setCompetencesJustification(competencesJustification);
	setOtherFactorsJustification(otherFactorsJustification);
	setExtremesJustification(extremesJustification);
	setCommentsAndProposals(commentsAndProposals);

	setFactorOneClassification(factorOneClassification);
	setFactorTwoClassification(factorTwoClassification);
	setFactorThreeClassification(factorThreeClassification);
	setFactorFourClassification(factorFourClassification);
	setFactorFiveClassification(factorFiveClassification);
	setFactorSixClassification(factorSixClassification);

    }

}
