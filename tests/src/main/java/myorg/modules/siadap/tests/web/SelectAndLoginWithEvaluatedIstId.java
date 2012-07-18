/*
 * @(#)SelectAndLoginWithEvaluatedIstId.java
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
package pt.ist.bennu.core.modules.siadap.tests.web;

import java.util.Map;

import org.cubictest.selenium.custom.ICustomTestStep;
import org.cubictest.selenium.custom.IElementContext;

import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author João Antunes
 * 
 */
public class SelectAndLoginWithEvaluatedIstId implements ICustomTestStep {

    /* (non-Javadoc)
     * @see org.cubictest.selenium.custom.ICustomTestStep#execute(java.util.Map, org.cubictest.selenium.custom.IElementContext, com.thoughtworks.selenium.Selenium)
     */
    public void execute(Map<String, String> arguments, IElementContext context, Selenium selenium) throws Exception {
	String allH2 = selenium.getText("css=H2");
	int istIdStartIndex = allH2.indexOf("ist");
	char[] istId = new char[255];
	allH2.getChars(allH2.indexOf("ist"), allH2.indexOf(")", istIdStartIndex), istId, 0);
	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append("IST ID: ");
	stringBuilder.append(istId);
	System.out.println(stringBuilder.toString());
	context.put("ISTID", new String(istId));
	
	

    }

}
