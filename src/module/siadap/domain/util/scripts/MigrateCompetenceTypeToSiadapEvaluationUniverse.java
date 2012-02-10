/**
 * 
 */
package module.siadap.domain.util.scripts;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationUniverse;
import module.siadap.domain.SiadapRootModule;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 6 de Fev de 2012
 *
 * 
 */
public class MigrateCompetenceTypeToSiadapEvaluationUniverse extends WriteCustomTask {

    /* (non-Javadoc)
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	int relationsDone = 0;
	int relationsNotDone = 0;
	//let's get all of the SIADAPs
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
	    SiadapEvaluationUniverse defaultSiadapEvaluationUniverse = siadap.getDefaultSiadapEvaluationUniverse();
	    //let's get its CompetenceType, if it has one, let's set the relation
	    if (defaultSiadapEvaluationUniverse != null && siadap.getCompetenceType() != null) {
		defaultSiadapEvaluationUniverse.setCompetenceSlashCareerType(siadap.getCompetenceType());
		relationsDone++;
	    }
 else {
		relationsNotDone++;
	    }
	}

	out.println("Migrated " + relationsDone + " competences and didn't migrate " + relationsNotDone);

    }

}
