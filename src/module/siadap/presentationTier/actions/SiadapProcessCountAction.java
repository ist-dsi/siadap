/*
 * @(#)SiadapProcessCountAction.java
 *
 * Copyright 2011 Instituto Superior Tecnico
 * Founding Authors: Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the SIADAP Module.
 *
 *   The SIADAP Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The SIADAP Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the SIADAP Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.OrganizationalModel;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.presentationTier.actions.OrganizationModelAction;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.SiadapProcessStateEnumWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapStateToShowInCount;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import myorg.domain.MyOrg;
import myorg.presentationTier.actions.ContextBaseAction;
import myorg.presentationTier.component.OrganizationChart;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/siadapProcessCount")
/**
 * 
 * @author João Antunes
 * @author Luis Cruz
 * 
 */
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
	SiadapYearConfiguration configuration = siadapYearWrapper.getSiadapYearConfiguration();

	if (configuration == null) {
	    return forward(request, "/module/siadap/unit.jsp");
	}
	request.setAttribute("configuration", configuration);

	SiadapProcessStateEnumWrapper siadapProcessStateToFilter = (SiadapProcessStateEnumWrapper) getRenderedObject("siadapProcessStateToFilter");
	if (siadapProcessStateToFilter == null) {
	    String enumAsParam = request.getParameter("siadapProcessStateEnumToFilterOrdinal");
	    SiadapProcessStateEnum enumToUse = SiadapStateToShowInCount.getDefaultStateToFilter();
	    if (!StringUtils.isBlank(enumAsParam)) {
		enumToUse = SiadapProcessStateEnum.valueOf(enumAsParam);
	    }
	    siadapProcessStateToFilter = new SiadapProcessStateEnumWrapper(enumToUse);
	}

	request.setAttribute("siadapProcessStateToFilter", siadapProcessStateToFilter);

	//let's always use the last day of the year
	LocalDate dayToUse = SiadapMiscUtilClass.lastDayOfYearWhereAccsAreActive(configuration.getYear());

	final Unit unit = configuration.getSiadapStructureTopUnit();

	final Collection<Party> parents = getParents(unit, configuration, dayToUse);
	final Collection<Party> children = getChildren(unit, configuration, dayToUse);

	OrganizationChart<Party> chart = new OrganizationChart<Party>(unit, parents, children, 3);
	request.setAttribute("chart", chart);

	final Collection<Accountability> workerAccountabilities = getChildrenWorkers(unit, configuration, dayToUse);
	request.setAttribute("workerAccountabilities", workerAccountabilities);

	final Person unitResponsible = findUnitChild(unit, dayToUse, configuration.getEvaluationRelation(),
		configuration.getUnitRelations());
	request.setAttribute("unitResponsible", unitResponsible);

	final Person unitHarmanizer = findUnitChild(unit, dayToUse, configuration.getHarmonizationResponsibleRelation(),
		configuration.getUnitRelations());
	request.setAttribute("unitHarmanizer", unitHarmanizer);

	return forward(request, "/module/siadap/unit.jsp");
    }

    public ActionForward showSummaryTables(ActionMapping mapping, ActionForm form, HttpServletRequest request,
	    HttpServletResponse response) throws Exception {

	//	final OrganizationalModel organizationalModel = findOrgModel();
	//	//get the top unit (IST)
	//	final Unit unit = getUnit(organizationalModel, request);
	//
	//	request.setAttribute("unit", unit);
	//
	return forward(request, "/module/siadap/summaryBoard.jsp");

    }

    private Person findUnitChild(final Unit unit, final LocalDate dayToUse,
	    final AccountabilityType accountabilityType, final AccountabilityType unitAccountabilityType) {
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (isActive(accountability, dayToUse, accountabilityType)) {
		return (Person) accountability.getChild();
	    }
	}
	final Unit parent = findUnitParent(unit, dayToUse, unitAccountabilityType);
	return parent == null ? null : findUnitChild(parent, dayToUse, accountabilityType, unitAccountabilityType);
    }

    private Unit findUnitParent(final Unit unit, final LocalDate dayToUse, final AccountabilityType accountabilityType) {
	for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
	    if (isActive(accountability, dayToUse, accountabilityType)) {
		return (Unit) accountability.getParent();
	    }
	}
	return null;
    }

    public Collection<Party> getParents(final Unit unit, final SiadapYearConfiguration configuration, final LocalDate dayToUse) {
	final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
	for (final Accountability accountability : unit.getParentAccountabilitiesSet()) {
	    if (isActiveUnit(accountability, configuration, dayToUse)) {
		result.add(accountability.getParent());
	    }
	}
	return result;
    }

    public Collection<Party> getChildren(final Unit unit, final SiadapYearConfiguration configuration, final LocalDate dayToUse) {
	final SortedSet<Party> result = new TreeSet<Party>(Party.COMPARATOR_BY_NAME);
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (isActiveUnit(accountability, configuration, dayToUse)
		    && hasSomeWorker((Unit) accountability.getChild(), configuration, dayToUse)) {
		result.add(accountability.getChild());
	    }
	}
	return result;
    }

    private boolean hasSomeWorker(final Unit unit, final SiadapYearConfiguration configuration, final LocalDate dayToUse) {
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (isActiveWorker(accountability, configuration, dayToUse)
		    || (isActiveUnit(accountability, configuration, dayToUse) && hasSomeWorker((Unit) accountability.getChild(),
			    configuration, dayToUse))) {
		return true;
	    }
	}
	return false;
    }

    public Collection<Accountability> getChildrenWorkers(final Unit unit, final SiadapYearConfiguration configuration,
	    final LocalDate dayToUse) {
	final SortedSet<Accountability> result = new TreeSet<Accountability>(Accountability.COMPARATOR_BY_CHILD_PARTY_NAMES);
	for (final Accountability accountability : unit.getChildAccountabilitiesSet()) {
	    if (isActiveWorker(accountability, configuration, dayToUse)) {
		result.add(accountability);
	    }
	}
	return result;
    }

    private boolean isActiveUnit(Accountability accountability, SiadapYearConfiguration configuration, LocalDate dayToUse) {
	return isActive(accountability, dayToUse, configuration.getUnitRelations());
    }

    private boolean isActiveWorker(Accountability accountability, SiadapYearConfiguration configuration, LocalDate dayToUse) {
	return isActive(accountability, dayToUse, configuration.getWorkingRelation(),
		configuration.getWorkingRelationWithNoQuota());
    }

    private boolean isActive(final Accountability accountability, final LocalDate dayToUse,
	    final AccountabilityType... accountabilityTypes) {
	final AccountabilityType accountabilityType = accountability.getAccountabilityType();
	if (accountability.isActive(dayToUse)) {
	    for (final AccountabilityType type : accountabilityTypes) {
		if (type == accountabilityType) {
		    return true;
		}
	    }
	}
	return false;
    }

    //    private Unit getUnit(final OrganizationalModel organizationalModel, final HttpServletRequest request) {
    //	final Unit unit = getDomainObject(request, "unitId");
    //	return unit == null
    //		? (organizationalModel.hasAnyParties()
    //			? (Unit) organizationalModel.getPartiesIterator().next()
    //			: null)
    //		: unit;
    //    }

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
