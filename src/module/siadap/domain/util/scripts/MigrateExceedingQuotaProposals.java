/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import module.organization.domain.Unit;
import module.siadap.domain.ExceedingQuotaProposal;
import module.siadap.domain.ExceedingQuotaSuggestionType;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import myorg.domain.scheduler.WriteCustomTask;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 30 de Jan de 2012
 * 
 *         Get the old {@link ExceedingQuotaProposal} and migrate them so that
 *         they have sequential numbers for each combination of:
 *         {@link SiadapUniverse}, {@link ExceedingQuotaSuggestionType}, boolean
 *         quotasUniverse, and {@link Unit}.
 * 
 */
public class MigrateExceedingQuotaProposals extends WriteCustomTask {

    /*
     * (non-Javadoc)
     * 
     * @see myorg.domain.scheduler.WriteCustomTask#doService()
     */

    HashMap<String, List<ExceedingQuotaProposal>> exceedingQuotaProposalsGrouper = new HashMap<String, List<ExceedingQuotaProposal>>();
    private boolean gotANullAttr = false;

    @Override
    protected void doService() {
	//let's get them all!!
	int count = 0;
	for (ExceedingQuotaProposal proposal : SiadapRootModule.getInstance().getExceedingQuotasProposals()) {
	    count++;
	    String generateKeyForGrouper = generateKeyForGrouper(proposal);
	    List<ExceedingQuotaProposal> list = exceedingQuotaProposalsGrouper.get(generateKeyForGrouper);
	    if (list == null) {
		list = new ArrayList<ExceedingQuotaProposal>();
		exceedingQuotaProposalsGrouper.put(generateKeyForGrouper, list);
	    }
	    list.add(proposal);

	}

	out.println("Got '" + count + "' proposals");


	if (gotANullAttr) {
	    out.println("aborting as we got a null in a key");
	    return;
	}
	
	//let's migrate the stuff by assigning the correct proposal orders
	for (List<ExceedingQuotaProposal> exceedingQuotaProposals : exceedingQuotaProposalsGrouper.values()) {
	    Collections.sort(exceedingQuotaProposals, ExceedingQuotaProposal.COMPARATOR_BY_PRIORITY_NUMBER);
	}
	
	//now everything should be in order, let's confirm
	out.println("Printing the grouped proposals");
	if (true)
	{
	    for (String key : exceedingQuotaProposalsGrouper.keySet())
	    {
		out.print(key);
		out.println("|||| Unit name: '"
			+ exceedingQuotaProposalsGrouper.get(key).get(0).getUnit().getPartyName().getContent() + "'");
		int newOrder = 0;
		for (ExceedingQuotaProposal proposal : exceedingQuotaProposalsGrouper.get(key))
		{
		    newOrder++;
		    out.println(proposal.getProposalOrder() + " new order: " + newOrder);
		    proposal.setProposalOrderProtectedForScript(newOrder);
		}
	    }
	    
	}

	//so, now let's migrate everything
    }

    private String generateKeyForGrouper(ExceedingQuotaProposal proposal) {
	String stringToReturn = String.valueOf(proposal.getYear()) + String.valueOf(proposal.getSuggestionType())
		+ String.valueOf(proposal.getWithinOrganizationQuotaUniverse()) + String.valueOf(proposal.getSiadapUniverse())
		+ String.valueOf(proposal.getUnit().getExternalId());
	if (stringToReturn.contains("null")) {
	    out.println("Got a null attr. Key: '" + stringToReturn + "'");
	    gotANullAttr = true;
	}
	return stringToReturn;
    }

}
