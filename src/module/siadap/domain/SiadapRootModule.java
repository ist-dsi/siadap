package module.siadap.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.groups.SiadapCCAGroup;
import module.siadap.domain.groups.SiadapScheduleEditorsGroup;
import module.siadap.domain.groups.SiadapStructureManagementGroup;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.groups.NamedGroup;
import myorg.domain.groups.PersistentGroup;
import myorg.domain.groups.UnionGroup;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;

import pt.ist.fenixWebFramework.services.Service;

public class SiadapRootModule extends SiadapRootModule_Base implements ModuleInitializer {

    private static final Logger LOGGER = Logger.getLogger(SiadapRootModule.class);

    private static boolean isInitialized = false;

    private static NamedGroup siadapTestUserGroup;

    private static ThreadLocal<SiadapRootModule> init = null;
    
    private static final byte[] istLogoBytes;
    //let's init the IST logo needed
    static {
	try {
	    InputStream is = SiadapRootModule.class.getResourceAsStream("/resources/IST-logo.png");
	    istLogoBytes = IOUtils.toByteArray(is);
	    is.close();
	} catch (IOException e) {
	    throw new SiadapException("error.loading.logo", e);
	}
    }

    private SiadapRootModule() {
	super();
	setMyOrg(MyOrg.getInstance());
	setNumber(0);
    }

    public static SiadapRootModule getInstance() {
	if (init != null) {
	    return init.get();
	}

	if (!isInitialized) {
	    initialize();
	}
	final MyOrg myOrg = MyOrg.getInstance();
	return myOrg.getSiadapRootModule();
    }

    @Override
    public void init(MyOrg root) {
	if (getSiadapTestUserGroup() == null) {
	    initializeSiadapGroups(root);
	}
    }

    /**
     * 
     * @param forPerson
     *            the person to whom we should be returning all of the
     *            PersonSiadapWrapper instances related with him
     * @param includeClosedYears
     *            if true, it will return all of the years, even the closed ones
     *            (where nothing should be able to be done for), false isn't
     *            implemented yet, but should return only for the open years
     *            TODO related with Issue #31
     * @return a set of PersonSiadapWrapper with all of the PersonSiadapWrapper
     *         instances associated with the given forPerson person and
     *         including or not closed years TODO depending on the
     *         includeClosedYears parameter. Or an empty list if none are
     *         available
     */
    public ArrayList<PersonSiadapWrapper> getAssociatedSiadaps(Person forPerson, boolean includeClosedYears) {
	ArrayList<PersonSiadapWrapper> personSiadapWrapperToReturn = new ArrayList<PersonSiadapWrapper>();
	//get all of the years
	//TODO implement the includeClosedYears functionality related with Issue #31
	for (SiadapYearConfiguration yearConfiguration : getYearConfigurations()) {
	    personSiadapWrapperToReturn.addAll(getAssociatedSiadaps(forPerson, yearConfiguration.getYear(), includeClosedYears));
	}

	return personSiadapWrapperToReturn;

    }

    public ArrayList<PersonSiadapWrapper> getAssociatedSiadaps(Person forPerson, int year, boolean includeClosedYears) {
	SiadapYearConfiguration yearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	ArrayList<PersonSiadapWrapper> personSiadapWrapperToReturn = new ArrayList<PersonSiadapWrapper>();
	//TODO Related with Issue #31 take the includeClosedYears into account
	if (yearConfiguration == null) {
	    return personSiadapWrapperToReturn;
	}
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(forPerson, year);
	// now let's add all of the evaluated persons to the list
	Set<PersonSiadapWrapper> peopleToEvaluate = personSiadapWrapper.getPeopleToEvaluate();
	if (peopleToEvaluate != null) {
	    personSiadapWrapperToReturn.addAll(peopleToEvaluate);
	}
	personSiadapWrapperToReturn.add(personSiadapWrapper);
	return personSiadapWrapperToReturn;

    }

    @Service
    public synchronized static void initialize() {
	if (!isInitialized) {
	    try {
		final MyOrg myOrg = MyOrg.getInstance();
		final SiadapRootModule system = myOrg.getSiadapRootModule();
		if (system == null) {
		    new SiadapRootModule();
		}
		init = new ThreadLocal<SiadapRootModule>();
		init.set(myOrg.getSiadapRootModule());

		isInitialized = true;
	    } finally {
		init = null;
	    }
	}

    }

    private void initializeSiadapGroups(MyOrg root) {
	SiadapRootModule.getInstance();
	for (PersistentGroup group : root.getPersistentGroups()) {
	    if (group instanceof NamedGroup) {
		//init the named groups
		if (((NamedGroup) group).getGroupName().equals(ImportTestUsers.groupName)) {
		    //init the test user group
		    setSiadapTestUserGroup((NamedGroup) group);
		}
	    }
	}

	if (getSiadapTestUserGroup() == null) {
	    //TODO create it ?!
	}
	if (getSiadapCCAGroup() == null) {
	    setSiadapCCAGroup(new SiadapCCAGroup());
	}
	if (getSiadapScheduleEditorsGroup() == null) {
	    setSiadapScheduleEditorsGroup(new SiadapScheduleEditorsGroup());
	}
	if (getSiadapStructureManagementGroup() == null) {
	    setSiadapStructureManagementGroup(new SiadapStructureManagementGroup());
	}
	if (getStatisticsAccessUnionGroup() == null) {
	    setStatisticsAccessUnionGroup(new UnionGroup(myorg.domain.groups.Role.getRole(myorg.domain.RoleType.MANAGER),
		    getSiadapScheduleEditorsGroup(), getSiadapCCAGroup(), getSiadapStructureManagementGroup()));
	}

    }

    @Override
    public Integer getNumber() {
	throw new UnsupportedOperationException("Use getNumberAndIncrement instead");
    }

    public Integer getNumberAndIncrement() {
	Integer processNumber = super.getNumber();
	setNumber(processNumber + 1);
	return processNumber;
    }

    private void addHarmonizationUnits(Set<Unit> set, SiadapYearConfiguration siadapYearConfiguration, Unit unit) {
	set.add(unit);
	for (Unit iteratingUnit : unit.getChildUnits(siadapYearConfiguration.getUnitRelations())) {
	    if (!iteratingUnit.getChildPersons(siadapYearConfiguration.getHarmonizationResponsibleRelation()).isEmpty()) {
		addHarmonizationUnits(set, siadapYearConfiguration, iteratingUnit);
	    }
	}
    }

    public Set<Unit> getHarmonizationUnits(Integer year) {
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	Unit topUnit = siadapYearConfiguration.getSiadapStructureTopUnit();
	Set<Unit> units = new HashSet<Unit>();
	addHarmonizationUnits(units, siadapYearConfiguration, topUnit);
	return units;
    }

    private static void setSiadapTestUserGroup(NamedGroup siadapTestUserGroup) {
	SiadapRootModule.siadapTestUserGroup = siadapTestUserGroup;
    }

    public NamedGroup getSiadapTestUserGroup() {
	return siadapTestUserGroup;
    }

    /**
     * Method that exports the SIADAP hierarchy
     * 
     * @param year
     *            the year to use to extract the hierarchies
     * @param shouldIncludeEndOfRole
     *            if true, the time limit for the role is also exported
     * @param includeHarmonizationResponsibles
     *            if true, it should also export the harmonization responsibles
     *            for each unit/cost center
     * @return an {@link HSSFWorkbook} with the SIADAP hierarchy
     */
    public HSSFWorkbook exportSIADAPHierarchy(int year, boolean shouldIncludeEndOfRole, boolean includeHarmonizationResponsibles) {
	User user = UserView.getCurrentUser();

	//let's first verify the current user can actually get the information
	if (!SiadapStructureManagementGroup.isMember(user, year))
	    throw new SiadapException("user.not.allowed.to.access.data");


	//let's get the SIADAP information

	SiadapYearConfiguration siadapConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

	Unit siadapStructureTopUnit = siadapConfiguration.getSiadapStructureTopUnit();

	UnitSiadapWrapper wrappedUnit = new UnitSiadapWrapper(siadapStructureTopUnit, year);


	HSSFWorkbook hierarchyWorkbook = new HSSFWorkbook();
	CreationHelper creationHelper = hierarchyWorkbook.getCreationHelper();
	HSSFSheet workingRelationWithQuotasSheet = hierarchyWorkbook.createSheet("Ordenação por Serviço (IST)");
	populateSheet(workingRelationWithQuotasSheet, true, wrappedUnit, hierarchyWorkbook);

	HSSFSheet workingRelationWithoutQuotasSheet = hierarchyWorkbook.createSheet("Ordenação por Serviço (ADIST)");
	populateSheet(workingRelationWithoutQuotasSheet, false, wrappedUnit, hierarchyWorkbook);

	return hierarchyWorkbook;
    }

    private static final int START_CELL_INDEX = 0;

    private static final int START_ROW_INDEX = 0;

    private void populateSheet(HSSFSheet sheetToWriteTo, boolean considerQuotas, UnitSiadapWrapper unitToSearchIn,
	    HSSFWorkbook wb) {

	CreationHelper creationHelper = wb.getCreationHelper();

	//make the sheet fit the page
	PrintSetup ps = sheetToWriteTo.getPrintSetup();

	sheetToWriteTo.setAutobreaks(true);

	ps.setFitHeight((short) 1);
	ps.setFitWidth((short) 1);


	/* ** styles ** */

	//CostCenter style
	HSSFFont costCenterFont = wb.createFont();
	costCenterFont.setColor(HSSFColor.DARK_BLUE.index);
	costCenterFont.setFontHeightInPoints((short) 12);
	costCenterFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	CellStyle costCenterStyle = wb.createCellStyle();
	costCenterStyle.setFont(costCenterFont);

	//make the Unit header style
	CellStyle unitHeaderStyle = wb.createCellStyle();
	unitHeaderStyle.setBorderBottom(CellStyle.BORDER_THIN);
	unitHeaderStyle.setBorderTop(CellStyle.BORDER_THIN);
	unitHeaderStyle.setBorderLeft(CellStyle.BORDER_THIN);
	unitHeaderStyle.setBorderRight(CellStyle.BORDER_THIN);
	unitHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);
	unitHeaderStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	HSSFFont headerFont = wb.createFont();
	headerFont.setFontHeightInPoints((short) 12);
	headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	headerFont.setItalic(true);
	unitHeaderStyle.setFont(headerFont);

	//make the default name style
	CellStyle defaultTextNameStyle = wb.createCellStyle();
	defaultTextNameStyle.setBorderLeft(CellStyle.BORDER_THIN);
	defaultTextNameStyle.setBorderRight(CellStyle.BORDER_THIN);
	defaultTextNameStyle.setBorderBottom(CellStyle.BORDER_NONE);
	defaultTextNameStyle.setBorderTop(CellStyle.BORDER_NONE);
	defaultTextNameStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	HSSFFont defaultFont = wb.createFont();
	defaultFont.setFontHeightInPoints((short) 11);
	defaultTextNameStyle.setFont(defaultFont);

	//make the last line name style
	CellStyle defaultTextNameLastStyle = wb.createCellStyle();
	defaultTextNameLastStyle.setBorderLeft(CellStyle.BORDER_THIN);
	defaultTextNameLastStyle.setBorderRight(CellStyle.BORDER_THIN);
	defaultTextNameLastStyle.setBorderBottom(CellStyle.BORDER_THIN);
	defaultTextNameLastStyle.setBorderTop(CellStyle.BORDER_NONE);
	defaultTextNameLastStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	defaultTextNameLastStyle.setFont(defaultFont);

	//make the default IST-ID last line style
	CellStyle defaultTextIstIdLastStyle = wb.createCellStyle();
	defaultTextIstIdLastStyle.setBorderLeft(CellStyle.BORDER_THIN);
	defaultTextIstIdLastStyle.setBorderBottom(CellStyle.BORDER_THIN);
	defaultTextIstIdLastStyle.setBorderTop(CellStyle.BORDER_NONE);
	defaultTextIstIdLastStyle.setBorderRight(CellStyle.BORDER_THIN);
	defaultTextIstIdLastStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	defaultTextIstIdLastStyle.setAlignment(CellStyle.ALIGN_CENTER);
	defaultTextIstIdLastStyle.setFont(defaultFont);

	//make the default IST-ID style
	CellStyle defaultTextIstIdStyle = wb.createCellStyle();
	defaultTextIstIdStyle.setBorderLeft(CellStyle.BORDER_THIN);
	defaultTextIstIdStyle.setBorderBottom(CellStyle.BORDER_NONE);
	defaultTextIstIdStyle.setBorderTop(CellStyle.BORDER_NONE);
	defaultTextIstIdStyle.setBorderRight(CellStyle.BORDER_THIN);
	defaultTextIstIdStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	defaultTextIstIdStyle.setAlignment(CellStyle.ALIGN_CENTER);
	defaultTextIstIdStyle.setFont(defaultFont);
	
	
	//header style
	
	//	CellStyle headerStyle = wb.createCellStyle();
	//	HSSFFont headerFont = wb.createFont();
	//	headerFont.setFontName(HSSFFont.FONT_ARIAL);
	//	headerFont.setFontHeightInPoints((short) 10);
	//	headerStyle.setFont(headerFont);
	//	
	
	
	//first line style
	CellStyle firstLineStyle = wb.createCellStyle();
	HSSFFont firstLineFont = wb.createFont();
	firstLineFont.setColor(HSSFColor.DARK_BLUE.index);
	firstLineFont.setFontHeightInPoints((short) 14);
	firstLineFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	firstLineStyle.setFont(firstLineFont);
	firstLineStyle.setAlignment(CellStyle.ALIGN_CENTER);
	firstLineStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
	
	//second line style
	CellStyle secondLineStyle = wb.createCellStyle();
	HSSFFont secondLineFont = wb.createFont();
	secondLineFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	secondLineFont.setFontHeightInPoints((short) 14);
	secondLineStyle.setFont(secondLineFont);
	secondLineStyle.setAlignment(CellStyle.ALIGN_CENTER);
	secondLineStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

	/* ** END of styles ** */

	/* ** Immutable IST header ** */
	
	HSSFHeader header = sheetToWriteTo.getHeader();
	header.setCenter(HSSFHeader.font("Arial", "Normal") + HSSFHeader.fontSize((short) 10));
	header.setCenter("Instituto Superior Técnico");

	int rowIndex = START_ROW_INDEX;
	int cellIndex = START_CELL_INDEX;

	int firstLineIndex = rowIndex++;
	int secondLineIndex = rowIndex++;
	/* ** Write the first lines with the dates ** */
	HSSFRow row = sheetToWriteTo.createRow(firstLineIndex);
	HSSFCell cell = row.createCell(cellIndex);
	cell.setCellValue("SIADAP - LISTA DE AVALIADORES " + unitToSearchIn.getYear());
	cell.setCellStyle(firstLineStyle);
	sheetToWriteTo.addMergedRegion(new CellRangeAddress(firstLineIndex, firstLineIndex, cellIndex, cellIndex + 3));

	//second line
	if (!considerQuotas) {
	    cellIndex = START_CELL_INDEX;
	    row = sheetToWriteTo.createRow(secondLineIndex);
	    cell = row.createCell(cellIndex);
	    cell.setCellValue("PESSOAL CONTRATADO PELA ADIST");
	    cell.setCellStyle(secondLineStyle);

	}

	/* ** write the IST logo ** */
	
	int pictureIdx = wb.addPicture(istLogoBytes, Workbook.PICTURE_TYPE_PNG);
	HSSFPatriarch drawingPatriarch = sheetToWriteTo.createDrawingPatriarch();
	ClientAnchor clientAnchor = creationHelper.createClientAnchor();
	clientAnchor.setCol1(cellIndex);
	clientAnchor.setRow1(rowIndex);
	HSSFPicture picture = drawingPatriarch.createPicture(clientAnchor, pictureIdx);

	//let's give the next item some space
	rowIndex += 6;

	/* ** Dynamic IST footer ** */

	HSSFFooter footer = sheetToWriteTo.getFooter();
	footer.setLeft("Lista gerada em: " + HSSFFooter.date() + " " + HSSFFooter.time());
	footer.setCenter(HSSFFooter.page());
	footer.setRight("SIADAP - Lista de avaliadores " + unitToSearchIn.getYear());

	for (UnitSiadapWrapper eachUnit : unitToSearchIn.getAllChildUnits()) {
	    if (eachUnit.getQuotaAwareTotalPeopleWorkingInUnit(false, considerQuotas) > 0) {
		
		row = sheetToWriteTo.createRow(++rowIndex);
		cellIndex = START_CELL_INDEX;
		//write the unit name and cost center
		String unitNameWithCC = eachUnit.getUnit().getPartyName().getContent() + " - CC ";
		unitNameWithCC += eachUnit.getUnit().getExpenditureUnit().getCostCenterUnit().getCostCenter();
		cell = row.createCell(cellIndex++);
		sheetToWriteTo.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, --cellIndex, ++cellIndex));
		cell.setCellValue(unitNameWithCC);
		cell.setCellStyle(costCenterStyle);
		
		/* **** write the Unit header ***** */


		//restart the cell's index
		cellIndex = START_CELL_INDEX;

		//IST id avaliado
		    int firstLineAfterUnitNameIndex = ++rowIndex;
		int secondLineAfterUnitNameIndex = ++rowIndex;

		row = sheetToWriteTo.createRow(firstLineAfterUnitNameIndex);
		cell = row.createCell(cellIndex);
		cell.setCellStyle(unitHeaderStyle);
		cell.setCellValue("IST id.");

		row = sheetToWriteTo.createRow(secondLineAfterUnitNameIndex);
		cell = row.createCell(cellIndex);
		cell.setCellStyle(unitHeaderStyle);

		//merge the IST id
		sheetToWriteTo.addMergedRegion(new CellRangeAddress(firstLineAfterUnitNameIndex, secondLineAfterUnitNameIndex,
			cellIndex, cellIndex));

		//		Nome avaliado
		row = sheetToWriteTo.getRow(firstLineAfterUnitNameIndex);
		cell = row.createCell(++cellIndex);
		cell.setCellStyle(unitHeaderStyle);
		cell.setCellValue("Nome");

		row = sheetToWriteTo.getRow(secondLineAfterUnitNameIndex);
		cell = row.createCell(cellIndex);
		cell.setCellStyle(unitHeaderStyle);

		//merge
		sheetToWriteTo.addMergedRegion(new CellRangeAddress(firstLineAfterUnitNameIndex, secondLineAfterUnitNameIndex,
			cellIndex, cellIndex));

		//Avaliador
		row = sheetToWriteTo.getRow(firstLineAfterUnitNameIndex);
		cell = row.createCell(++cellIndex);
		cell.setCellStyle(unitHeaderStyle);
		cell.setCellValue("IST id.");

		row = sheetToWriteTo.getRow(secondLineAfterUnitNameIndex);
		cell = row.createCell(cellIndex);
		cell.setCellStyle(unitHeaderStyle);

		//merge
		sheetToWriteTo.addMergedRegion(new CellRangeAddress(firstLineAfterUnitNameIndex, secondLineAfterUnitNameIndex,
			cellIndex, cellIndex));

		//ist id avaliador
		row = sheetToWriteTo.getRow(firstLineAfterUnitNameIndex);
		cell = row.createCell(++cellIndex);
		cell.setCellStyle(unitHeaderStyle);
		cell.setCellValue("Avaliador");

		row = sheetToWriteTo.getRow(secondLineAfterUnitNameIndex);
		cell = row.createCell(cellIndex);
		cell.setCellStyle(unitHeaderStyle);

		//merge
		sheetToWriteTo.addMergedRegion(new CellRangeAddress(firstLineAfterUnitNameIndex, secondLineAfterUnitNameIndex,
			cellIndex, cellIndex));

		List<PersonSiadapWrapper> listToUse = (considerQuotas) ? eachUnit.getUnitEmployeesWithQuotas(false) : eachUnit
			.getUnitEmployeesWithoutQuotas(true);

		//now let's take care of exporting the persons
		for (PersonSiadapWrapper personWrapper : listToUse) {
		    row = sheetToWriteTo.createRow(++rowIndex);
		    //restart the cell's index
		    cellIndex = START_CELL_INDEX;
		    String istIdEvaluated = personWrapper.getPerson().getUser().getUsername();
		    cell = row.createCell(cellIndex++);
		    cell.setCellValue(istIdEvaluated);
		    cell.setCellStyle(defaultTextIstIdStyle);

		    String nameEvaluatedPerson = personWrapper.getPerson().getName();
		    cell = row.createCell(cellIndex++);
		    cell.setCellValue(nameEvaluatedPerson);
		    cell.setCellStyle(defaultTextNameStyle);

		    PersonSiadapWrapper evaluatorWrapper = personWrapper.getEvaluator();
		    String istIdEvaluator = evaluatorWrapper.getPerson().getUser().getUsername();
		    cell = row.createCell(cellIndex++);
		    cell.setCellValue(istIdEvaluator);
		    cell.setCellStyle(defaultTextIstIdStyle);

		    String nameEvaluatorWrapper = evaluatorWrapper.getName();
		    cell = row.createCell(cellIndex++);
		    cell.setCellValue(nameEvaluatorWrapper);
		    cell.setCellStyle(defaultTextNameStyle);

		}
		//let's make a bottom border on the last four cells
		for (int i = START_CELL_INDEX; i < START_CELL_INDEX + 4; i++) {
		    cell = row.getCell(i);
		    //let's diferentaitate between the IST-id and the name
		    if (i == START_CELL_INDEX || i == START_CELL_INDEX + 2) //first cell, IST-ID then. or third cell the other IST-ID
		    {
			cell.setCellStyle(defaultTextIstIdLastStyle);
		    } else {
			cell.setCellStyle(defaultTextNameLastStyle);
		    }

		}
		row = sheetToWriteTo.createRow(++rowIndex);
		row = sheetToWriteTo.createRow(++rowIndex);

	    }

	}

	sheetToWriteTo.autoSizeColumn(START_CELL_INDEX);
	sheetToWriteTo.autoSizeColumn(START_CELL_INDEX + 1);
	sheetToWriteTo.autoSizeColumn(START_CELL_INDEX + 2);
	sheetToWriteTo.autoSizeColumn(START_CELL_INDEX + 3);

	//now let's resize the logo
	picture.resize();
    }
}
