/**
 * 
 */
package module.siadap.domain.wrappers;

import java.io.Serializable;
import java.util.List;

import module.siadap.domain.SiadapRootModule;
import module.siadap.domain.SiadapYearConfiguration;

/**
 * The purpose of this class is to provide to the interface a bean with the year
 * that is selected so that the SIADAP processes can be listed by year
 * 
 * @author João Antunes (joao.antunes@tagus.ist.utl.pt)
 * 
 */
public class SiadapYearWrapper implements Serializable {
	
	
	private SiadapYearConfiguration siadapYearConfiguration;
	
	public SiadapYearWrapper(SiadapYearConfiguration siadapYearConfiguration)
	{
		this.siadapYearConfiguration = siadapYearConfiguration;
	}
	
	public SiadapYearWrapper(int year)
	{
		for(SiadapYearConfiguration siadapYearConfiguration : SiadapRootModule.getInstance().getYearConfigurations())
		{
			if (siadapYearConfiguration.getYear() == year)
				{this.siadapYearConfiguration = siadapYearConfiguration;}
		}
	}


	public void setSiadapYearConfiguration(SiadapYearConfiguration siadapYearConfiguration) {
		this.siadapYearConfiguration = siadapYearConfiguration;
	}

	public SiadapYearConfiguration getSiadapYearConfiguration() {
		return siadapYearConfiguration;
	}
}
