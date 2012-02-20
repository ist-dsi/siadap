package module.siadap.presentationTier.actions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.contacts.domain.EmailAddress;
import module.contacts.ist.domain.ContactsIstSystem;
import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.activities.ChangePersonnelSituationActivityInformation;
import module.siadap.domain.CompetenceType;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.groups.SiadapStructureManagementGroup;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.activities.ActivityException;
import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.VirtualHost;
import myorg.domain.exceptions.DomainException;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.util.BundleUtil;
import myorg.util.VariantBean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.ist.fenixframework.plugins.remote.domain.exception.RemoteException;
import pt.utl.ist.fenix.tools.util.excel.Spreadsheet;
import pt.utl.ist.fenix.tools.util.excel.Spreadsheet.Row;

@Mapping(path = "/siadapPersonnelManagement")
public class SiadapPersonnelManagement extends ContextBaseAction {

    private static Logger logger = Logger.getLogger(SiadapPersonnelManagement.class.getName());

    public final ActionForward start(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
	if (siadapYearWrapper == null) {
	    ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
	    if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
		int year = new LocalDate().getYear();
		siadapYearWrapper = new SiadapYearWrapper(year);
	    } else {
		siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
	    }
	}
	request.setAttribute("siadapYearWrapper", siadapYearWrapper);
	VariantBean bean = new VariantBean();
	request.setAttribute("bean", bean);

	request.setAttribute("person", new PersonSiadapWrapper(UserView.getCurrentUser().getPerson(), new LocalDate().getYear()));
	return forward(request, "/module/siadap/management/start.jsp");
    }

    public final ActionForward createNewSiadapProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	SiadapCreationBean siadapCreationBean = getRenderedObject("createSiadapBean");
	Person evaluated = (Person) getDomainObject(request, "personId");

	try {
	    SiadapProcess.createNewProcess(evaluated, year, siadapCreationBean.getDefaultSiadapUniverse(),
		    siadapCreationBean.getCompetenceType());
	} catch (DomainException ex) {
	    addMessage(request, ex.getKey(), ex.getArgs());
	}

	return viewPerson(mapping, form, request, response);

    }

    private final ActionForward changePersonnelSituation(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response,
	    ActivityInformationBeanWrapper informationBeanWrapper) throws Exception {
	int year = Integer.parseInt(request.getParameter("year"));
	Person evaluated = (Person) getDomainObject(request, "personId");
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(evaluated, year);
	Siadap siadap = personSiadapWrapper.getSiadap();
	//let's get the activity and the AI
	WorkflowActivity<WorkflowProcess, ActivityInformation<WorkflowProcess>> activity = getActivity(siadap.getProcess(),
		request);
	ActivityInformation activityInformation = new ChangePersonnelSituationActivityInformation(siadap.getProcess(), activity,
		informationBeanWrapper);

	try {
	    if (!activityInformation.hasAllneededInfo()) {
		throw new SiadapException(((ChangePersonnelSituationActivityInformation) activityInformation).getBeanWrapper()
			.getClass().getName()
			+ ".needs.info");
	    }
	    activity.execute(activityInformation);

	} catch (DomainException ex) {
	    addLocalizedMessage(request, ex.getLocalizedMessage());
	} catch (ActivityException e) {
	    addLocalizedMessage(request, e.getMessage());
	}

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeCompetenceType(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	CompetenceTypeBean competenceTypeBean = getRenderedObject("changeCompetenceTypeBean");
	return changePersonnelSituation(mapping, form, request, response, competenceTypeBean);
    }

    private <T extends WorkflowProcess> WorkflowActivity<T, ActivityInformation<T>> getActivity(WorkflowProcess process,
	    HttpServletRequest request) {
	String activityName = request.getParameter("activity");
	return process.getActivity(activityName);
    }

    public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	VariantBean bean = getRenderedObject("searchPerson");
	Person person = (Person) ((bean != null) ? bean.getDomainObject() : getDomainObject(request, "personId"));

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);

	//checking for the existence of the e-mail addresses of the SiadapStructureManagementGroup users and let's warn if they don't exist
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	for (Person structureMngmntMember : configuration.getStructureManagementGroupMembers()) {
	    String emailAddress = null;
	    try {
		emailAddress = ContactsIstSystem.retrieveLatestEmailAddress(person);
	    } catch (NullPointerException ex) {
		ex.printStackTrace();
	    }
	    if (emailAddress == null || StringUtils.isBlank(emailAddress)) {
		addMessage(request, "WARNING", "manage.siadapStructure.person.has.no.valid.emailaddress",
			new String[] { structureMngmntMember.getName() });
	    }
	}

	request.setAttribute("person", personSiadapWrapper);
	request.setAttribute("bean", new VariantBean());
	request.setAttribute("changeWorkingUnit", new ChangeWorkingUnitBean());
	request.setAttribute("changeEvaluator", new ChangeEvaluatorBean());
	request.setAttribute("createSiadapBean", new SiadapCreationBean(personSiadapWrapper));
	request.setAttribute("changeSiadapUniverse", new ChangeSiadapUniverseBean(person, year));
	request.setAttribute("changeCompetenceTypeBean", new CompetenceTypeBean(personSiadapWrapper));
	request.setAttribute("history", personSiadapWrapper.getAccountabilitiesHistory());
	return forward(request, "/module/siadap/management/editPerson.jsp");
    }

    public final ActionForward terminateUnitHarmonization(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	LocalDate now = new LocalDate();
	int year = Integer.parseInt(request.getParameter("year"));
	Unit unit = getDomainObject(request, "unitId");
	Person person = getDomainObject(request, "personId");

	Set<AccountabilityType> accountabilityTypes = Collections.singleton(SiadapYearConfiguration.getSiadapYearConfiguration(
		year).getHarmonizationResponsibleRelation());
	Collection<Accountability> parentAccountabilities = person.getParentAccountabilities(accountabilityTypes);

	for (Accountability accountability : parentAccountabilities) {
	    if (accountability.getParent() == unit) {
		accountability.editDates(accountability.getBeginDate(), now);
	    }
	}
	//notify the users who have access to this interface
	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.subject", String.valueOf(year),
		person.getName(), unit.getPresentationName());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.content", person.getName(), person
			.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the user
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.terminateUnitHarmonization.subject", String.valueOf(year),
		unit.getPresentationName());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.terminateUnitHarmonization.content", String.valueOf(year),
		unit.getPresentationName(), unit.getAcronym());

	notifyUser(request, notificationSubject, notificationContent, person);

	return viewPerson(mapping, form, request, response);
    }

    private void notifySiadapStructureManagementUsers(final HttpServletRequest request, String subject, String content) {
	//get the SiadapStructureManagementUsers
	int year = Integer.parseInt(request.getParameter("year"));
	List<Person> persons = SiadapStructureManagementGroup.getListOfMembers(year);
	Person[] personArray = new Person[persons.size()];
	int i = 0;
	for (Person person : persons) {
	    personArray[i++] = person;
	}

	//notify them
	notifyUser(request, subject, content, personArray);
    }

    private void notifyUser(HttpServletRequest request, String notificationSubject, String notificationContent, Person... persons) {
	//get the user e-mail
	ArrayList<String> usersEmails = new ArrayList<String>();
	for (Person person : persons) {
	    try {
		String emailAddress = EmailAddress.getEmailForSendingEmails(person);
		if (StringUtils.isBlank(emailAddress)) {
		    String[] arguments = { person.getName() };
		    addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo", arguments);

		} else {
		    usersEmails.add(emailAddress);
		}
	    } catch (RemoteException ex) {
		String[] arguments = { person.getName() };
		addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo", arguments);
	    }
	}
	auxNotifyUser(usersEmails, notificationSubject, notificationContent);
    }

    //created because of the faulty dml injector
    @Service
    private void auxNotifyUser(ArrayList<String> usersEmails, String notificationSubject, String notificationContent) {
	final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
	new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(), new String[] {},
		usersEmails, Collections.EMPTY_LIST, Collections.EMPTY_LIST, notificationSubject, notificationContent);

    }

    public final ActionForward addHarmonizationUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));

	VariantBean bean = getRenderedObject("addHarmonizationUnit");
	Person person = getDomainObject(request, "personId");

	UnitSiadapWrapper unitWrapper = new UnitSiadapWrapper((Unit) bean.getDomainObject(), year);

	unitWrapper.addResponsibleForHarmonization(person);

	RenderUtils.invalidateViewState("addHarmonizationUnit");

	//notify the users who have access to this interface
	Unit unit = unitWrapper.getHarmonizationUnit();

	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.addHarmonizationUnit.subject", String.valueOf(year), person
			.getUser().getUsername(), unit.getAcronym());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.addHarmonizationUnit.content", person.getName(), person
			.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the user
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.addHarmonizationUnit.subject", String.valueOf(year),
		unit.getPresentationName());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.addHarmonizationUnit.content", String.valueOf(year),
		unit.getPresentationName(), unit.getAcronym());

	notifyUser(request, notificationSubject, notificationContent, person);

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeWorkingUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	ChangeWorkingUnitBean bean = getRenderedObject("changeWorkingUnit");

	return changePersonnelSituation(mapping, form, request, response, bean);
    }

    public final ActionForward changeEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
	ChangeEvaluatorBean changeEvaluatorBean = getRenderedObject("changeEvaluator");
	return changePersonnelSituation(mapping, form, request, response, changeEvaluatorBean);
    }

    public final ActionForward changeSiadapUniverse(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	ChangeSiadapUniverseBean changeUniverseBean = getRenderedObject("changeSiadapUniverse");
	return changePersonnelSituation(mapping, form, request, response, changeUniverseBean);
    }

    public final ActionForward removeCustomEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	return changePersonnelSituation(mapping, form, request, response, new RemoveCustomEvaluatorBean());
    }

    public final ActionForward removeSiadap(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	Person evaluated = (Person) getDomainObject(request, "personId");

	try {
	    new PersonSiadapWrapper(evaluated, year).removeSiadap();
	} catch (DomainException ex) {
	    addMessage(request, ex.getKey(), ex.getArgs());
	}

	return viewPerson(mapping, form, request, response);

    }

    public final ActionForward downloadNormalSIADAPStructure(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	int year = Integer.parseInt(((String) getAttribute(request, "year")));

	return streamSpreadsheet(response, "SIADAP_hierarquia_" + year,
		siadapRootModule.exportSIADAPHierarchy(year, false, true, false));
    }

    public final ActionForward downloadSIADAPRawData(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	int year = Integer.parseInt(((String) getAttribute(request, "year")));

	Spreadsheet siadapRawDataSpreadsheet = new Spreadsheet("SIADAP-" + year);

	siadapRawDataSpreadsheet.setHeader("istId avaliado");
	siadapRawDataSpreadsheet.setHeader("nome");
	siadapRawDataSpreadsheet.setHeader("istId avaliador");
	siadapRawDataSpreadsheet.setHeader("nome avaliador");
	siadapRawDataSpreadsheet.setHeader("unidade onde trabalha");
	siadapRawDataSpreadsheet.setHeader("unidade onde é harmonizado");
	siadapRawDataSpreadsheet.setHeader("categoria SIADAP");
	siadapRawDataSpreadsheet.setHeader("universo SIADAP");
	siadapRawDataSpreadsheet.setHeader("Conta para quotas IST");

	//let's get all of the SIADAPs
	SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	//	List<Siadap> siadaps = siadapYearConfiguration.getSiadaps();
	List<Siadap> siadaps = SiadapRootModule.getInstance().getSiadaps();
	for (Siadap siadap : siadaps) {
	    if (siadap.getYear().intValue() == year) {
		Row row = siadapRawDataSpreadsheet.addRow();
		row.setCell(siadap.getEvaluated().getUser().getUsername());
		row.setCell(siadap.getEvaluated().getPresentationName());
		row.setCell(siadap.getEvaluator().getPerson().getUser().getUsername());
		row.setCell(siadap.getEvaluator().getPerson().getPresentationName());
		PersonSiadapWrapper evaluatedWrapper = new PersonSiadapWrapper(siadap.getEvaluated(), year);
		row.setCell(evaluatedWrapper.getWorkingUnit().getUnit().getPresentationName());
		row.setCell(evaluatedWrapper.getSiadap() == null
			|| evaluatedWrapper.getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse()) == null ? "-"
			: evaluatedWrapper.getUnitWhereIsHarmonized(evaluatedWrapper.getSiadap().getDefaultSiadapUniverse())
				.getPresentationName());
		row.setCell(String.valueOf(siadap.getDefaultSiadapUniverse()));
		row.setCell(evaluatedWrapper.getCareerName());
		row.setCell(evaluatedWrapper.isQuotaAware() ? "Sim" : "Não");
	    }
	}

	return streamSpreadsheet(response, "SIADAP-" + year, siadapRawDataSpreadsheet);
    }

    public final ActionForward downloadSIADAPStructureWithUniverse(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	int year = Integer.parseInt(((String) getAttribute(request, "year")));

	return streamSpreadsheet(response, "SIADAP_hierarquia_" + year,
		siadapRootModule.exportSIADAPHierarchy(year, false, true, true));

    }

    private ActionForward streamSpreadsheet(final HttpServletResponse response, final String fileName,
	    final Spreadsheet resultSheet) throws IOException {
	response.setContentType("application/xls ");
	response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

	ServletOutputStream outputStream = response.getOutputStream();
	resultSheet.exportToXLSSheet(outputStream);
	outputStream.flush();
	outputStream.close();

	return null;
    }

    private ActionForward streamSpreadsheet(final HttpServletResponse response, final String fileName,
	    final HSSFWorkbook resultSheet) throws IOException {

	response.setContentType("application/xls ");
	response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

	ServletOutputStream outputStream = response.getOutputStream();

	resultSheet.write(outputStream);
	outputStream.flush();
	outputStream.close();

	return null;
    }

    public static class RemoveCustomEvaluatorBean extends ActivityInformationBeanWrapper implements Serializable {

	@Override
	public boolean hasAllNeededInfo() {
	    return true;
	}

	@Override
	public void execute(SiadapProcess process) throws SiadapException {
	    new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap().getYear()).removeCustomEvaluator();

	}

	@Override
	public String[] getArgumentsDescription(SiadapProcess process) {
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		    RemoveCustomEvaluatorBean.class.getSimpleName(), process.getSiadap().getEvaluator().getPerson()
			    .getPresentationName()) };
	}

    }

    public static class ChangeSiadapUniverseBean extends ActivityInformationBeanWrapper implements Serializable {
	private SiadapUniverse siadapUniverse;

	private LocalDate dateOfChange;

	ChangeSiadapUniverseBean(Person person, int year) {
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	    Siadap siadapFor = (siadapYearConfiguration == null) ? null : siadapYearConfiguration.getSiadapFor(person);
	    if (siadapFor == null)
		this.setSiadapUniverse(null);
	    else
		this.setSiadapUniverse(siadapFor.getDefaultSiadapUniverse());
	}

	public SiadapUniverse getSiadapUniverse() {
	    return siadapUniverse;
	}

	public void setSiadapUniverse(SiadapUniverse siadapUniverse) {
	    this.siadapUniverse = siadapUniverse;
	}

	@Override
	public boolean hasAllNeededInfo() {
	    return siadapUniverse != null && dateOfChange != null;
	}

	public LocalDate getDateOfChange() {
	    return dateOfChange;
	}

	public void setDateOfChange(LocalDate dateOfChange) {
	    this.dateOfChange = dateOfChange;
	}

	@Override
	public void execute(SiadapProcess process) throws SiadapException {
	    Siadap siadap = process.getSiadap();
	    new PersonSiadapWrapper(siadap.getEvaluated(), siadap.getYear()).changeDefaultUniverseTo(getSiadapUniverse(),
		    getDateOfChange());

	}

	@Override
	public String[] getArgumentsDescription(SiadapProcess process) {
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		    ChangeSiadapUniverseBean.class.getSimpleName(), getSiadapUniverse().getLocalizedName(), getDateOfChange()
			    .toString()) };
	}

    }

    public static class SiadapCreationBean implements Serializable {
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	private SiadapUniverse defaultSiadapUniverse;
	private CompetenceType competenceType;

	public SiadapCreationBean(PersonSiadapWrapper personWrapper) {
	    setDefaultSiadapUniverse(personWrapper.getDefaultSiadapUniverse());
	    setCompetenceType(personWrapper.getDefaultCompetenceTypeObject());
	}

	public CompetenceType getCompetenceType() {
	    return competenceType;
	}

	public void setCompetenceType(CompetenceType competenceType) {
	    this.competenceType = competenceType;
	}

	public SiadapUniverse getDefaultSiadapUniverse() {
	    return defaultSiadapUniverse;
	}

	public void setDefaultSiadapUniverse(SiadapUniverse defaultSiadapUniverse) {
	    this.defaultSiadapUniverse = defaultSiadapUniverse;
	}

    }

    public static class ChangeEvaluatorBean extends ActivityInformationBeanWrapper implements Serializable {
	private Person evaluator;
	private LocalDate dateOfChange;

	public ChangeEvaluatorBean() {
	    this.dateOfChange = new LocalDate();
	}

	public void setEvaluator(Person person) {
	    this.evaluator = person;
	}

	public Person getEvaluator() {
	    return evaluator;
	}

	public void setDateOfChange(LocalDate dateOfChange) {
	    this.dateOfChange = dateOfChange;
	}

	public LocalDate getDateOfChange() {
	    return dateOfChange;
	}

	@Override
	public boolean hasAllNeededInfo() {
	    return evaluator != null && dateOfChange != null;
	}

	@Override
	public void execute(SiadapProcess process) throws SiadapException {
	    Siadap siadap = process.getSiadap();
	    if (siadap.isDefaultEvaluationDone())
		throw new SiadapException("error.cannot.change.evaluator.evaluation.already.done");
	    new PersonSiadapWrapper(siadap.getEvaluated(), siadap.getYear()).changeEvaluatorTo(getEvaluator(), getDateOfChange());

	}

	@Override
	public String[] getArgumentsDescription(SiadapProcess process) {
	    return new String[] { BundleUtil
		    .getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, ChangeEvaluatorBean.class.getSimpleName(),
			    getEvaluator().getPresentationName(), getDateOfChange().toString()) };
	}
    }

    public static class CompetenceTypeBean extends ActivityInformationBeanWrapper implements Serializable {
	private CompetenceType competenceType;

	public CompetenceTypeBean(PersonSiadapWrapper personSiadapWrapper) {
	    this.competenceType = personSiadapWrapper.getDefaultCompetenceTypeObject();
	}

	public CompetenceType getCompetenceType() {
	    return competenceType;
	}

	public void setCompetenceType(CompetenceType competenceType) {
	    this.competenceType = competenceType;
	}

	@Override
	public boolean hasAllNeededInfo() {
	    return competenceType != null;
	}

	@Override
	public void execute(SiadapProcess process) throws SiadapException {
	    if (process.getSiadap().getCompetences() != null && process.getSiadap().getCompetences().isEmpty() == false)
		throw new SiadapException("error.changing.competence.type.cant.due.to.existing.competences.defined");
	    process.getSiadap().getDefaultSiadapEvaluationUniverse().setCompetenceSlashCareerType(getCompetenceType());
	}

	@Override
	public String[] getArgumentsDescription(SiadapProcess process) {
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		    CompetenceTypeBean.class.getSimpleName(), competenceType.getName()) };
	}
    }

    public static class ChangeWorkingUnitBean extends ActivityInformationBeanWrapper implements Serializable {

	private Boolean withQuotas;
	private Unit unit;
	private LocalDate dateOfChange;

	public ChangeWorkingUnitBean() {
	    this.dateOfChange = new LocalDate();
	}

	public Unit getUnit() {
	    return unit;
	}

	public void setUnit(Unit unit) {
	    this.unit = unit;
	}

	public void setWithQuotas(Boolean withQuotas) {
	    this.withQuotas = withQuotas;
	}

	public Boolean getWithQuotas() {
	    return withQuotas;
	}

	public void setDateOfChange(LocalDate dateOfChange) {
	    this.dateOfChange = dateOfChange;
	}

	public LocalDate getDateOfChange() {
	    return dateOfChange;
	}

	@Override
	public boolean hasAllNeededInfo() {
	    return (getUnit() != null && getWithQuotas() != null && getDateOfChange() != null);
	}

	@Override
	public void execute(SiadapProcess process) throws SiadapException {
	    new PersonSiadapWrapper(process.getSiadap().getEvaluated(), process.getSiadap().getYear()).changeWorkingUnitTo(
		    getUnit(), getWithQuotas(), getDateOfChange());

	}

	@Override
	public String[] getArgumentsDescription(SiadapProcess process) {
	    String countsForInstitutionalQuotas = (withQuotas) ? BundleUtil.getFormattedStringFromResourceBundle(
		    Siadap.SIADAP_BUNDLE_STRING, "siadap.true.yes") : BundleUtil.getFormattedStringFromResourceBundle(
		    Siadap.SIADAP_BUNDLE_STRING, "siadap.false.no");
	    return new String[] { BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		    ChangeWorkingUnitBean.class.getSimpleName(), unit.getPartyName().getContent(), String.valueOf(withQuotas),
		    dateOfChange.toString()) };
	}

    }

    public static abstract class ActivityInformationBeanWrapper {

	public abstract boolean hasAllNeededInfo();

	/**
	 * Executes the change
	 * 
	 * @throws SiadapException
	 *             if some kind of error was found
	 */
	public abstract void execute(SiadapProcess process) throws SiadapException;

	/**
	 * 
	 * @return an array of strings with the arguments description
	 */
	public abstract String[] getArgumentsDescription(SiadapProcess process);

    }

}
