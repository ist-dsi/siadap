/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.exceptions.SiadapException;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;
import myorg.util.BundleUtil;

import org.joda.time.LocalDate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 11 de Nov de 2011
 * 
 *         This activity allows you to have intermediate states when doing the
 *         evaluation so that the evaluator can save intermediate states of his
 *         evaluation and iterate at will before making it available
 * 
 * 
 */
public class SubmitEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluator().getPerson().getUser().equals(user) && !siadap.isDefaultEvaluationDone()
		&& new Evaluation().isActive(process, user) && siadap.getEvaluationData2() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	//validate the existing evaluation data
	Siadap siadap = activityInformation.getProcess().getSiadap();
	for (SiadapEvaluationItem item : siadap.getCurrentEvaluationItems()) {
	    if (item.getItemEvaluation() == null || item.getItemEvaluation().getPoints() == null) {
		throw new ActivityException(BundleUtil.getStringFromResourceBundle(getUsedBundle(),
			"error.siadapEvaluation.mustFillAllItems"), getLocalizedName());
	    }
	}

	//let's make some extra checks on the data inserted
	siadap.getEvaluationData2().validateData();

	activityInformation.getProcess().getSiadap().setEvaluationSealedDate(new LocalDate());

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	return !siadap.isAutoEvaliationDone() && !siadap.isAutoEvaluationIntervalFinished();
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    protected static void revertProcess(ActivityInformation<SiadapProcess> activityInformation) {
	Siadap siadap = activityInformation.getProcess().getSiadap();
	if (siadap.isHarmonizationOfDefaultUniverseDone())
	    throw new SiadapException("error.cannot.revert.harmonized.to.no.self.evaluation");
	siadap.setEvaluationSealedDate(null);
    }


}
