/**
 * 
 */
package module.siadap.domain.exceptions;

import java.util.ResourceBundle;

import myorg.domain.exceptions.DomainException;

/**
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 12 de Out de 2011
 * 
 * 
 */
public class SiadapException extends DomainException {

    public SiadapException(String key, String... args) {
	super(key, args);
    }
    
    public SiadapException(String key, Throwable throwable, String... args)
    {
	super(key, throwable,args);
    }

    @Override
    public ResourceBundle getBundle() {
	return ResourceBundle.getBundle("resources/SiadapResources");
    }

}
