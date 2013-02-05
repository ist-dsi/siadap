/*
 * @(#)CorrectUserXSchedule.java
 *
 * Copyright 2011 Instituto Superior Tecnico
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
package module.siadap.domain.util.scripts;

import java.util.ArrayList;

import module.siadap.activities.ChangeCustomSchedule;
import module.siadap.activities.ChangeCustomScheduleActivityInformation;
import module.siadap.activities.ChangeCustomScheduleActivityInformation.CustomScheduleRepresentation;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.siadap.domain.wrappers.PersonSiadapWrapper;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.fenixWebFramework.security.UserView;

/**
 * 
 * @author João Antunes
 * 
 */
public class CorrectUserXSchedule extends WriteCustomTask {

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        String username = "ist24386";
        String operatorUsername = "ist154457";
        User userToAlter = User.findByUsername(username);
        out.println("Retrieving info for user: " + username);
        //let's print all of the schedules:
        PersonSiadapWrapper siadapWrapper = new PersonSiadapWrapper(userToAlter.getPerson(), 2011);
        Siadap siadap = siadapWrapper.getSiadap();
        out.println("Objective specification begin date : " + siadap.getObjectiveSpecificationBeginDate());
        out.println("Objective specification end date : " + siadap.getObjectiveSpecificationEndDate());
        out.println("Auto Evaluation begin date : " + siadap.getAutoEvaluationBeginDate());
        out.println("Auto Evaluation end date : " + siadap.getAutoEvaluationEndDate());
        out.println("Evaluation begin date : " + siadap.getEvaluationBeginDate());
        out.println("Evaluation end date : " + siadap.getEvaluationEndDate());

        //let's set the userView to my username
        final User userThatMadeTheChange = User.findByUsername(operatorUsername);
        final pt.ist.bennu.core.applicationTier.Authenticate.UserView currentUserView = UserView.getUser();
        final pt.ist.bennu.core.applicationTier.Authenticate.UserView userView = Authenticate.authenticate(userThatMadeTheChange);

        //correcting ObjectiveSpecificationBeginDate
        ChangeCustomSchedule changeCustomScheduleActivity = new ChangeCustomSchedule();

        ChangeCustomScheduleActivityInformation activityInformation =
                (ChangeCustomScheduleActivityInformation) changeCustomScheduleActivity
                        .getActivityInformation(siadap.getProcess());

        ArrayList<CustomScheduleRepresentation> customScheduleRepresentations =
                new ArrayList<ChangeCustomScheduleActivityInformation.CustomScheduleRepresentation>();
        CustomScheduleRepresentation representation =
                new CustomScheduleRepresentation(SiadapProcessSchedulesEnum.OBJECTIVES_SPECIFICATION_END_DATE, new LocalDate(),
                        "Correcção do sistema", siadap);
        customScheduleRepresentations.add(representation);
        activityInformation.setCustomScheduleRepresentation(customScheduleRepresentations);

        try {
            UserView.setUser(userView);
            changeCustomScheduleActivity.execute(activityInformation);
        } finally {
            UserView.setUser(currentUserView);
        }

    }

}
