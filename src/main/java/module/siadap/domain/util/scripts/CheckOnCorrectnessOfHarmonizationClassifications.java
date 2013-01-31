/*
 * @(#)CheckOnCorrectnessOfHarmonizationClassifications.java
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

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * Task that checks that all classifications have been given for
 * terminated harmonizations for a given year
 * 
 * @author João Antunes
 * 
 */
public class CheckOnCorrectnessOfHarmonizationClassifications extends WriteCustomTask {

	private final static int YEAR_TO_CHECK = 2011;

	/* (non-Javadoc)
	 * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
	 */
	@Override
	protected void doService() {
		List<Siadap> siadapsWithoutGrade = new ArrayList<Siadap>();
		int nrOfSiadaps = 0;
		int nrOfTerminatedHarmonizationSiadaps = 0;
		int nrOfHarmonizedWithoutGrade = 0;
		for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
			if (siadap.getYear() == YEAR_TO_CHECK) {
				nrOfSiadaps++;

				if (siadap.getDefaultSiadapEvaluationUniverse().getHarmonizationDate() != null) {
					nrOfTerminatedHarmonizationSiadaps++;

					if (siadap.getDefaultSiadapEvaluationUniverse().getHarmonizationClassification() == null) {
						nrOfHarmonizedWithoutGrade++;
						siadapsWithoutGrade.add(siadap);
					}
				}

			}
		}

		out.println("Number of SIADAPS for the given year: " + nrOfSiadaps + " nr of terminated harm. SIADAPs: "
				+ nrOfTerminatedHarmonizationSiadaps + " of which " + nrOfHarmonizedWithoutGrade + " are without grade");

		out.println("Siadaps wihout grade: ");
		for (Siadap siadap : siadapsWithoutGrade) {
			out.println(siadap.getProcess().getProcessNumber() + " with Skipped eval?: "
					+ siadap.getDefaultSiadapEvaluationUniverse().isWithSkippedEvaluation());
		}
		out.println("-- END of LIST --");

	}

}
