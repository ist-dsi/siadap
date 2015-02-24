/**
 * 
 */
package module.siadap.domain.util.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.DynamicGroup;
import org.fenixedu.bennu.core.groups.UserGroup;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.messaging.domain.Message.MessageBuilder;
import org.fenixedu.messaging.domain.MessagingSystem;
import org.fenixedu.messaging.domain.Sender;

import pt.ist.fenixframework.Atomic;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 30 de Jan de 2013
 * 
 * 
 */
public class SiadapUtilActions {

    public static void notifyRemovalOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {
        // notify the users who have access to this interface
        String notificationSubject =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.subject",
                        String.valueOf(year), person.getName(), unit.getPresentationName());
        String notificationContent =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.content",
                        person.getName(), person.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

        // notify the user
        notificationSubject =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.person.terminateUnitHarmonization.subject",
                        String.valueOf(year), unit.getPresentationName());

        notificationContent =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.person.terminateUnitHarmonization.content",
                        String.valueOf(year), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifyUser(request, notificationSubject, notificationContent, Collections.singleton(person.getUser()));

    }

    public static void notifyAdditionOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {

        // notify the users who have access to this interface

        String notificationSubject =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.managers.addHarmonizationUnit.subject", String.valueOf(year),
                        person.getUser().getUsername(), unit.getAcronym());
        String notificationContent =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.managers.addHarmonizationUnit.content", person.getName(),
                        person.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

        // notify the user
        notificationSubject =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.person.addHarmonizationUnit.subject", String.valueOf(year),
                        unit.getPresentationName());

        notificationContent =
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                        "manage.siadapStructure.notification.email.person.addHarmonizationUnit.content", String.valueOf(year),
                        unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifyUser(request, notificationSubject, notificationContent, Collections.singleton(person.getUser()));

    }

    public static void notifySiadapStructureManagementUsers(final HttpServletRequest request, String subject, String content) {
        // get the SiadapStructureManagementUsers
        int year = Integer.parseInt(request.getParameter("year"));
        Collection<User> users = DynamicGroup.get("SiadapStructureManagementGroup").getMembers();

        // notify them
        notifyUser(request, subject, content, users);
    }

    public static void notifyUser(HttpServletRequest request, String notificationSubject, String notificationContent,
            Collection<User> users) {
        // get the user e-mail
        Set<User> usersEmails = new HashSet<User>();
        for (User user : users) {
            try {
                String emailAddress = user.getProfile() == null ? "" : user.getProfile().getEmail();
                if (StringUtils.isBlank(emailAddress)) {
                    String[] arguments = { user.getPerson().getName() };
                    addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo", arguments);
                } else {
                    usersEmails.add(user);
                }
            } catch (Throwable ex) {
                String[] arguments = { user.getPerson().getName() };
                addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo", arguments);
            }
        }
        auxNotifyUser(usersEmails, notificationSubject, notificationContent);
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
    private static void auxNotifyUser(Set<User> users, String notificationSubject, String notificationContent) {
        final Sender sender = MessagingSystem.getInstance().getSystemSender();
        final MessageBuilder message = sender.message(notificationSubject, notificationContent);
        message.to(UserGroup.of(users));
        message.send();
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
