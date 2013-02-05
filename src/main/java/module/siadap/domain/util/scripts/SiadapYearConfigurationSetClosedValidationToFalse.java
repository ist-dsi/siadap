/*
 * @(#)SiadapYearConfigurationSetClosedValidationToFalse.java
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
package module.siadap.domain.util.scripts;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * This is a simple script that will make the new slot on the {@link SiadapYearConfiguration}
 * SiadapYearConfiguration#getClosedValidation to false
 * 
 * @author João Antunes
 * 
 */
public class SiadapYearConfigurationSetClosedValidationToFalse extends WriteCustomTask {

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        int nrOfBoolsSet = 0;
        for (SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations()) {
            if (siadapYearConfiguration.getClosedValidation() == null) {
                siadapYearConfiguration.setClosedValidation(Boolean.FALSE);
                nrOfBoolsSet++;
            }
        }

        out.println("Number of bools that were set: " + nrOfBoolsSet);

    }

}
