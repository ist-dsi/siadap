package module.siadap.domain.util.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.PartyType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.domain.User;
import myorg.domain.scheduler.ReadCustomTask;
import myorg.domain.scheduler.TransactionalThread;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

/**
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 16 de Dez de 2011
 * 
 *         Script that imports the harmonization units in the format: istId of
 *         the responsible of harmonization;Name of the harm. unit;cost center
 *         of the subunits
 */
public class ImportHarmonizationUnitStructureFromFile extends ReadCustomTask {

    public final static String csvContent = new String("ist23470;Unidade de harm. da Direcção Técnica Geral;6301\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6311\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6312\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6313\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6315\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6316\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6331\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6332\n"
	    + "ist23470;Unidade de harm. da Direcção Técnica Geral;6333\n"
	    + "ist12037;Unidade de harm. do Departamento de Engenharia e Gestão;2901\n");

    public final static HashMap<String, HarmonizationUnit> inferedUnits = new HashMap<String, HarmonizationUnit>();
    public final int yearToUse = 2011;

    class HarmonizationUnit {
	private final String unitName;
	private final Set<Unit> subUnits;
	private final User harmonizationResponsible;

	public HarmonizationUnit(String unitName, User harmonizationResponsible) {
	    subUnits = new HashSet<Unit>();
	    this.unitName = unitName;
	    this.harmonizationResponsible = harmonizationResponsible;
	}

	public void addCC(Unit ccUnit) {
	    getSubUnits().add(ccUnit);
	}

	public Set<Unit> getSubUnits() {
	    return subUnits;
	}

	public User getHarmonizationResponsible() {
	    return harmonizationResponsible;
	}

	public String getUnitName() {
	    return unitName;
	}

    }

    @Override
    public void doIt() {

	//	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(yearToUse);

	//let's read each line and create the appropriate objects
	try {
	    StringReader csvContentReader = new StringReader(csvContent);
	    BufferedReader br = new BufferedReader(csvContentReader);
	    String strLine;
	    // Read File Line By Line
	    while ((strLine = br.readLine()) != null) {
		processLine(strLine);

	    }
	} catch (IOException e) {// Catch exception if any
	    out.println("Error: " + e.getMessage());
	}

	//print what you got
	for (HarmonizationUnit harmUnit : inferedUnits.values()) {
	    for (Unit costCenter : harmUnit.getSubUnits()) {
		out.println(harmUnit.getHarmonizationResponsible().getUsername() + ";" + harmUnit.getUnitName() + ";"
			+ costCenter.getAcronym());
	    }
	}

	ProcessHarmonizationUnits processHarmonizationUnits = new ProcessHarmonizationUnits(inferedUnits.values());
	processHarmonizationUnits.start();
	try {
	    processHarmonizationUnits.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    e.printStackTrace(out);
	    throw new Error(e);
	}

    }

    class ProcessHarmonizationUnits extends TransactionalThread {

	final Collection<HarmonizationUnit> harmonizationUnits;
	private final AccountabilityType unitRelations;
	private final Unit siadapStructureTopUnit;
	private final PartyType siadapHarmonizationUnitType;
	private OrganizationalModel organizationModelToUse;
	private int unitsCount;
	private final AccountabilityType harmonizationResponsibleRelation;

	ProcessHarmonizationUnits(Collection<HarmonizationUnit> harmonizationUnits) {
	    this.harmonizationUnits = harmonizationUnits;
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(yearToUse);
	    unitRelations = siadapYearConfiguration.getUnitRelations();
	    siadapStructureTopUnit = siadapYearConfiguration.getSiadapStructureTopUnit();
	    //	    for (HarmonizationUnit harmonizationUnit : harmonizationUnits)
	    //	    {
	    //		for (Unit subUnit : harmonizationUnit.getSubUnits())
	    //		{
	    //		    subUnit.getOr
	    //		}
	    //	    }

	    siadapHarmonizationUnitType = PartyType.readBy(UnitSiadapWrapper.SIADAP_HARMONIZATION_UNIT_TYPE);
	    for (OrganizationalModel organizationModel : siadapStructureTopUnit.getOrganizationalModels()) {
		if (organizationModel.getName().equalInAnyLanguage(UnitSiadapWrapper.SIADAP_ORGANIZATION_MODEL_NAME))
		    organizationModelToUse = organizationModel;
	    }
	    harmonizationResponsibleRelation = siadapYearConfiguration.getHarmonizationResponsibleRelation();
	    this.unitsCount = siadapStructureTopUnit.getChildren(unitRelations).size();

	}

	@Override
	public void transactionalRun() {
	    for (HarmonizationUnit harmonizationUnit : harmonizationUnits) {
		debugLn("Processing unit " + harmonizationUnit.getUnitName());
		//let's find out if we have a unit with this name already
		Unit unitToUse = null;
		for (Unit unit : siadapStructureTopUnit.getChildUnits(unitRelations)) {
		    if (unit.getPartyName().equalInAnyLanguage(harmonizationUnit.getUnitName())
			    && unit.getPartyTypes().contains(siadapHarmonizationUnitType)) {
			unitToUse = unit;
			break;
			//let's find out if it's of the right type
			//			for (PartyType partyType : unit.getPartyTypes()) {
			//			    if (partyType.getType().equalsIgnoreCase(UnitSiadapWrapper.SIADAP_HARMONIZATION_UNIT_TYPE)) {
			//				unitToUse = unit;//that means we found an already existing unit
			//				break;
			//			    }
			//			}
		    }
		}

		//let's create it if we need to
		if (unitToUse == null) {
		    debugLn(" -- creating it");
		    unitToUse = Unit.create(siadapStructureTopUnit, new MultiLanguageString(harmonizationUnit.getUnitName()),
			    "SIADAP - U.H." + String.valueOf(unitsCount++), siadapHarmonizationUnitType, unitRelations,
			    new LocalDate(), lastDayOfYear(yearToUse), organizationModelToUse);
		} else
		    debugLn(" -- It already existed");
		//so now let's just add all of the subunits
		Collection<Unit> previousChildUnits = unitToUse.getChildUnits(unitRelations);
		for (Unit subUnit : harmonizationUnit.getSubUnits()) {
		    if (!previousChildUnits.contains(subUnit)) {
			debugLn(" ---- creating the relation with " + subUnit.getPartyName().getContent());
			subUnit.addParent(unitToUse, unitRelations, new LocalDate(), lastDayOfYear(yearToUse));
		    } else {
			debugLn(" ---- relation with " + subUnit.getPartyName().getContent() + " already existed");
		    }
		}
		//let's remove the ones that don't belong
		for (Unit previousChildUnit : previousChildUnits) {
		    if (!harmonizationUnit.getSubUnits().contains(previousChildUnit))
		    //let's remove it
		    {
			for (Accountability previousAccs : previousChildUnit.getParentAccountabilities(unitRelations)) {
			    if (previousAccs.getParent().equals(unitToUse)) {
				//this is one that should be removed
				debugLn(" ---- removing relation with " + previousChildUnit.getPartyName().getContent());
				previousChildUnit.removeParent(previousAccs);
			    }
			}
		    }
		}
		
		//and now let's take care of the responsible
		Collection<Person> previousResponsibles = unitToUse.getChildPersons(harmonizationResponsibleRelation);
		boolean usableAccNotFound = true;
		for (Accountability acc : unitToUse.getChildAccountabilities())
		{
		    if (acc.getAccountabilityType().equals(harmonizationResponsibleRelation))
		    {
			if (acc.getChild().equals(harmonizationUnit.getHarmonizationResponsible().getPerson()))
			{
			    //			    if (acc.isActiveNow()) {
				//make sure that it will be active until the end of the year
			    debugLn(" ------ Acc. for responsible added/augmented");
				acc.editDates(acc.getBeginDate(), lastDayOfYear(yearToUse));
			    //			    }
			    usableAccNotFound = false;
			}
			else if (acc.isActiveNow())
			{
			    //let's 'remove' it
			    debugLn(" ------ Acc. for previous responsible ended now");
			    acc.editDates(acc.getBeginDate(), new LocalDate());
			}
		    }
		}
		if (usableAccNotFound)
		{
		    //let's add the responsible
		    unitToUse.addChild(harmonizationUnit.getHarmonizationResponsible().getPerson(),
			    harmonizationResponsibleRelation, new LocalDate(), lastDayOfYear(yearToUse));
		    debugLn(" ------ Added the responsible, no previous relation was found");
		}

	    }

	}
    }

    private void debugLn(String message) {
	if (true)
	    out.println(message);
    }

    private LocalDate lastDayOfYear(int year) {
	return new LocalDate(year, 12, 31);
    }

    private void processLine(String strLine) {

	String[] values = strLine.split(";");

	if (values.length != 3) {
	    out.println("skipped: " + strLine);
	    return;
	}

	String istIdHarmResponsible = values[0].trim();
	String harmUnitName = values[1].trim();
	Integer ccNumber = Integer.valueOf(values[2].trim());

	if (StringUtils.isBlank(istIdHarmResponsible) || StringUtils.isBlank(harmUnitName)
		|| User.findByUsername(istIdHarmResponsible) == null) {
	    out.println("skipped: " + strLine + "could not get needed values");
	    return;
	}

	User istIdHarmUser = User.findByUsername(istIdHarmResponsible);

	CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(ccNumber.toString());
	Unit ccUnit = c.getUnit();

	//let's try to get the harmonization unit
	HarmonizationUnit harmonizationUnit = inferedUnits.get(harmUnitName);
	if (harmonizationUnit == null) {
	    harmonizationUnit = new HarmonizationUnit(harmUnitName, istIdHarmUser);
	}
	harmonizationUnit.addCC(ccUnit);

	inferedUnits.put(harmUnitName, harmonizationUnit);

    }

}