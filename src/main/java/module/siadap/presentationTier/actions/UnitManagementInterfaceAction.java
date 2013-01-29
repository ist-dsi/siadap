/**
 * 
 */
package module.siadap.presentationTier.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.Party;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.organization.presentationTier.actions.OrganizationModelAction;
import module.siadap.domain.Siadap;
import module.siadap.domain.SiadapProcessStateEnum;
import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;
import module.siadap.domain.util.SiadapMiscUtilClass;
import module.siadap.domain.wrappers.SiadapProcessStateEnumWrapper;
import module.siadap.domain.wrappers.SiadapYearWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.siadap.presentationTier.renderers.providers.SiadapStateToShowInCount;
import module.siadap.presentationTier.renderers.providers.SiadapYearsFromExistingSiadapConfigurations;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.joda.time.LocalDate;

import pt.ist.bennu.core.presentationTier.actions.ContextBaseAction;
import pt.ist.bennu.core.presentationTier.component.OrganizationChart;
import pt.ist.bennu.core.util.VariantBean;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/unitManagementInterface")
/**
 * 
 * Interface used to manage the units.
 * 
 * Currently it is used to: - Add/remove unit harmonization responsibles to
 * harmonization units; - Edit the default evaluator to regular working units
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 28 de Jan de 2013
 * 
 * 
 */
public class UnitManagementInterfaceAction extends ContextBaseAction {

    @Override
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ActionForward forward = super.execute(mapping, form, request, response);
        OrganizationModelAction.addHeadToLayoutContext(request);
        return forward;
    }

    public static enum Mode {
        REGULAR_UNIT_MODE {
            @Override
            public AccountabilityType getUnitAccType(SiadapYearConfiguration configuration) {
                return configuration.getUnitRelations();
            }

            @Override
            public AccountabilityType[] getEmployeeAccTypes(SiadapYearConfiguration configuration) {
                return new AccountabilityType[] { configuration.getWorkingRelation(),
                        configuration.getWorkingRelationWithNoQuota() };
            }
        },
        HARMONIZATION_UNIT_MODE {
            @Override
            public AccountabilityType getUnitAccType(SiadapYearConfiguration configuration) {
                return configuration.getHarmonizationUnitRelations();
            }

            @Override
            public AccountabilityType[] getEmployeeAccTypes(SiadapYearConfiguration configuration) {
                return new AccountabilityType[] { configuration.getSiadap2HarmonizationRelation(),
                        configuration.getSiadap3HarmonizationRelation() };
            }
        };

        public abstract AccountabilityType getUnitAccType(SiadapYearConfiguration configuration);

        public abstract AccountabilityType[] getEmployeeAccTypes(SiadapYearConfiguration configuration);
    }
    public ActionForward addHarmonizationUnitResponsible(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.getAttribute("addHarmonizationUnitResponsible");
        
        return showUnit(mapping, form, request, response);
        
    }

    public ActionForward showUnit(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        Mode mode = null;
        String modeString = getAttribute(request, "mode");
        if (modeString != null)
            mode = Mode.valueOf(modeString);
        else
            mode = Mode.REGULAR_UNIT_MODE;

        request.setAttribute("mode", mode);

        SiadapYearWrapper siadapYearWrapper = (SiadapYearWrapper) getRenderedObject("siadapYearWrapper");
        if (siadapYearWrapper == null) {
            // let's try to get the year through the parameter
            String yearString = getAttribute(request, "year");
            if (yearString == null) {
                ArrayList<Integer> yearsWithConfigs = SiadapYearsFromExistingSiadapConfigurations
                        .getYearsWithExistingConfigs();
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

        if (configuration == null) {
            return forward(request, "/module/siadap/unitManagement/showUnit.jsp");
        }
        request.setAttribute("configuration", configuration);

        // let's always use the last day of the year
        Unit unit = (Unit) getDomainObject(request, "unitId");
        if (unit == null) {
            unit = configuration.getSiadapStructureTopUnit();

            if (mode.equals(Mode.REGULAR_UNIT_MODE)) {
                // and let's also get the total number of SIADAPs for this year
                int siadapsCount = SiadapYearConfiguration
                        .getSiadapYearConfiguration(siadapYearWrapper.getChosenYear()).getSiadapsCount();
                int siadapsDefinitiveCount = 0;
                Map<SiadapProcessStateEnum, Integer> stateCount = new HashMap<SiadapProcessStateEnum, Integer>();
                for (Siadap siadap : SiadapRootModule.getInstance().getSiadaps()) {
                    if (siadap.getYear().equals(siadapYearWrapper.getChosenYear())) {
                        siadapsDefinitiveCount++;
                        // let's put them on an HashMap by state TODO use Guava
                        // to
                        // do this
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
        }

        AccountabilityType unitAcc = mode.getUnitAccType(configuration);
        AccountabilityType[] employeeAccs = mode.getEmployeeAccTypes(configuration);

        UnitSiadapWrapper unitSiadapWrapper = new UnitSiadapWrapper(unit, configuration.getYear());
        final Collection<Party> parents = UnitSiadapWrapper.UnitTransverseUtil.getActiveParents(unit, configuration,
                unitAcc);
        final Collection<Party> children = UnitSiadapWrapper.UnitTransverseUtil.getActiveChildren(unit, configuration,
                unitAcc, employeeAccs);

        OrganizationChart<Party> chart = new OrganizationChart<Party>(unit, parents, children, 3);
        request.setAttribute("chart", chart);

        final Collection<Accountability> workerAccountabilities = UnitSiadapWrapper.UnitTransverseUtil
                .getActiveChildrenWorkers(unit, configuration);
        request.setAttribute("workerAccountabilities", workerAccountabilities);

        final Person unitResponsible = unitSiadapWrapper.getEvaluationResponsible();
        request.setAttribute("unitResponsible", unitResponsible);

        final Collection<Person> unitHarmonizers = new TreeSet<Person>(Party.COMPARATOR_BY_NAME);
        UnitSiadapWrapper harmonizationUnit = new UnitSiadapWrapper(unitSiadapWrapper.getHarmonizationUnit(),
                configuration.getYear());

        if (harmonizationUnit.isValidHarmonizationUnit())
            unitHarmonizers.addAll(harmonizationUnit.getHarmonizationResponsibles());

        request.setAttribute("unitHarmonizers", unitHarmonizers);
        
        VariantBean bean = new VariantBean();
        request.setAttribute("bean", bean);

        return forward(request, "/module/siadap/unitManagement/showUnit.jsp");
    }

}
