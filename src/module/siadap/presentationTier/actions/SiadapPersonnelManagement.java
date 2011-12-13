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
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapUniverse;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.groups.SiadapStructureManagementGroup;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;
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

@Mapping(path = "/siadapPersonnelManagement")
public class SiadapPersonnelManagement extends ContextBaseAction {

    private static Logger logger = Logger.getLogger(SiadapPersonnelManagement.class.getName());

    private static final String SIADAP_BUNDLE_STRING = "resources/SiadapResources";

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

    public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {

	VariantBean bean = getRenderedObject("searchPerson");
	Person person = (Person) ((bean != null) ? bean.getDomainObject() : getDomainObject(request, "personId"));

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(person, year);

	//checking for the existence of the e-mail addresses of the SiadapStructureManagementGroup users and let's warn if they don't exist
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	for (Person structureMngmntMember : configuration.getStructureManagementGroupMembers()) {
	    String emailAddress = ContactsIstSystem.retrieveLatestEmailAddress(person);
	    if (emailAddress == null || StringUtils.isBlank(emailAddress)) {
		addMessage(request, "WARNING", "manage.siadapStructure.person.has.no.valid.emailaddress",
			new String[] { structureMngmntMember.getName() });
	    }
	}

	request.setAttribute("person", personSiadapWrapper);
	request.setAttribute("bean", new VariantBean());
	request.setAttribute("changeWorkingUnit", new ChangeWorkingUnitBean(person, year));
	request.setAttribute("changeEvaluator", new ChangeEvaluatorBean());
	request.setAttribute("changeSiadapUniverse", new ChangeSiadapUniverseBean(person, year));
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
	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.subject", String.valueOf(year),
		person.getName(), unit.getPresentationName());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.content", person.getName(), person
			.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the user
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.terminateUnitHarmonization.subject", String.valueOf(year),
		unit.getPresentationName());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
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
	new Email(virtualHost.getApplicationSubTitle().getContent(),
		    virtualHost.getSystemEmailAddress(), new String[] {}, usersEmails,
		Collections.EMPTY_LIST, Collections.EMPTY_LIST, notificationSubject, notificationContent);

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

	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.addHarmonizationUnit.subject", String.valueOf(year), person
			.getUser().getUsername(), unit.getAcronym());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.addHarmonizationUnit.content", person.getName(), person
			.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the user
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.addHarmonizationUnit.subject", String.valueOf(year),
		unit.getPresentationName());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.addHarmonizationUnit.content", String.valueOf(year),
		unit.getPresentationName(), unit.getAcronym());

	notifyUser(request, notificationSubject, notificationContent, person);

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeWorkingUnit(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	ChangeWorkingUnitBean bean = getRenderedObject("changeWorkingUnit");

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper(bean.getPerson(), year);
	Unit unit = null;
	Boolean quotasApply = null;
	LocalDate dateOfChange = null;
	try {
	    unit = bean.getUnit();
	    quotasApply = bean.getWithQuotas();
	    dateOfChange = bean.getDateOfChange();
	    personWrapper.changeWorkingUnitTo(bean.getUnit(), bean.getWithQuotas(), bean.getDateOfChange());
	} catch (DomainException e) {
	    addMessage(request, e.getKey(), e.getArgs());
	}

	Person person = personWrapper.getPerson();

	String quotasString = null;
	if (quotasApply) {
	    quotasString = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.label.quotasApply");
	} else {
	    quotasString = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.label.quotasDontApply");

	}

	//notify the users who have access to this interface
	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.changeWorkingUnit.subject", String.valueOf(year), person
			.getUser().getUsername(), unit.getAcronym());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.changeWorkingUnit.content", person.getName(), person
			.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym(), String.valueOf(year),
		dateOfChange.toString(), quotasString);

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the user
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeWorkingUnit.subject", String.valueOf(year),
		unit.getPresentationName());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeWorkingUnit.content", String.valueOf(year),
		unit.getPresentationName(), unit.getAcronym(), dateOfChange.toString(), quotasString);

	notifyUser(request, notificationSubject, notificationContent, person);

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	ChangeEvaluatorBean changeEvaluatorBean = getRenderedObject("changeEvaluator");
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);
	LocalDate dateOfChange = null;

	try {
	    dateOfChange = changeEvaluatorBean.getDateOfChange();
	    personWrapper.changeEvaluatorTo(changeEvaluatorBean.getEvaluator(), dateOfChange);
	} catch (DomainException e) {
	    addMessage(request, e.getKey(), e.getArgs());
	}
	Person evaluated = personWrapper.getPerson();
	User evaluatedUser = evaluated.getUser();
	Person evaluator = personWrapper.getEvaluator().getPerson();
	User evaluatorUser = evaluator.getUser();
	//notify the users who have access to this interface
	String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.changeEvaluator.subject", String.valueOf(year),
		evaluated.getName(), evaluatedUser.getUsername(), evaluator.getName(), evaluatorUser.getUsername());
	String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.managers.changeEvaluator.content", String.valueOf(year),
		evaluated.getName(), evaluatedUser.getUsername(), evaluator.getName(), evaluatorUser.getUsername(),
		dateOfChange.toString());

	notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	//notify the direct intervenients
	//notifying the new evaluator
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeEvaluator.evaluator.subject", String.valueOf(year),
		evaluated.getName(), evaluatedUser.getUsername());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeEvaluator.evaluator.content", String.valueOf(year),
		evaluated.getName(), evaluatedUser.getUsername(), dateOfChange.toString());

	notifyUser(request, notificationSubject, notificationContent, evaluator);

	//notifying the evaluated
	notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeEvaluator.evaluated.subject", String.valueOf(year),
		evaluator.getName(), evaluatorUser.getUsername());

	notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		"manage.siadapStructure.notification.email.person.changeEvaluator.evaluated.content", String.valueOf(year),
		evaluator.getName(), evaluatorUser.getUsername(), dateOfChange.toString());
	notifyUser(request, notificationSubject, notificationContent, evaluated);

	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward changeSiadapUniverse(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	ChangeSiadapUniverseBean changeUniverseBean = getRenderedObject("changeSiadapUniverse");
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);
	try {
	    SiadapUniverse siadapUniverseToChangeTo = changeUniverseBean.getSiadapUniverse();
	    personWrapper.changeUniverseTo(siadapUniverseToChangeTo);
	} catch (DomainException e) {
	    addMessage(request, e.getKey(), e.getArgs());
	}
	return viewPerson(mapping, form, request, response);
    }

    public final ActionForward removeCustomEvaluator(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	int year = Integer.parseInt(request.getParameter("year"));
	PersonSiadapWrapper personWrapper = new PersonSiadapWrapper((Person) getDomainObject(request, "personId"), year);
	//let's retrieve the important fields to assert if anything changed at all
	Person oldEvaluator = personWrapper.getEvaluator().getPerson();

	personWrapper.removeCustomEvaluator();

	Person newEvaluator = personWrapper.getEvaluator().getPerson();

	if (!oldEvaluator.equals(newEvaluator)) {
	    Person evaluated = personWrapper.getPerson();
	    User evaluatedUser = evaluated.getUser();
	    Person evaluator = personWrapper.getEvaluator().getPerson();
	    User evaluatorUser = evaluator.getUser();
	    LocalDate dateOfChange = new LocalDate();

	    //notify the users who have access to this interface
	    String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.managers.changeEvaluator.subject", String.valueOf(year),
		    evaluated.getName(), evaluatedUser.getUsername(), evaluator.getName(), evaluatorUser.getUsername());
	    String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.managers.changeEvaluator.content", String.valueOf(year),
		    evaluated.getName(), evaluatedUser.getUsername(), evaluator.getName(), evaluatorUser.getUsername(),
		    dateOfChange.toString());

	    notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

	    //notify the direct intervenients
	    //notifying the new evaluator
	    notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.person.changeEvaluator.evaluator.subject", String.valueOf(year),
		    evaluated.getName(), evaluatedUser.getUsername());

	    notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.person.changeEvaluator.evaluator.content", String.valueOf(year),
		    evaluated.getName(), evaluatedUser.getUsername(), dateOfChange.toString());

	    notifyUser(request, notificationSubject, notificationContent, evaluator);

	    //notifying the evaluated
	    notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.person.changeEvaluator.evaluated.subject", String.valueOf(year),
		    evaluator.getName(), evaluatorUser.getUsername());

	    notificationContent = BundleUtil.getFormattedStringFromResourceBundle(SIADAP_BUNDLE_STRING,
		    "manage.siadapStructure.notification.email.person.changeEvaluator.evaluated.content", String.valueOf(year),
		    evaluator.getName(), evaluatorUser.getUsername(), dateOfChange.toString());
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

    public final ActionForward downloadSIADAPStructureWithUniverse(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {

	SiadapRootModule siadapRootModule = SiadapRootModule.getInstance();
	int year = Integer.parseInt(((String) getAttribute(request, "year")));

	return streamSpreadsheet(response, "SIADAP_hierarquia_" + year,
		siadapRootModule.exportSIADAPHierarchy(year, false, true, true));


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

    public static class ChangeSiadapUniverseBean implements Serializable {
	final Person person;
	final int year;

	private SiadapUniverse siadapUniverse;

	ChangeSiadapUniverseBean(Person person, int year) {
	    this.person = person;
	    this.year = year;
	    SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
	    Siadap siadapFor = siadapYearConfiguration.getSiadapFor(person);
	    this.setSiadapUniverse(siadapFor.getDefaultSiadapUniverse());
	}

	public SiadapUniverse getSiadapUniverse() {
	    return siadapUniverse;
	}

	public void setSiadapUniverse(SiadapUniverse siadapUniverse) {
	    this.siadapUniverse = siadapUniverse;
	}
    }

    public static class ChangeEvaluatorBean implements Serializable {
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
    }

    public static class ChangeWorkingUnitBean implements Serializable {

	private Person person;
	private Boolean withQuotas;
	private int year;
	private Unit unit;
	private LocalDate dateOfChange;

	public ChangeWorkingUnitBean(Person person, int year) {
	    this.person = person;
	    this.year = year;
	    this.dateOfChange = new LocalDate();
	}

	public Person getPerson() {
	    return person;
	}

	public void setPerson(Person person) {
	    this.person = person;
	}

	public Unit getUnit() {
	    return unit;
	}

	public void setUnit(Unit unit) {
	    this.unit = unit;
	}

	public int getYear() {
	    return year;
	}

	public void setYear(int year) {
	    this.year = year;
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

    }
}
