/**
 * 
 */
package myorg.modules.siadap.tests.web;

import java.util.Map;

import org.cubictest.selenium.custom.ICustomTestStep;
import org.cubictest.selenium.custom.IElementContext;

import com.thoughtworks.selenium.Selenium;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
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
