/**
 * 
 */
package module.siadap.activities;

import java.math.BigDecimal;
import java.util.List;

import module.siadap.activities.ChangeGradeAnytimeActivityInformation.GradePerUniverseBean;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.i18n.BundleUtil;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Abr de 2012
 * 
 *         Activity that can occur at anytime after the validation and even
 *         after the homologation occurs.
 * 
 *         Also, this activity is connected with the {@link Homologate} one, as
 *         when the Homologate is done directly on the view of the process, the
 *         user is invited to change the grade, if it doesn't then it's the same
 *         as this activity was never executed
 * 
 * 
 * 
 */
public class ChangeGradeAnytimeAfterValidationByCCA extends
        WorkflowActivity<SiadapProcess, ChangeGradeAnytimeActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        if (!process.isActive()) {
            return false;
        }
        Siadap siadap = process.getSiadap();
        if (siadap.getState().ordinal() < SiadapProcessStateEnum.WAITING_SUBMITTAL_BY_EVALUATOR_AFTER_VALIDATION.ordinal()) {
            return false;
        }
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(siadap.getYear());
        return siadapYearConfiguration.isPersonMemberOfCCA(user.getPerson())
                || siadapYearConfiguration.isPersonResponsibleForHomologation(user.getPerson());

    }

    @Override
    protected void process(ChangeGradeAnytimeActivityInformation activityInformation) {
        List<GradePerUniverseBean> siadapEvaluationUniversesBeans = activityInformation.getSiadapEvaluationUniversesBeans();
        boolean foundAValidGrade = false;
        for (GradePerUniverseBean gradePerUniverseBean : siadapEvaluationUniversesBeans) {
            BigDecimal gradeToChangeTo = gradePerUniverseBean.getGradeToChangeTo();
            boolean assignExcellency = gradePerUniverseBean.isAssignExcellency();
            if (gradeToChangeTo != null) {
                if (!SiadapGlobalEvaluation.isValidGrade(gradePerUniverseBean.getGradeToChangeTo(), assignExcellency)) {
                    throw new SiadapException("error.ChangeGradeAnytimeAfterValidationByCCA.invalid.valid.grade.found");
                } else if (!StringUtils.isBlank(gradePerUniverseBean.getJustification())) {
                    foundAValidGrade = true;
                } else {
                    throw new SiadapException("error.ChangeGradeAnytimeAfterValidationByCCA.no.justification.given");
                }

            }
        }
        if (!foundAValidGrade) {
            throw new SiadapException("error.ChangeGradeAnytimeAfterValidationByCCA.no.valid.grade.found");
        }
        //so let's get the valid grades to assign and let's assign them
        for (GradePerUniverseBean gradePerUniverseBean : activityInformation.getSiadapEvaluationUniversesBeans()) {
            if (SiadapGlobalEvaluation.isValidGrade(gradePerUniverseBean.getGradeToChangeTo(),
                    gradePerUniverseBean.isAssignExcellency())) {
                gradePerUniverseBean.getSiadapEvaluationUniverse().setCcaAfterValidationGrade(
                        gradePerUniverseBean.getGradeToChangeTo());
                gradePerUniverseBean.getSiadapEvaluationUniverse().setCcaAfterValidationExcellencyAward(
                        gradePerUniverseBean.isAssignExcellency());
            }
        }
    }

    @Override
    protected String[] getArgumentsDescription(ChangeGradeAnytimeActivityInformation activityInformation) {
        String stringToReturn = null;
        for (GradePerUniverseBean gradePerUniverseBean : activityInformation.getSiadapEvaluationUniversesBeans()) {
            if (SiadapGlobalEvaluation.isValidGrade(gradePerUniverseBean.getGradeToChangeTo(),
                    gradePerUniverseBean.isAssignExcellency())) {
                if (stringToReturn != null) {
                    stringToReturn += ", ";
                } else {
                    stringToReturn = "";
                }
                String currentExcellencyAwardString =
                        BundleUtil.getString("resources/MyorgResources",
                                String.valueOf(gradePerUniverseBean.getSiadapEvaluationUniverse().getCurrentExcellencyAward()));
                String newExcellencyAwardString =
                        BundleUtil.getString("resources/MyorgResources",
                                String.valueOf(gradePerUniverseBean.isAssignExcellency()));
                stringToReturn +=
                        BundleUtil.getString(getUsedBundle(),
                                "label.description.module.siadap.activities.ChangeGradeAnytimeAfterValidationByCCA.gradeChange",
                                gradePerUniverseBean.getSiadapEvaluationUniverse().getSiadapUniverse().getLocalizedName(),
                                gradePerUniverseBean.getSiadapEvaluationUniverse().getCurrentGrade().toString(),
                                currentExcellencyAwardString, gradePerUniverseBean.getGradeToChangeTo().toString(),
                                newExcellencyAwardString, gradePerUniverseBean.getJustification());

            }
        }
        return new String[] { stringToReturn };
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
        return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new ChangeGradeAnytimeActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
        return Siadap.SIADAP_BUNDLE_STRING;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
        return false;
    }

}
