/*
 * @(#)SendToReviewCommission.java
 *
 * Copyright 2012 Instituto Superior Tecnico
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
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;

import org.joda.time.LocalDate;

import pt.ist.bennu.core.domain.User;

/**
 * 
 * Marks a process as pending by the Review Commission
 * 
 * @author João Roxo Neves
 * 
 */
public class SendToReviewCommission extends WorkflowActivity<SiadapProcess, ActivityInformation<SiadapProcess>> {

    @Override
    public boolean isActive(SiadapProcess process, User user) {
        if (!process.isActive()) {
            return false;
        }
        int year = process.getSiadap().getYear();
        if (SiadapRootModule.getInstance().getSiadapCCAGroup().isMember(user)
                && (process.getSiadap().getState().equals(SiadapProcessStateEnum.VALIDATION_ACKNOWLEDGED) || process.getSiadap()
                        .getState().equals(SiadapProcessStateEnum.WAITING_HOMOLOGATION))) {
            return true;
        }
        return false;
    }

    @Override
    public String getUsedBundle() {
        return Siadap.SIADAP_BUNDLE_STRING;
    }

    @Override
    public boolean isUserAwarenessNeeded(SiadapProcess process) {
        return false;
    }

    @Override
    protected void process(ActivityInformation<SiadapProcess> activityInformation) {
        activityInformation.getProcess().getSiadap().setAssignedToReviewCommissionDate(new LocalDate());
    }
}
