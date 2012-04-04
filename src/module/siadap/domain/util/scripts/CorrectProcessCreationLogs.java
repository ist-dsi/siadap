/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.HashMap;
import java.util.Map;

import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.workflow.domain.LabelLog;
import module.workflow.domain.WorkflowLog;
import myorg.domain.scheduler.WriteCustomTask;

import org.apache.commons.lang.StringUtils;

import pt.utl.ist.fenix.tools.util.Strings;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 4 de Abr de 2012
 * 
 * 
 *         There was a problem with the resource strings and the way the logs
 *         are rendered. This is a quick fix for that problem
 * 
 */
public class CorrectProcessCreationLogs extends WriteCustomTask {

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */
    @Override
    protected void doService() {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(2011);
	Map<WorkflowLog, Siadap> logsToModify = new HashMap<WorkflowLog, Siadap>();
	int processes = 0;
	for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {

	    processes++;
	    for (WorkflowLog log : siadap.getProcess().getExecutionLogs()) {
		if (log instanceof LabelLog) {
		    LabelLog labelLog = (LabelLog) log;
		    if (!StringUtils.isBlank(labelLog.getLabel())
			    && labelLog.getLabel().equals(SiadapProcess.class.getName() + ".creation")
			    && (labelLog.getDescriptionArguments() != null && labelLog.getDescriptionArguments().size() != 4)) {
			logsToModify.put(labelLog, siadap);
		    }
		}
	    }
	}

	//let's print them and change them!!
	out.println("Got " + logsToModify.size() + " logs from " + processes + " processes. Printing them:");
	for (WorkflowLog log : logsToModify.keySet()) {
	    out.print("ORIGINAL: ");
	    out.println(log.getDescription());
	    Siadap siadap = logsToModify.get(log);
	    String competenceType = siadap.getDefaultCompetenceType() == null ? "- não definida -" : siadap
		    .getDefaultCompetenceType().getName();
	    String siadapUniverse = siadap.getDefaultSiadapUniverse() == null ? "- não definido -" : siadap
		    .getDefaultSiadapUniverse().getLocalizedName();
	    log.setDescriptionArguments(new Strings(new String[] { siadap.getEvaluated().getName(),
		    String.valueOf(siadap.getYear()), siadapUniverse, competenceType }));
	    out.print("ALTERED Version: ");
	    out.println(log.getDescription());
	}

    }
}
