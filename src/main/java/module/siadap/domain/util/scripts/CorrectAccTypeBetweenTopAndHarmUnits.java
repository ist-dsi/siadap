/*
 * @(#)CorrectAccTypeBetweenTopAndHarmUnits.java
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

import java.util.ArrayList;
import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.PartyType;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * Simple script done to correct the relation between the TopUnit and
 * the Harmonization Units (which should be an Harmonization Unit
 * relation, but isn't ATM)
 * 
 * @author João Antunes
 * 
 */
public class CorrectAccTypeBetweenTopAndHarmUnits extends WriteCustomTask {

    public final static int YEAR_TO_USE = 2011;
    private SiadapYearConfiguration siadapYearConfiguration;
    private final List<Accountability> accToReplace = new ArrayList<Accountability>();

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
        //let's get all the data that we need
        siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_USE);
        AccountabilityType harmonizationUnitRelations = siadapYearConfiguration.getHarmonizationUnitRelations();
        AccountabilityType unitRelations = siadapYearConfiguration.getUnitRelations();
        PartyType harmonizationType = PartyType.readBy(UnitSiadapWrapper.SIADAP_HARMONIZATION_UNIT_TYPE);

        //let's get all the children Accs which are of the U.H. type and that are active for the given year
        for (Accountability acc : siadapYearConfiguration.getSiadapStructureTopUnit().getChildrenAccountabilities(
                harmonizationUnitRelations, unitRelations)) {
            if (acc.isActive(SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(YEAR_TO_USE))) {
                if (acc.getChild() instanceof Unit) {
                    //let's check to see if it is of the correct type
                    Unit childUnit = (Unit) acc.getChild();
                    if (childUnit.getPartyTypes().contains(harmonizationType)
                            && acc.getAccountabilityType().equals(unitRelations)) {
                        //let's change this one
                        acc.setAccountabilityType(harmonizationUnitRelations);
                        out.println("Changed acc of '"
                                + siadapYearConfiguration.getSiadapStructureTopUnit().getPartyName().getContent() + "' to '"
                                + childUnit.getPresentationName() + "' from acc type Unit Relations to HarmonizationUnit");
                    }
                }

            }
        }

    }

}
