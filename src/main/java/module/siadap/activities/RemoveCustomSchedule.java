/*
 * @(#)RemoveCustomSchedule.java
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
package module.siadap.activities;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessSchedulesEnum;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;

/**
 * 
 * @author João Antunes
 * 
 */
public class RemoveCustomSchedule extends WorkflowActivity<SiadapProcess, RemoveCustomScheduleActivityInformation> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        return ChangeCustomSchedule.shouldBeAbleToChangeSchedules(process, user);
    }

    @Override
    protected void process(RemoveCustomScheduleActivityInformation activityInformation) {
        SiadapProcessSchedulesEnum processSchedulesEnum =
                SiadapProcessSchedulesEnum.valueOf(activityInformation.getSiadapProcessSchedulesEnumToRemove());
        Siadap siadap = activityInformation.getSiadap();
        if (processSchedulesEnum == null || siadap == null) {
            throw new DomainException("error.could.not.remove.custom.schedule");
        }
        activityInformation.getProcess().changeCustomSiadapSchedule(processSchedulesEnum, null);

    }

    @Override
    public boolean isConfirmationNeeded(SiadapProcess process) {
        return true;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean isDefaultInputInterfaceUsed() {
        return true;
    }

    @Override
    protected String[] getArgumentsDescription(RemoveCustomScheduleActivityInformation activityInformation) {
        SiadapProcessSchedulesEnum processSchedulesEnum =
                SiadapProcessSchedulesEnum.valueOf(activityInformation.getSiadapProcessSchedulesEnumToRemove());
        return new String[] { processSchedulesEnum.getLocalizedName() };
    }

    @Override
    public ActivityInformation<SiadapProcess> getActivityInformation(SiadapProcess process) {
        return new RemoveCustomScheduleActivityInformation(process, this);
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process, User user) {
        return false;
    }

    @Override
    public String getUsedBundle() {
        return "resources/SiadapResources";
    }

}
