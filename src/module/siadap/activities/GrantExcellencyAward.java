package module.siadap.activities;

import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

public class GrantExcellencyAward extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	return siadap.getSiadapYearConfiguration().isPersonMemberOfCCA(user.getPerson())
		&& Boolean.TRUE.equals(siadap.getValidated())
		&& !Boolean.TRUE.equals(siadap.getEvaluationData().getExcellencyAward());
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
	Siadap siadap = process.getSiadap();
	int year = siadap.getSiadapYearConfiguration().getYear();
	Person person = siadap.getEvaluated();
	PersonSiadapWrapper wrapper = new PersonSiadapWrapper(person, year);
	UnitSiadapWrapper workingUnit = new UnitSiadapWrapper(wrapper.getWorkingUnit().getHarmonizationUnit(), year);
	int usedQuota = workingUnit.getCurrentUsedExcellencyGradeQuota();
	int totalQuota = workingUnit.getExcellencyGradeQuota();
	return usedQuota + 1 > totalQuota;

    }

    @Override
    public String getUsedBundle() {
	return "resources/SiadapResources";
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
	activityInformation.getProcess().getSiadap().getEvaluationData().setExcellencyAward(Boolean.TRUE);
    }

}
