/**
 *
 */
package module.siadap.domain.util.actions;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.messaging.core.domain.Message;
import org.fenixedu.messaging.core.template.DeclareMessageTemplate;
import org.fenixedu.messaging.core.template.TemplateParameter;

import com.google.common.collect.Maps;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import pt.ist.fenixframework.Atomic;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 30 de Jan de 2013
 *
 *
 */
@DeclareMessageTemplate(id = "siadap.harmonization.person", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.harmonization.person", subject = "template.siadap.harmonization.person.subject",
        text = "template.siadap.harmonization.person.text", parameters = {
                @TemplateParameter(id = "action", description = "template.parameter.harmonization.action"),
                @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "unitName", description = "template.parameter.harmonization.unit.name"),
                @TemplateParameter(id = "unitAcronym", description = "template.parameter.harmonization.unit.acronym"),
                @TemplateParameter(id = "year", description = "template.parameter.year") })
@DeclareMessageTemplate(id = "siadap.harmonization.managers", bundle = Siadap.SIADAP_BUNDLE_STRING,
        description = "template.siadap.harmonization.managers", subject = "template.siadap.harmonization.managers.subject",
        text = "template.siadap.harmonization.managers.text", parameters = {
                @TemplateParameter(id = "action", description = "template.parameter.harmonization.action"),
                @TemplateParameter(id = "applicationUrl", description = "template.parameter.application.url"),
                @TemplateParameter(id = "personName", description = "template.parameter.harmonization.person.name"),
                @TemplateParameter(id = "personUsername", description = "template.parameter.harmonization.person.username"),
                @TemplateParameter(id = "unitName", description = "template.parameter.harmonization.unit.name"),
                @TemplateParameter(id = "unitAcronym", description = "template.parameter.harmonization.unit.acronym"),
                @TemplateParameter(id = "year", description = "template.parameter.year") })
public class SiadapUtilActions {

    public static void notifyRemovalOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {
        notifyOfHarmonizationResponsible(person, unit, year, request, "removal");
    }

    public static void notifyAdditionOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {
        notifyOfHarmonizationResponsible(person, unit, year, request, "addition");
    }

    private static void notifyOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request,
            String action) {
        String template = "siadap.harmonization.person";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("year", year);
        parameters.put("unitName", unit.getPresentationName());
        parameters.put("unitAcronym", unit.getAcronym());
        parameters.put("action", action);
        parameters.put("applicationUrl", CoreConfiguration.getConfiguration().applicationUrl());
        Supplier<Stream<User>> users = () -> Stream.of(person.getUser());
        SiadapUtilActions.notifyUser(request, users, template, parameters);

        template = "siadap.harmonization.managers";
        parameters.put("personName", person.getName());
        parameters.put("personUsername", person.getUser().getUsername());
        SiadapUtilActions.notifySiadapStructureManagementUsers(request, template, parameters);
    }

    public static void notifySiadapStructureManagementUsers(final HttpServletRequest request, String template,
            Map<String, Object> parameters) {
        // get the SiadapStructureManagementUsers
        int year = Integer.parseInt(request.getParameter("year"));
        Supplier<Stream<User>> users = () -> Group.dynamic("SiadapStructureManagementGroup").getMembers();

        // notify them
        auxNotifyUser(users, template, parameters);
    }

    public static void notifyUser(HttpServletRequest request, Supplier<Stream<User>> users, String template,
            Map<String, Object> parameters) {
        auxNotifyUser(users, template, parameters);
    }

    protected static void addMessage(final HttpServletRequest request, final String key, final String... args) {
        addMessage(request, "message", key, args);
    }

    protected static void addMessage(final HttpServletRequest request, final String property, final String key,
            final String... args) {
        final ActionMessages messages = getMessages(request);
        messages.add(property, new ActionMessage(key, args));
        saveMessages(request, messages);
    }

    // created because of the faulty dml injector
    @Atomic
    private static void auxNotifyUser(Supplier<Stream<User>> users, String template, Map<String, Object> parameters) {
        Message.fromSystem().to(Group.users(users.get())).template(template, parameters).send();
    }

    private static ActionMessages getMessages(HttpServletRequest request) {
        ActionMessages messages = (ActionMessages) request.getAttribute(Globals.MESSAGE_KEY);
        if (messages == null) {
            messages = new ActionMessages();
        }
        return messages;
    }

    private static void saveMessages(HttpServletRequest request, ActionMessages messages) {

        // Remove any messages attribute if none are required
        if ((messages == null) || messages.isEmpty()) {
            request.removeAttribute(Globals.MESSAGE_KEY);
            return;
        }

        // Save the messages we need
        request.setAttribute(Globals.MESSAGE_KEY, messages);
    }
}
