package module.siadap.domain.util.scripts;

import java.util.List;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.scheduler.WriteCustomTask;

import org.joda.time.LocalDate;

public class MakeXRelationsEndAtEndYear extends WriteCustomTask {

    int nrOfEvalAccEnded = 0;

    @Override
    protected void doService() {

	out.println("STarting");

	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	AccountabilityType workingRelation = configuration.getWorkingRelation();
	AccountabilityType workingRelationWithNoQuota = configuration.getWorkingRelationWithNoQuota();

	//let's get all of the evaluation relations already set

	List<Accountability> evaluationAccountabilities = configuration.getEvaluationRelation().getAccountabilities();

	endUnfinishedAcc(workingRelationWithNoQuota);
	endUnfinishedAcc(workingRelation);



	out.println("Done. Converted " + nrOfEvalAccEnded + " eval relations");

    }

    private void endUnfinishedAcc(AccountabilityType accType) {
	for (Accountability accountability : accType.getAccountabilities()) {
	    LocalDate beginDate = accountability.getBeginDate();
	    LocalDate endDate = accountability.getEndDate();
	    if (endDate == null) {
		endDate = new LocalDate(beginDate.getYear(), 12, 31);
		accountability.setEndDate(endDate);
		nrOfEvalAccEnded++;
	    }
	}
    }


}
