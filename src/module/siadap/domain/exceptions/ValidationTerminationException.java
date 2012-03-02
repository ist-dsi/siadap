/**
 * 
 */
package module.siadap.domain.exceptions;

/**
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt) - 2 de Mar de 2012
 *
 * 
 */
public class ValidationTerminationException extends SiadapException {

    public ValidationTerminationException(String key, String... args) {
	super(key, args);
    }


    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
