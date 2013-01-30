/**
 * 
 */
package module.siadap.domain.util.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import module.contacts.domain.EmailAddress;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import module.siadap.domain.Siadap;
import module.siadap.domain.groups.SiadapStructureManagementGroup;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.presentationTier.actions.BaseAction;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.emailNotifier.domain.Email;
import pt.ist.fenixWebFramework.services.Service;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 30 de Jan de 2013
 *
 * 
 */
public class SiadapUtilActions {
    
    public static void notifyRemovalOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {
        // notify the users who have access to this interface
        String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.subject",
                String.valueOf(year), person.getName(), unit.getPresentationName());
        String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.managers.terminateUnitHarmonization.content",
                person.getName(), person.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

        // notify the user
        notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.person.terminateUnitHarmonization.subject",
                String.valueOf(year), unit.getPresentationName());

        notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.person.terminateUnitHarmonization.content",
                String.valueOf(year), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifyUser(request, notificationSubject, notificationContent, person);
        
    }
    public static void notifyAdditionOfHarmonizationResponsible(Person person, Unit unit, int year, HttpServletRequest request) {
        
        // notify the users who have access to this interface

        String notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.managers.addHarmonizationUnit.subject",
                String.valueOf(year), person.getUser().getUsername(), unit.getAcronym());
        String notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.managers.addHarmonizationUnit.content", person.getName(),
                person.getUser().getUsername(), unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifySiadapStructureManagementUsers(request, notificationSubject, notificationContent);

        // notify the user
        notificationSubject = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.person.addHarmonizationUnit.subject", String.valueOf(year),
                unit.getPresentationName());

        notificationContent = BundleUtil.getFormattedStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
                "manage.siadapStructure.notification.email.person.addHarmonizationUnit.content", String.valueOf(year),
                unit.getPresentationName(), unit.getAcronym());

        SiadapUtilActions.notifyUser(request, notificationSubject, notificationContent, person);
        
    }
    
    public static void notifySiadapStructureManagementUsers(final HttpServletRequest request, String subject, String content) {
        // get the SiadapStructureManagementUsers
        int year = Integer.parseInt(request.getParameter("year"));
        List<Person> persons = SiadapStructureManagementGroup.getListOfMembers(year);
        Person[] personArray = new Person[persons.size()];
        int i = 0;
        for (Person person : persons) {
            personArray[i++] = person;
        }

        // notify them
        notifyUser(request, subject, content, personArray);
    }

    public static void notifyUser(HttpServletRequest request, String notificationSubject, String notificationContent,
            Person... persons) {
        // get the user e-mail
        ArrayList<String> usersEmails = new ArrayList<String>();
        for (Person person : persons) {
            try {
                String emailAddress = EmailAddress.getEmailForSendingEmails(person);
                if (StringUtils.isBlank(emailAddress)) {
                    String[] arguments = { person.getName() };
                    addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo",
                            arguments);

                } else {
                    usersEmails.add(emailAddress);
                }
            } catch (Throwable ex) {
                String[] arguments = { person.getName() };
                addMessage(request, "WARNING", "manage.siadapStructure.notification.email.notAbleToSendTo", arguments);
            }
        }
        auxNotifyUser(usersEmails, notificationSubject, notificationContent);
    }

    protected static void addMessage(final HttpServletRequest request, final String key, final String... args) {
        addMessage(request, "message", key, args);
        }

        protected static void addMessage(final HttpServletRequest request, final String property, final String key, final String... args) {
        final ActionMessages messages = getMessages(request);
        messages.add(property, new ActionMessage(key, args));
        saveMessages(request, messages);
        }

    

    // created because of the faulty dml injector
    @Service
    private static void auxNotifyUser(ArrayList<String> usersEmails, String notificationSubject, String notificationContent) {
        final VirtualHost virtualHost = VirtualHost.getVirtualHostForThread();
        new Email(virtualHost.getApplicationSubTitle().getContent(), virtualHost.getSystemEmailAddress(),
                new String[] {}, usersEmails, Collections.EMPTY_LIST, Collections.EMPTY_LIST, notificationSubject,
                notificationContent);

    }
    
    private static ActionMessages getMessages(HttpServletRequest request) {
        ActionMessages messages =
            (ActionMessages) request.getAttribute(Globals.MESSAGE_KEY);
        if (messages == null) {
            messages = new ActionMessages();
        }
        return messages;
    }
    
    private static void saveMessages(
            HttpServletRequest request,
            ActionMessages messages) {

            // Remove any messages attribute if none are required
            if ((messages == null) || messages.isEmpty()) {
                request.removeAttribute(Globals.MESSAGE_KEY);
                return;
            }

            // Save the messages we need
            request.setAttribute(Globals.MESSAGE_KEY, messages);
        }
}
