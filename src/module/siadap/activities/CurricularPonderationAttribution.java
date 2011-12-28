/**
 * 
 */
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.WorkflowActivity;
import myorg.domain.User;

import org.apache.commons.lang.StringUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 26 de Dez de 2011
 * 
 * 
 */
public class CurricularPonderationAttribution extends WorkflowActivity<SiadapProcess, CurricularPonderationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
	Siadap siadap = process.getSiadap();
	SiadapYearConfiguration siadapYearConfiguration = siadap.getSiadapYearConfiguration();
	if (!siadap.hasAnAssociatedCurricularPonderationEval() && siadapYearConfiguration.isCurrentUserMemberOfCCA())
	    return true;
	return false;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
	return false;
    }

    @Override
    protected void process(CurricularPonderationActivityInformation activityInformation) {
	Siadap siadap = activityInformation.getSiadap();
	siadap.createCurricularPonderation(activityInformation.getSiadapUniverseToApply(),
		activityInformation.getAssignedGrade(), activityInformation.getAssignExcellentGrade(),
		activityInformation.getExcellentGradeJustification(), activityInformation.getCurricularPonderationRemarks());
    }

    //Ponderação curricular atríbuida no universo {0} com a nota {1} ({2}) - Observações "{3}"
    @Override
    protected String[] getArgumentsDescription(CurricularPonderationActivityInformation activityInformation) {
	String observations = (StringUtils.isEmpty(activityInformation.getExcellentGradeJustification())) ? activityInformation
		.getCurricularPonderationRemarks() : activityInformation.getCurricularPonderationRemarks() + " - "
		+ activityInformation.getExcellentGradeJustification();
	return new String[] {
		activityInformation.getSiadapUniverseToApply().getLocalizedName(),
		activityInformation.getAssignedGrade().toString(),
		SiadapGlobalEvaluation.getGlobalEvaluation(activityInformation.getAssignedGrade(),
			activityInformation.getAssignExcellentGrade().booleanValue()).getLocalizedName(), observations };
    }
}
