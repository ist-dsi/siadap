/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

import org.joda.time.LocalDate;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 11 de Nov de 2011
 * 
 *         This activity allows you to have intermediate states when doing the
 *         auto evaluation so that the evaluatee can save intermediate states of
 *         his auto evaluation and iterate at will before making it public
 * 
 * 
 */
public class SubmitAutoEvaluation extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getEvaluated().getUser().equals(user) && !siadap.isAutoEvaliationDone()
		&& new AutoEvaluation().isActive(process, user) && siadap.getAutoEvaluationData2() != null;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	Siadap siadap = activityInformation.getProcess().getSiadap();

	siadap.getAutoEvaluationData2().validateData();
	activityInformation.getProcess().getSiadap().setAutoEvaluationSealedDate(new LocalDate());
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	return true;
    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    protected static void revertProcess(ActivityInformation<SiadapProcess> activityInformation) {
	//TODO also test it! SIADAP-134
    }


}
