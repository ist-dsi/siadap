/*
 * @(#)ImportTestUsers.java
 *
 * Copyright 2010 Instituto Superior Tecnico
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
package module.siadap.domain;

import java.util.ArrayList;
import java.util.List;

import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.exceptions.DomainException;
import pt.ist.bennu.core.domain.groups.NamedGroup;
import pt.ist.bennu.core.domain.groups.PersistentGroup;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;

/**
 * 
 * @author João Antunes
 * 
 */
public class ImportTestUsers extends WriteCustomTask {

    public static final String groupName = "SIADAP Test user group 2010";

    private static final String testUsersListString = new String(
    // Managers:
            "ist154457\n" + // João Antunes (eu)
                    //DEC Civil: ??
                    "ist12603\n" + //Vitor Leitão
                    //DEM Mecãnica: ??
                    "ist12889\n" + //Miguel Ayala Botto
                    // CIIST:
                    "ist23000\n" + //José Luis (por causa da ajuda)
                    "ist24698\n" + // Rita Wahl
                    "ist22619\n" + // Paula César
                    "ist12048\n" + // Fernando Mira da Silva
                    "ist24235\n" + //Nuno Pedroso
                    "ist138407\n" + // Fernando Oliveira
                    "ist24421\n" + // Miguel Cabeça
                    "ist13499\n" + // Carlos Ribeiro
                    "ist22159\n" + //Carlos Manuel Martins
                    "ist21534\n" + //Victor Manuel Loucao Correia Coias
                    //Núcleo de arquivo ??
                    "ist23555\n" + //Ana Cristina Fonseca da Silva Rigueiro
                    "ist23073\n" + //Aurora de Jesus Barbosa
                    // DRH
                    "ist24877\n" + // Nuno Cunha Rolo
                    "ist23932\n" + // Miguel Coimbra
                    "ist22752\n" + // Fátima Novais
                    "ist24252\n" + // Dulce Cunha
                    //DT
                    "ist23470\n" + //José Manuel Riscado
                    "ist22757\n" + //Maria Salomé Louro
                    "ist22234\n" + //Paula Sequeira
                    "ist24229\n" + //Ana Cristina Oliveira 

                    "ist21901"); // Fátima Fernandes 

    @Override
    protected void doService() {
        // let's try to get the group
        MyOrg myOrg = MyOrg.getInstance();
        NamedGroup siadapTestUserGroup = null;
        for (PersistentGroup group : myOrg.getPersistentGroups()) {
            if (group instanceof NamedGroup) {
                if (((NamedGroup) group).getName().equals(groupName)) {
                    siadapTestUserGroup = (NamedGroup) group;
                    out.println("Found a previously existing group!");
                }
            }

        }

        // get all of the users from the static list of strings with their
        // userIds, abort if any isn't found
        List<User> existingSystemUsers = myOrg.getUser();
        List<User> testUsersList = new ArrayList<User>();
        for (User user : existingSystemUsers) {
            if (testUsersListString.contains(user.getUsername())) {
                testUsersList.add(user);
                out.println("*DEBUG* added user with userId: " + user.getUsername());
                if (user.getPerson() != null) {
                    out.println("*DEBUG* added user with name: " + user.getPerson().getName());
                } else {
                    out.println("*DEBUG* found a user without name!");
                    out.println("*DEBUG* users info, comments count: " + user.getCommentsCount() + " password: "
                            + user.getPassword() + " ExternalID: " + user.getExternalId());
                    out.println("*DEBUG* Ignoring it!");
                    testUsersList.remove(user);
                }
            }
        }
        if (testUsersList.size() != testUsersListString.split("\n").length) {
            out.println("Mismatch between the number of users parsed on the string (" + testUsersListString.split("\n").length
                    + ") and the existing and found ones (" + testUsersList.size() + "). Users given:\n--\n"
                    + testUsersListString + "--\n Users on the found users list:\n--");
            for (User user : testUsersList) {
                out.println(user.getUsername());
            }
            out.println("--\nend of the list");
            throw new DomainException("siadap.create.test.group.invalid.users", "resources/SiadapResources");
        }
        int removedUserCounter = 0;

        int addedUserCounter = 0;
        int alreadyExistsCounter = 0;
        // if we haven't found the group, let's create it and the users to it if
        // they
        // aren't there already
        if (siadapTestUserGroup == null) {
            siadapTestUserGroup = new NamedGroup(groupName);
            out.println("Group didn't existed, created the group '" + groupName + "'");
        }
        List<User> currentUsers = new ArrayList<User>(siadapTestUserGroup.getMembers());
        for (User user : testUsersList) {
            if (!currentUsers.contains(user)) {
                siadapTestUserGroup.addUsers(user);
                addedUserCounter++;
            } else {
                alreadyExistsCounter++;
            }
        }
        // let's remove the users in excess
        currentUsers = new ArrayList<User>(siadapTestUserGroup.getMembers());
        currentUsers.removeAll(testUsersList);
        for (User user : currentUsers) {
            siadapTestUserGroup.removeUsers(user);
            removedUserCounter++;
        }
        out.println("Job done! Added users: " + addedUserCounter + " already existing users: " + alreadyExistsCounter
                + " removed users: " + removedUserCounter);

    }
}
