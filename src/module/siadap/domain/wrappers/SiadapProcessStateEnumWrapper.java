/**
 * 
 */
package module.siadap.domain.wrappers;

import java.io.Serializable;

import module.siadap.domain.SiadapProcessStateEnum;

/**
 * The purpose of this class is to provide to the interface a bean with the
 * SiadapProcessStateEnum that is selected so that the SIADAP processes can be
 * filtered by state (initially useful for the statistics
 * [SiadapProcessCountAction] interface)
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapProcessStateEnumWrapper implements Serializable {
	
	
    /**
     * Default serial version id
     */
    private static final long serialVersionUID = 1L;
    private SiadapProcessStateEnum processStateEnum;
	
	
    public SiadapProcessStateEnumWrapper(SiadapProcessStateEnum stateEnum)
	{
	this.setProcessStateEnum(stateEnum);

	}


    public SiadapProcessStateEnum getProcessStateEnum() {
	return processStateEnum;
	}

    public void setProcessStateEnum(SiadapProcessStateEnum processStateEnum) {
	this.processStateEnum = processStateEnum;
	}


}
