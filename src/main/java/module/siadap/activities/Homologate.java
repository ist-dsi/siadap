/*
 * @(#)Homologate.java
 *
 * Copyright 2010 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.activities;

import module.siadap.activities.ChangeGradeAnytimeActivityInformation.GradePerUniverseBean;
import module.siadap.domain.HomologationDocumentFile;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.scoring.SiadapGlobalEvaluation;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.domain.User;
import org.joda.time.LocalDate;

/**
 * 
 * 
 */
public class Homologate extends WorkflowActivity<SiadapProcess, HomologationActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        if (!process.isActive()) {
            return false;
        }
        Siadap siadap = process.getSiadap();
        return siadap.getSiadapYearConfiguration().isPersonResponsibleForHomologation(user.getPerson())
                && (siadap.getState().equals(SiadapProcessStateEnum.WAITING_FOR_REVIEW_COMMISSION) || siadap.getState().equals(
                        SiadapProcessStateEnum.WAITING_HOMOLOGATION));
    }

    @Override
    protected void process(HomologationActivityInformation activityInformation) {
        //let's check if we should process the ChangeGrade as well
        if (activityInformation.isShouldShowChangeGradeInterface()) {
            //let's check to see what is there. if we have any field filled, we should have a valid grade
            boolean shouldExecuteChangeGradeActivity = false;
            for (GradePerUniverseBean gradePerUniverseBean : activityInformation.getChangeGradeAnytimeActivityInformation()
                    .getSiadapEvaluationUniversesBeans()) {
                if (gradePerUniverseBean.getGradeToChangeTo() != null
                        || !StringUtils.isBlank(gradePerUniverseBean.getJustification())
                        || gradePerUniverseBean.isAssignExcellency()) {
                    if (!SiadapGlobalEvaluation.isValidGrade(gradePerUniverseBean.getGradeToChangeTo(),
                            gradePerUniverseBean.isAssignExcellency())) {
                        throw new SiadapException("error.ChangeGradeAnytimeAfterValidationByCCA.invalid.valid.grade.found");
                    } else if (StringUtils.isBlank(gradePerUniverseBean.getJustification())) {
                        throw new SiadapException("error.ChangeGradeAnytimeAfterValidationByCCA.no.justification.given");
                    } else {
                        shouldExecuteChangeGradeActivity = true;
                    }
                }
            }

            if (shouldExecuteChangeGradeActivity) {
                ChangeGradeAnytimeAfterValidationByCCA changeGradeAnytimeAfterValidationByCCA =
                        (ChangeGradeAnytimeAfterValidationByCCA) SiadapProcess
                                .getActivityStaticly(ChangeGradeAnytimeAfterValidationByCCA.class.getSimpleName());
                changeGradeAnytimeAfterValidationByCCA.execute(activityInformation.getChangeGradeAnytimeActivityInformation());
            }

        }
        //let's generate the document
        activityInformation.getProcess().getSiadap().setHomologationDate(new LocalDate());
        new HomologationDocumentFile(new PersonSiadapWrapper(activityInformation.getProcess().getSiadap().getEvaluated(),
                activityInformation.getProcess().getSiadap().getYear()));
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
        return false;
    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        return false;
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new HomologationActivityInformation(process, this);
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }

}
