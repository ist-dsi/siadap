/*
 * @(#)SIADAPSpecialHarmonizationUnitAccCorrection.java
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

import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

/**
 * 
 *         Task made to correct the lack of harmonization unit accountability
 *         between the special harmonization unit and the top unit
 * 
 * @author João Antunes
 * 
 */
public class SIADAPSpecialHarmonizationUnitAccCorrection extends WriteCustomTask {

    /* (non-Javadoc)
     * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
     */
    private static final int YEAR_TO_START = 2011;
    /**
     * The year until which the accountability should be extended, or till
     * infinity if year =0;
     */
    //    private static final int YEAR_TO_END = 0;
    @Override
    protected void doService() {
	//let's get the year to begin data
	SiadapYearConfiguration siadapYearToStartConf = SiadapYearConfiguration.getSiadapYearConfiguration(YEAR_TO_START);
	Unit specialHarmUnit = siadapYearToStartConf.getSiadapSpecialHarmonizationUnit();
	//WARNING we are just blindly assigning an accountability without checking for an already existing one, so we should only run this script once
	if (true)
	    throw new DomainException("please.make.sure.you.want.to.run.this.again");

	Unit siadapTopUnit = siadapYearToStartConf.getSiadapStructureTopUnit();

	specialHarmUnit.addParent(siadapTopUnit, siadapYearToStartConf.getHarmonizationUnitRelations(), new LocalDate(
		YEAR_TO_START, 12, 20), null);

    }

}
