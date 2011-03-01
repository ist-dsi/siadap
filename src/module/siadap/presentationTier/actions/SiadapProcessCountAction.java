package module.siadap.presentationTier.actions;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.Party;
import module.organization.domain.Unit;
import module.organization.presentationTier.actions.OrganizationModelAction;
import module.siadap.domain.SiadapYearConfiguration;
import myorg.domain.MyOrg;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.presentationTier.component.OrganizationChart;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapProcessCount")
public class SiadapProcessCountAction extends ContextBaseAction {

    @Override
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {
	final ActionForward forward = super.execute(mapping, form, request, response);
	OrganizationModelAction.addHeadToLayoutContext(request);
	return forward;
    }

    public ActionForward showUnit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	final LocalDate today = new LocalDate();
	final SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	request.setAttribute("configuration", configuration);

	final OrganizationalModel organizationalModel = findOrgModel();
	final Unit unit = getUnit(organizationalModel, request);

	final Collection<Party> parents = getParents(unit, configuration, today);
	final Collection<Party> children = getChildren(unit, configuration, today);

	OrganizationChart<Party> chart = new OrganizationChart<Party>(unit, parents, children, 3);
	request.setAttribute("chart", chart);

	return forward(request, "/module/siadap/unit.jsp");
    }

    public Collection<Party> getParents(final Unit unit, final SiadapYearConfiguration configuration, final LocalDate today) {
	final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
	for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
	    if (isActive(accountability, configuration, today)) {
		result.add(accountability.getParent());
	    }
	}
	return result;
    }

    public Collection<Party> getChildren(final Unit unit, final SiadapYearConfiguration configuration, final LocalDate today) {
	final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (isActive(accountability, configuration, today)) {
		result.add(accountability.getChild());
	    }
	}
	return result;
    }

    private boolean isActive(Accountability accountability, SiadapYearConfiguration configuration, LocalDate today) {
	final AccountabilityType accountabilityType = accountability.getAccountabilityType();
	if (accountabilityType == configuration.getUnitRelations()
		|| accountabilityType == configuration.getWorkingRelation()
		|| accountabilityType == configuration.getWorkingRelationWithNoQuota()) {
	    if (accountability.isActive(today)) {
		return true;
	    }
	}
	return false;
    }

    private Unit getUnit(final OrganizationalModel organizationalModel, final HttpServletRequest request) {
	final Unit unit = getDomainObject(request, "unitId");
	return unit == null
		? (organizationalModel.hasAnyParties()
			? (Unit) organizationalModel.getPartiesIterator().next()
			: null)
		: unit;
    }

    private OrganizationalModel findOrgModel() {
	final MyOrg instance = MyOrg.getInstance();
	for (final OrganizationalModel organizationalModel : instance.getOrganizationalModelsSet()) {
	    if (organizationalModel.getName().getContent().equals("SIADAP")) {
		return organizationalModel;
	    }
	}
	return null;
    }

}
