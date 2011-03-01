package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapEvaluationItem;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.User;
import myorg.domain.scheduler.ReadCustomTask;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class GetSiadapStatisticTotalCounts extends ReadCustomTask {

    public static Set<User> mappedUsers = new HashSet<User>();

    @Override
    public void doIt() {
	//let's iterate throught the relations to get a count of all of the users
	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	out.println("Retrieving statistics for SIADAP for year: " + today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	AccountabilityType workingUnitWithQuotaRelation = configuration.getWorkingRelation();
	AccountabilityType workingUnitWithoutQuotaRelation = configuration.getWorkingRelationWithNoQuota();

	//iterate through all of the accountabilities and get all of the users involved in the process

	List<Accountability> allAccountabilities = new ArrayList<Accountability>();
	allAccountabilities.addAll(evaluationRelation.getAccountabilities());
	allAccountabilities.addAll(workingUnitWithoutQuotaRelation.getAccountabilities());
	allAccountabilities.addAll(workingUnitWithQuotaRelation.getAccountabilities());

	out.println("Going to iterate through " + allAccountabilities.size() + " accountabilties that might be relevant");
	System.out.println("Going to iterate through " + allAccountabilities.size() + " accountabilties that might be relevant");
	for (Accountability accountability : allAccountabilities) {
	    //let's filter the accountabilities
	    if (accountability.isActive(today)) {
		if (accountability.getParent() instanceof Person) {
		    mappedUsers.add(((Person) accountability.getParent()).getUser());
		}
		if (accountability.getChild() instanceof Person) {
		    mappedUsers.add(((Person) accountability.getChild()).getUser());
		}
	    }
	}

	out.println("Got " + mappedUsers.size() + " users");
	System.out.println("Got " + mappedUsers.size() + " users");

	int nrOfCreatedSiadapProcesses = 0;
	int nrOfSealedSiadapProcesses = 0;
	int nrOfSubmittedForAcknowledgement = 0;
	int nrOfAcknowledged = 0;

	boolean printedInexistantProcessUser = false;
	boolean printedUnsealedProcessUser = false;
	boolean printedUnsubmittedProcessUser = false;
	boolean printedUnacknowledgedProcessUser = false;

	boolean printOneOfEach = true;

	for (User user : mappedUsers) {
	    Siadap siadap = configuration.getSiadapFor(user.getPerson(), today.getYear());

	    if (siadap != null) {
		nrOfCreatedSiadapProcesses++;
		if (siadap.getObjectivesAndCompetencesSealedDate() != null) {
		    nrOfSealedSiadapProcesses++;
		    if (siadap.getRequestedAcknowledgeDate() != null) {
			nrOfSubmittedForAcknowledgement++;
			List<SiadapEvaluationItem> evaluationItems = siadap.getCurrentEvaluationItems();
			if (evaluationItems != null && evaluationItems.size() >= 1
				&& evaluationItems.get(0).getAcknowledgeDate() != null) {
			    nrOfAcknowledged++;
			    if (nrOfAcknowledged == 1 && printOneOfEach) {

				out.println("User with an acknowledged process: " + user.getUsername());
			    }
			} //siadap process hasn't been acknowledged
			else if (!printedUnacknowledgedProcessUser && printOneOfEach) {
			    printedUnacknowledgedProcessUser = true;
			    out.println("User with unacknowledged process: " + user.getUsername());
			}
		    } //siadap process hasn't been submited for acknowledgement
		    else if (!printedUnsubmittedProcessUser && printOneOfEach) {
			printedUnsubmittedProcessUser = true;
			out.println("User with unsubmitted for ack process: " + user.getUsername());

		    }

		} //siadap processe hasn't been sealed
		else if (!printedUnsealedProcessUser && printOneOfEach) {
		    printedUnsealedProcessUser = true;
		    out.println("User with unsealed process: " + user.getUsername());
		}

	    } //siadap process is null
	    else if (!printedInexistantProcessUser && printOneOfEach) {
		printedInexistantProcessUser = true;
		out.println("User with no SIADAP process: " + user.getUsername());
	    }
	}

	out.println("Nr of SIADAP processes created: " + nrOfCreatedSiadapProcesses);
	out.println("Nr of SIADAP processes sealed: " + nrOfSealedSiadapProcesses);
	out.println("Nr of SIADAP processes submitted for acknowledgement: " + nrOfSubmittedForAcknowledgement);
	out.println("Nr of SIADAP processes acknowledged: " + nrOfAcknowledged);
	out.println("Statistics gathered at " + new DateTime());

    }
}
