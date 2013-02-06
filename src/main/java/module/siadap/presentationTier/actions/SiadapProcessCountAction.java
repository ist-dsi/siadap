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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcess;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.util.SiadapProcessCounter;
import module.siadap.domain.wrappers.SiadapProcessStateEnumWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapStateToShowInCount;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;
import module.workflow.domain.ProcessFile;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.bennu.core.presentationTier.component.OrganizationChart;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

import com.google.common.collect.ArrayListMultimap;

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

    public ActionForward listSiadapsByStateAndYear(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        SiadapProcessStateEnum state = SiadapProcessStateEnum.valueOf(request.getParameter("state"));
        Integer year = Integer.valueOf(request.getParameter("year"));

        if (state == null || year == null)
            return null;

        ArrayList<Siadap> siadapsOfYearAndState = new ArrayList<Siadap>();
        for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
            if (siadap.getYear().equals(year) && siadap.getState().equals(state)) {
                siadapsOfYearAndState.add(siadap);
            }

        }

        java.util.Collections
                .sort(siadapsOfYearAndState, Siadap.COMPARATOR_BY_EVALUATED_PRESENTATION_NAME_FALLBACK_YEAR_THEN_OID);

        request.setAttribute("renderRemoveLink", Boolean.FALSE);
        request.setAttribute("siadaps", siadapsOfYearAndState);

        return forward(request, "/module/siadap/listGivenCollectionOfSiadaps.jsp");
    }

    public ActionForward removeSiadap(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Siadap siadap = getDomainObject(request, "siadapId");
        Integer year = siadap.getYear();

        deleteSiadapEvenWithFiles(siadap);

        return manageDuplicateSiadaps(request, year);

    }

    @Service
    private void deleteSiadapEvenWithFiles(Siadap siadap) {
        SiadapProcess process = siadap.getProcess();
        for (ProcessFile file : process.getFiles()) {
            process.removeFiles(file);
        }
        for (ProcessFile file : process.getDeletedFiles()) {
            process.removeDeletedFiles(file);
        }
        siadap.delete(true);
    }

    private ActionForward manageDuplicateSiadaps(HttpServletRequest request, Integer year) throws Exception {
        RenderUtils.invalidateViewState();
        //let's make a list of duplicates
        ArrayListMultimap<Person, Siadap> duplicateList = ArrayListMultimap.create();
        for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
            if (siadap.getYear().equals(year)) {
                //and in the duplicateList
                duplicateList.get(siadap.getEvaluated()).add(siadap);
            }
        }

        Set<Siadap> siadaps = new TreeSet<Siadap>(Siadap.COMPARATOR_BY_EVALUATED_PRESENTATION_NAME_FALLBACK_YEAR_THEN_OID);

        for (Person person : duplicateList.keys()) {
            List<Siadap> list = duplicateList.get(person);
            if (list.size() > 1) {
                siadaps.addAll(list);
            }
        }

        request.setAttribute("siadaps", siadaps);
        request.setAttribute("renderRemoveLink", Boolean.TRUE);
        return forward(request, "/module/siadap/listGivenCollectionOfSiadaps.jsp");
    }

    public ActionForward manageDuplicateSiadaps(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Integer year = Integer.valueOf(request.getParameter("year"));

        return manageDuplicateSiadaps(request, year);

    }

    public ActionForward manageUnlistedUsers(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Integer year = Integer.valueOf(request.getParameter("year"));
        //
        SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        //
        //let's get all of the listed ones
        //
        SiadapProcessCounter siadapProcessCounter =
                new SiadapProcessCounter(configuration.getSiadapStructureTopUnit(), false, configuration, true);
        //

        request.setAttribute("siadaps", siadapProcessCounter.getOrCreateSiadapsNotListed());
        request.setAttribute("renderRemoveLink", Boolean.FALSE);
        request.setAttribute("renderManageSiadapPersonLink", Boolean.TRUE);
        return forward(request, "/module/siadap/listGivenCollectionOfSiadaps.jsp");

        //	TypeOfList siadapsNotListedInStatistics = ListSiadapsComponent.TypeOfList.SIADAPS_NOT_LISTED_IN_STATISTICS;

        //	return forward(
        //		request,
        //		"/vaadinContext.do?method=forwardToVaadin#"
        //			+ new FragmentQuery(ListSiadapsComponent.class, year.toString(), siadapsNotListedInStatistics.name())
        //				.getQueryString());

    }

    public ActionForward showUnknownCategoryUsers(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String categoryString = request.getParameter("categoryString");
        Boolean quotaAware = Boolean.valueOf(request.getParameter("quota"));
        Integer year = Integer.valueOf(request.getParameter("year"));
        //
        SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        //
        //let's get all of the listed ones
        //
        SiadapProcessCounter siadapProcessCounter =
                new SiadapProcessCounter(configuration.getSiadapStructureTopUnit(), true, configuration, true);
        //

        request.setAttribute("siadaps", siadapProcessCounter.getCountsByQuotaAndCategories().get(quotaAware).get(categoryString)
                .getSiadapsForThisCategory());
        request.setAttribute("renderRemoveLink", Boolean.FALSE);
        request.setAttribute("renderManageSiadapPersonLink", Boolean.FALSE);
        return forward(request, "/module/siadap/listGivenCollectionOfSiadaps.jsp");

        //	TypeOfList siadapsNotListedInStatistics = ListSiadapsComponent.TypeOfList.SIADAPS_NOT_LISTED_IN_STATISTICS;

        //	return forward(
        //		request,
        //		"/vaadinContext.do?method=forwardToVaadin#"
        //			+ new FragmentQuery(ListSiadapsComponent.class, year.toString(), siadapsNotListedInStatistics.name())
        //				.getQueryString());

    }

    public ActionForward manageDuplicatePersons(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Integer year = Integer.valueOf(request.getParameter("year"));

        //let's get the siadaps for the duplicate persons
        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);
        SiadapProcessCounter processCounter =
                new SiadapProcessCounter(siadapYearConfiguration.getSiadapStructureTopUnit(), false, siadapYearConfiguration);
        Set<Person> duplicatePersons = processCounter.getDuplicatePersons();
        ArrayList<Siadap> siadaps = new ArrayList<Siadap>();
        for (Person person : duplicatePersons) {
            for (Siadap siadap : person.getSiadapsAsEvaluated()) {
                if (siadap.getYear().equals(year)) {
                    siadaps.add(siadap);
                }
            }
        }

        request.setAttribute("siadaps", siadaps);
        request.setAttribute("renderRemoveLink", Boolean.TRUE);
        request.setAttribute("renderManageSiadapPersonLink", Boolean.TRUE);
        return forward(request, "/module/siadap/listGivenCollectionOfSiadaps.jsp");

    }

    public ActionForward showUnitAccDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Integer year = Integer.valueOf(request.getParameter("year"));

        SiadapYearConfiguration siadapYearConfiguration = SiadapYearConfiguration.getSiadapYearConfiguration(year);

        List<AccountabilityType> accountabilities = new ArrayList<AccountabilityType>();
        accountabilities.add(siadapYearConfiguration.getUnitRelations());

        LocalDate startDate = SiadapMiscUtilClass.firstDayOfYear(year);

        request.setAttribute("startDate", startDate);
        request.setAttribute("accountabilities", accountabilities);
        request.setAttribute("showDeletedAccountabilities", Boolean.TRUE);
        request.setAttribute("returnPageURL", "/siadapProcessCount.do?method=manageDuplicatePersons&year=" + year.toString());

        return forward(request, "/organization/viewAccsAndHistory.jsp");

    }

    public ActionForward showUnit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
        if (siadapYearWrapper == null) {
            //let's try to get the year through the parameter
            String yearString = getAttribute(request, "year");
            if (yearString == null) {
                ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations.getYearsWithExistingConfigs();
                if (yearsWithConfigs.contains(new Integer(new LocalDate().getYear()))) {
                    int year = new LocalDate().getYear();
                    siadapYearWrapper = new SiadapYearWrapper(year);
                } else {
                    siadapYearWrapper = new SiadapYearWrapper(yearsWithConfigs.get(yearsWithConfigs.size() - 1));
                }

            } else {
                siadapYearWrapper = new SiadapYearWrapper(Integer.parseInt(yearString));
            }
        }
        request.setAttribute("siadapYearWrapper", siadapYearWrapper);
        SiadapYearConfiguration configuration = siadapYearWrapper.getSiadapYearConfiguration();

        if (configuration == null)
            return forward(request, "/module/siadap/unit.jsp");
        request.setAttribute("configuration", configuration);

        SiadapProcessStateEnumWrapper siadapProcessStateToFilter =
                (SiadapProcessStateEnumWrapper) getRenderedObject("siadapProcessStateToFilter");
        if (siadapProcessStateToFilter == null) {
            String enumAsParam = request.getParameter("siadapProcessStateEnumToFilterOrdinal");
            SiadapProcessStateEnum enumToUse = SiadapStateToShowInCount.getDefaultStateToFilter();
            if (!StringUtils.isBlank(enumAsParam)) {
                enumToUse = SiadapProcessStateEnum.valueOf(enumAsParam);
            }
            siadapProcessStateToFilter = new SiadapProcessStateEnumWrapper(enumToUse);
        }

        request.setAttribute("siadapProcessStateToFilter", siadapProcessStateToFilter);

        Unit unit = (Unit) getDomainObject(request, "unitId");
        if (unit == null) {
            unit = configuration.getSiadapStructureTopUnit();

            //and let's also get the total number of SIADAPs for this year
            int siadapsCount =
                    SiadapYearConfiguration.getSiadapYearConfiguration(siadapYearWrapper.getChosenYear()).getSiadapsCount();
            int siadapsDefinitiveCount = 0;
            Map<SiadapProcessStateEnum, Integer> stateCount = new HashMap<SiadapProcessStateEnum, Integer>();
            for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
                if (siadap.getYear().equals(siadapYearWrapper.getChosenYear())) {
                    siadapsDefinitiveCount++;
                    //let's put them on an HashMap by state TODO use Guave to do this
                    SiadapProcessStateEnum state = siadap.getState();
                    Integer integer = stateCount.get(state);
                    if (integer == null) {
                        integer = 0;
                    }
                    integer++;
                    stateCount.put(state, integer);

                }
            }

            request.setAttribute("totalDefinitiveCount", stateCount);
            request.setAttribute("siadapsCount", siadapsCount);
            request.setAttribute("siadapsDefinitiveCount", siadapsDefinitiveCount);

        }

        final Collection<Party> parents = UnitSiadapWrapper.UnitTransverseUtil.getActiveParents(unit, configuration);
        final Collection<Party> children = UnitSiadapWrapper.UnitTransverseUtil.getActiveChildren(unit, configuration);

        OrganizationChart<Party> chart = new OrganizationChart<Party>(unit, parents, children, 3);
        request.setAttribute("chart", chart);

        final Collection<Accountability> workerAccountabilities =
                UnitSiadapWrapper.UnitTransverseUtil.getActiveChildrenWorkers(unit, configuration);
        request.setAttribute("workerAccountabilities", workerAccountabilities);

        UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, configuration.getYear());
        final Person unitResponsible = unitSiadapWrapper.getEvaluationResponsible();
        request.setAttribute("unitResponsible", unitResponsible);

        final Collection<Person> unitHarmonizers = new TreeSet<Person>(Party.COMPARATOR_BY_NAME);
        UnitSiadapWrapper harmonizationUnit =
                new UnitSiadapWrapper(unitSiadapWrapper.getHarmonizationUnit(), configuration.getYear());

        if (harmonizationUnit.isValidHarmonizationUnit()) {
            unitHarmonizers.addAll(harmonizationUnit.getHarmonizationResponsibles());
        }

        request.setAttribute("unitHarmonizers", unitHarmonizers);

        return forward(request, "/module/siadap/unit.jsp");
    }

    public ActionForward showSummaryTables(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        final OrganizationalModel organizationalModel = UnitSiadapWrapper.findRegularOrgModel();
        //get the top unit (IST)
        String unitExternalId = getAttribute(request, "unitId");
        final Unit unit = UnitSiadapWrapper.getUnit(organizationalModel, unitExternalId);

        request.setAttribute("unit", unit);

        String year = getAttribute(request, "year");
        SiadapYearConfiguration siadapYearConfiguration =
                SiadapYearConfiguration.getSiadapYearConfiguration(Integer.valueOf(year));
        request.setAttribute("configuration", siadapYearConfiguration);

        return forward(request, "/module/siadap/summaryBoard.jsp");

    }

}
