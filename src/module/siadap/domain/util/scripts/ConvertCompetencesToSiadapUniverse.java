/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import myorg.domain.scheduler.ReadCustomTask;
import myorg.domain.scheduler.TransactionalThread;

import org.apache.commons.lang.StringUtils;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Dez de 2011
 *
 * 
 */
public class ConvertCompetencesToSiadapUniverse extends ReadCustomTask {

    int nrSiadapsMigrated = 0;
    private final static int NR_MIGRATIONS_PER_THREAD = 100;

    /* (non-Javadoc)
     * @see jvstm.TransactionalCommand#doIt()
     */
    @Override
    public void doIt() {
	int nrSiadapsToMigrate = 0;
	int nrSiadapsUnableToMigrate = 0;
	int nrSiadaps = 0;
	int nrSiadapTwos = 0;
	int nrSiadapThrees = 0;

	//let's get all the processes for all of the years 

	List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();

	nrSiadaps = siadaps.size();

	Map<Siadap, SiadapUniverse> auxMap = new HashMap<Siadap, SiadapUniverse>();
	List<Siadap> siadapsUnableToMigrate = new ArrayList<Siadap>();
	for (Siadap siadap : siadaps) {
	    if (getMainSiadapUniverse(siadap) == null) {
		//let's assert the SIADAP based on the competences
		if (siadap.getCompetenceType() == null)
		{
		    nrSiadapsUnableToMigrate++;
		    siadapsUnableToMigrate.add(siadap);
		}
		else if (StringUtils.containsIgnoreCase("Dirigente Intermédio", siadap.getCompetenceType().getName())) {
		    nrSiadapThrees++;
		    nrSiadapsToMigrate++;
		    auxMap.put(siadap, SiadapUniverse.SIADAP2);
		} 
		else {
		    nrSiadapTwos++;
		    nrSiadapsToMigrate++;
		    auxMap.put(siadap, SiadapUniverse.SIADAP3);
		}
	    }
	}

	out.println("Read everything, results:\n Total nr SIADAPS: " + nrSiadaps + " nr SIADAPS to migrate: "
		+ nrSiadapsToMigrate + " nr SIADAP which we couldn't migrate: " + nrSiadapsUnableToMigrate);
	out.println(" Nr SIADAP2: " + nrSiadapTwos + " Nr SIADAP3:" + nrSiadapThrees);

	out.println("Listing all the SIADAPs which we were unable to migrate: ");
	for (Siadap siadap : siadapsUnableToMigrate) {
	    out.println("SIADAP Nr: " + siadap.getProcess().getProcessNumber() + " evaluated with knowledge of objectives: "
		    + siadap.isEvaluatedWithKnowledgeOfObjectives() + " with skiped eval: " + siadap.isWithSkippedEvaluation());
	}
	
	out.println("Going to migrate them");
	
	Map<Siadap, SiadapUniverse> auxSplittedMap = new HashMap<Siadap, SiadapUniverse>();
	Iterator<Siadap> siadapI = auxMap.keySet().iterator();
	for (int i = 0; i < auxMap.size(); i++)
	{
	    if (i >= NR_MIGRATIONS_PER_THREAD) {
		convertSiadaps(auxSplittedMap);
		auxSplittedMap = new HashMap<Siadap, SiadapUniverse>();
	    }
	    Siadap siadapBeingUsed = siadapI.next();
	    auxSplittedMap.put(siadapBeingUsed, auxMap.get(siadapBeingUsed));
	}

	//let's take care of the rest of them
	if (nrSiadapsMigrated < auxMap.size()) {
	    convertSiadaps(auxSplittedMap);
	}
	printStatus();
    }

    public SiadapUniverse getMainSiadapUniverse(Siadap siadap) {
	//seen that we only have evaluationItems on one of the SiadapEvaluationUniverses
	for (SiadapEvaluationItem evaluationItem : siadap.getSiadapEvaluationItems()) {
	    return evaluationItem.getSiadapEvaluationUniverse().getSiadapUniverse();
	}
	return null;
    }

    public static void setMainSiadapUniverse(Siadap siadap, SiadapUniverse siadapUniverse) {
	//seen that we only have evaluationItems on one of the SiadapEvaluationUniverses
	for (SiadapEvaluationItem evaluationItem : siadap.getSiadapEvaluationItems()) {
	    evaluationItem.getSiadapEvaluationUniverse().setSiadapUniverse(siadapUniverse);
	}
    }

    private void convertSiadaps(Map<Siadap, SiadapUniverse> siadapsToConvert) {
	MigrateSiadapProcesses migrateSiadapProcesses = new MigrateSiadapProcesses(siadapsToConvert);
	migrateSiadapProcesses.start();
	try {
	    migrateSiadapProcesses.join();
	    nrSiadapsMigrated += siadapsToConvert.size();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    e.printStackTrace(out);
	    out.println("Error! printing status");
	    printStatus();
	    throw new Error(e);
	}
    }

    private void printStatus() {
	out.println("Nr of SIADAPS migrated: " + nrSiadapsMigrated);
    }

    class MigrateSiadapProcesses extends TransactionalThread {

	final Map<Siadap, SiadapUniverse> siadapsToMigrate;

	public MigrateSiadapProcesses(Map<Siadap, SiadapUniverse> siadapsToMigrate) {
	    this.siadapsToMigrate = siadapsToMigrate;
	}

	@Override
	public void transactionalRun() {
	    for (Siadap siadap : siadapsToMigrate.keySet()) {
		SiadapUniverse siadapUniverse = siadapsToMigrate.get(siadap);
		setMainSiadapUniverse(siadap, siadapUniverse);
	    }

	}
    }

}
