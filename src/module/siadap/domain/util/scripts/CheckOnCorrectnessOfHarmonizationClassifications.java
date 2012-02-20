/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.List;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 18 de Fev de 2012
 * 
 *         Task that checks that all classifications have been given for
 *         terminated harmonizations for a given year
 * 
 */
public class CheckOnCorrectnessOfHarmonizationClassifications extends WriteCustomTask {

    private final static int YEAR_TO_CHECK = 2011;

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
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
