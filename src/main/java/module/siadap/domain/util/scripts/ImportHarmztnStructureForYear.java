/**
 * 
 */
package module.siadap.domain.util.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import module.fileManagement.domain.FileNode;
import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.utl.ist.fenix.tools.util.i18n.Language;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 21 de Jan de 2013
 * 
 *         Given a CSV with the following format:
 * 
 *         Name of the unit ; Cost center number ; Harmonization unit name ;
 *         Number of Harmonization unit \n
 * 
 *         and the year, it makes all of the units there under the given
 *         harmonization unit name for that given year. If the harmonization
 *         number doesn't exist, it creates it; If the names differ, it gives a
 *         warning and makes no writes;
 * 
 * 
 * 
 */
public class ImportHarmztnStructureForYear extends WriteCustomTask {

	private static final FileNode dataFileNode = AbstractDomainObject
			.fromExternalId("7073811478915");

	private static final int year = 2012;

	private static final boolean DRY_RUN = false;

	private static final boolean DEBUG = true;

	private static final boolean IGNORE_DIFFERENT_NAMES_IN_CC_UNITS = true;

	private static final String VIRTUAL_HOST_SERVER_NAME = "joantune-workstation";

	private static final String CHARSET_NAME = "iso-8859-1";

	private static final String JUSTIFICATION = "Importação dos dados de harmonização facultados pela DRH, com o nome Harmonizacao SIADAP 17-01-2013 e alterações as bibliotecas";

	/*
	 * (non-Javadoc)
	 * 
	 * @see pt.ist.bennu.core.domain.scheduler.WriteCustomTask#doService()
	 */
	@Override
	protected void doService() {

		int nrLinesInFile = 0;
		try {

			VirtualHost virtualHost = VirtualHost
					.setVirtualHostForThread(VIRTUAL_HOST_SERVER_NAME);

			// let's get the file content into a string
			BufferedReader contentReader = new BufferedReader(
					new InputStreamReader(dataFileNode.getDocument()
							.getLastVersionedFile().getStream(),
							Charset.forName(CHARSET_NAME)));
			String strLine;
			try {
				while ((strLine = contentReader.readLine()) != null) {
					if (DEBUG)
						out.println(strLine);
					readLine(strLine);
					nrLinesInFile++;
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new DomainException("error reading the document", e);
			}

			out.println("Number of lines in file: " + nrLinesInFile);

			if (printAndValidateImportedResults()) {

				if (!DRY_RUN) {
					Map<Unit, Set<Unit>> abandonedUnits = commitChanges();
					
					//let's print the abandonedUnits
					print(abandonedUnits);
					
				}

			}
		} finally {
			VirtualHost.releaseVirtualHostFromThread();
		}
	}

	private void print(Map<Unit, Set<Unit>> abandonedUnits) {
		for (Unit deactivatedHUnit : abandonedUnits.keySet()) {
			out.println("Abandoned units for H.U.: " + deactivatedHUnit.getPresentationName()+ " :");
			for (Unit abadonedUnit : abandonedUnits.get(deactivatedHUnit)) {
				out.println(abadonedUnit.getPresentationName() + " people working there: " + new UnitSiadapWrapper(abadonedUnit, year).getTotalPeopleWorkingInUnitIncludingNoQuotaPeople());
			}
		}
		
	}

	private Map<Unit,Set<Unit>> commitChanges() {
		Set<Unit> unitsToDeactivate = new HashSet<Unit>();
		for (HarmonizationUnit harmonizationUnit : importedHarmonizationUnits.values()) {
			harmonizationUnit.fillOrCreateUnit();
			
			Unit realUnit = harmonizationUnit.getRealUnit();
			
			//let's do the deactivated units later, to see if we have pending units connected to this one or not
			if (harmonizationUnit.isDeactivated())
			{
				unitsToDeactivate.add(realUnit);
				continue;
			}
				
			
			UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(realUnit, year);
			
			//let's make sure it is connected for that given year
			unitSiadapWrapper.connectToTopHarmonizationUnit(JUSTIFICATION);
			
			List<UnitSiadapWrapper> currentUnitsHarmonized = unitSiadapWrapper.getSubHarmonizationUnits();
			
			//let's get the new set of units
			Collection<UnitSiadapWrapper> newListOfUnits = Collections2.transform(harmonizationUnit.getUnitsHarmonized(), new Function<CCUnit, UnitSiadapWrapper>() {

				@Override
				@Nullable
				public UnitSiadapWrapper apply(@Nullable CCUnit ccUnit) {
					if (ccUnit == null)
						return null;
					ccUnit.fillUnit();
					return new UnitSiadapWrapper(ccUnit.getRealUnit(), year);
					
				}
			});
			
			
			//let's get the list of units to remove from being harmonized:
			currentUnitsHarmonized.removeAll(newListOfUnits);
			
			//let's remove all of the units that are already harmonized by this H.U.
			newListOfUnits.removeAll(unitSiadapWrapper.getSubHarmonizationUnits());
			
			addSubHarmonizationUnits(realUnit, new HashSet(newListOfUnits));
			
			deleteSubHarmonizationUnits(realUnit, new HashSet(currentUnitsHarmonized));
			
		}
		
		Map<Unit,Set<Unit>> abandonedUnits = new HashMap<Unit, Set<Unit>>();
		//now let's take care of the deactivated units
		for (Unit unitToDeactivate : unitsToDeactivate) {
			//let's get all of the sub units and 
			UnitSiadapWrapper unitToDeactivateWrapper = new UnitSiadapWrapper(unitToDeactivate, year);
			List<UnitSiadapWrapper> subHarmonizationWrappedUnits = unitToDeactivateWrapper.getSubHarmonizationUnits();
			if (!subHarmonizationWrappedUnits.isEmpty())
			{
				Collection<Unit> units = Collections2.transform(subHarmonizationWrappedUnits, new Function<UnitSiadapWrapper, Unit>() {

					@Override
					@Nullable
					public Unit apply(@Nullable UnitSiadapWrapper input) {
						if (input == null)
							return null;
						return input.getUnit();
					}
				});
				//let's add it to the abandonedUnits
				abandonedUnits.put(unitToDeactivate, new HashSet<Unit>(units));
				
				//let's disconnect it from the top unit
				unitToDeactivateWrapper.deactivateHarmonizationUnit(JUSTIFICATION);
			}
		}
		
		return abandonedUnits;
		
	}
	
	private void deleteSubHarmonizationUnits(Unit harmonizationUnit, Set<UnitSiadapWrapper> unitsToRemove) {
		for (UnitSiadapWrapper unitToRemove : unitsToRemove) {
			UnitSiadapWrapper.removeHarmonizationUnitRelation(harmonizationUnit, unitToRemove.getUnit(), year, JUSTIFICATION);
		}
		
	}

	public void addSubHarmonizationUnits(Unit harmonizationUnit, Set<UnitSiadapWrapper> unitsToHarmonize) {
		for (UnitSiadapWrapper unitToAdd : unitsToHarmonize) {
			UnitSiadapWrapper.addHarmonizationUnitRelation(harmonizationUnit, unitToAdd.getUnit(), year, JUSTIFICATION);
		}
		
	}

	boolean printAndValidateImportedResults() {
		out.println("-----");
		int nrUnitsToCreate = 0;
		int nrUnitsToDisable = 0;
		int nrHarmonizationUnits = 0;
		int nrIncorrectData = 0;
		int totalEntries = 0;

		// let's also print out the information we have
		for (HarmonizationUnit harmonizationUnit : importedHarmonizationUnits
				.values()) {
			nrHarmonizationUnits++;
			if (harmonizationUnit.isDeactivated()) {
				totalEntries++;
				nrUnitsToDisable++;
			} else if (harmonizationUnit.isToBeCreated()) {
				nrUnitsToCreate++;
			}
			if (!harmonizationUnit.isValid()) {
				nrIncorrectData++;
				try {
					harmonizationUnit.validate();
				} catch (SiadapException ex) {
					out.println(ex.getMessage());
				}

			}
			for (CCUnit ccUnit : harmonizationUnit.getUnitsHarmonized()) {
				if (!ccUnit.isValid()) {
					nrIncorrectData++;
					try {
						ccUnit.validate();
					} catch (SiadapException ex) {
						out.println(ex.getMessage());
					}
				}
				totalEntries++;
			}

		}
		// let's print the summary information
		out.println("Total entries detected: " + totalEntries + " of which "
				+ nrIncorrectData + " are incorrect");
		out.println("Total nr of harmonization units found : "
				+ nrHarmonizationUnits + " Harmonization units to create: "
				+ nrUnitsToCreate + " nr harmonization units to disable: "
				+ nrUnitsToDisable);

		return nrIncorrectData == 0;
	}

	public static class CCUnit {

		private final String name;
		private final int costCenterNumber;

		private Unit realUnit;

		public Unit getRealUnit() {
			return realUnit;
		}

		public CCUnit(String name, int number) {
			this.name = name;
			this.costCenterNumber = number;
		}

		public void validate() {

			String costCenterNumberString = String
					.valueOf(getCostCenterNumber());

			if (costCenterNumberString.length() < 4) {
				// we have to add zeros
				while (costCenterNumberString.length() < 4) {
					costCenterNumberString = "0" + costCenterNumberString;
				}
			}

			pt.ist.expenditureTrackingSystem.domain.organization.Unit costCenterUnit = CostCenter
					.findUnitByCostCenter(String
							.valueOf(costCenterNumberString));
			if (costCenterUnit == null)
				throw new SiadapException("Unit of CC. "
						+ getCostCenterNumber() + " doesn't exist");
			Unit unit = costCenterUnit.getUnit();
			if (!IGNORE_DIFFERENT_NAMES_IN_CC_UNITS
					&& !containsNameIgnoreCase(unit, getName()))
				throw new SiadapException("Names differ: CC. "
						+ getCostCenterNumber()
						+ " name found on import data: " + getName()
						+ " actual presentation name: "
						+ unit.getPresentationName());
		}

		public void fillUnit() {
			validate();
			String costCenterNumberString = String
					.valueOf(getCostCenterNumber());

			if (costCenterNumberString.length() < 4) {
				// we have to add zeros
				while (costCenterNumberString.length() < 4) {
					costCenterNumberString = "0" + costCenterNumberString;
				}
			}

			this.realUnit = CostCenter.findUnitByCostCenter(
					String.valueOf(costCenterNumberString)).getUnit();
		}

		public boolean isValid() {
			try {
				validate();
				return true;
			} catch (SiadapException ex) {
				return false;
			}
		}

		@Override
		public boolean equals(Object obj) {
			if ((obj instanceof CCUnit) == false)
				return false;
			CCUnit ccUnitToCompare = (CCUnit) obj;
			if (!StringUtils.equals(ccUnitToCompare.getName(), getName()))
				return false;

			if (ccUnitToCompare.getCostCenterNumber() != getCostCenterNumber())
				return false;
			return true;

		}
		@Override
		public int hashCode() {
			return getCostCenterNumber() + getName().hashCode();
		}

		public String getName() {
			return name;
		}

		public int getCostCenterNumber() {
			return costCenterNumber;
		}
	}

	static boolean containsNameIgnoreCase(Unit unit, String name) {
		Collection<String> unitNames = unit.getPartyName().getAllContents();
		for (String unitName : unitNames) {
			if (unitName.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}

	public static class HarmonizationUnit {

		private final String name;
		private final int number;

		private final Set<CCUnit> unitsHarmonized;

		private final boolean deactivated;

		public boolean isDeactivated() {
			return deactivated;
		}

		private Unit realUnit;

		public HarmonizationUnit(String name, int number, boolean isDeactivated) {
			this.name = name;
			this.number = number;
			this.unitsHarmonized = new HashSet<CCUnit>();
			this.deactivated = isDeactivated;
		}

		public void addUnit(CCUnit unit) {
			getUnitsHarmonized().add(unit);
		}

		public boolean isValid() {
			try {
				validate();
				return true;
			} catch (SiadapException ex) {
				return false;
			}
		}

		/**
		 * Uses the given number to retrieve the harmonization unit, and checks
		 * on the name.
		 * 
		 * @throws SiadapException
		 *             if the name and the number don't correspond isn't the
		 *             same
		 */
		public void validate() throws SiadapException {
			Unit unit = UnitSiadapWrapper.getHarmonizationUnit(number);
			if (unit == null) {
				if (deactivated) {
					throw new SiadapException(
							"There was a deactivated unit to be processed, but we couldn't find it in the first place. Unit nr: "
									+ number);
				} else
					return; // this is probably a new Harmonization unit to be
							// made

			}
			Collection<String> unitNames = unit.getPartyName().getAllContents();
			if (!containsNameIgnoreCase(unit, name))
				throw new SiadapException("Given name '" + name
						+ "' not found for H.U. nr: " + number
						+ " name found: " + unit.getPresentationName());
		}

		public boolean isToBeCreated() {
			if (deactivated)
				return false;
			Unit unit = UnitSiadapWrapper.getHarmonizationUnit(number);
			if (unit == null)
				return true;
			return false;
		}

		@SuppressWarnings("boxing")
		public void fillOrCreateUnit() {
			validate();
			if (isToBeCreated()) {
				this.realUnit = UnitSiadapWrapper.createSiadapHarmonizationUnit(year,
						new MultiLanguageString(Language.pt, name), number);

			} else {
				this.realUnit=UnitSiadapWrapper.getHarmonizationUnit(number);
			}

		}

		public Set<CCUnit> getUnitsHarmonized() {
			return unitsHarmonized;
		}

		public Unit getRealUnit() {
			return realUnit;
		}


	}

	static final Map<Integer, HarmonizationUnit> importedHarmonizationUnits = new HashMap<Integer, HarmonizationUnit>();

	private void readLine(String readLine) {

		String[] values = readLine.split(";");

		if (values.length != 4) {
			out.println("skipped line: " + readLine);
			return;
		}

		String unitName = values[0].trim();
		String costCenterNumberString = values[1].trim();
		String harmonizationUnitName = values[2].trim();
		String harmonizationUnitNumberString = values[3].trim();

		boolean deactivatedHarmonizationUnit = false;

		Integer costCenterNumber = null;
		try {
			costCenterNumber = Integer.valueOf(costCenterNumberString);
		} catch (NumberFormatException ex) {
			if (unitName.equalsIgnoreCase("desactivada")) {
				deactivatedHarmonizationUnit = true;
			} else
				throw ex;
		}

		Integer harmonizationUnitNumber = Integer
				.valueOf(harmonizationUnitNumberString);

		HarmonizationUnit harmonizationUnit = importedHarmonizationUnits
				.get(harmonizationUnitNumber);
		if (harmonizationUnit == null) {
			harmonizationUnit = new HarmonizationUnit(harmonizationUnitName,
					harmonizationUnitNumber, deactivatedHarmonizationUnit);
			importedHarmonizationUnits.put(harmonizationUnitNumber,
					harmonizationUnit);
		}

		if (!deactivatedHarmonizationUnit)
			harmonizationUnit.addUnit(new CCUnit(unitName, costCenterNumber));

	}

}
