package module.siadap.util;

import net.sf.jasperreports.engine.fill.JRBaseFiller;
import net.sf.jasperreports.engine.fill.JRFillSubreport;
import net.sf.jasperreports.engine.fill.JRSubreportRunner;
import net.sf.jasperreports.engine.fill.JRSubreportRunnerFactory;

public class JRTxThreadSubreportRunnerFactory implements JRSubreportRunnerFactory {

    @Override
    public JRSubreportRunner createSubreportRunner(JRFillSubreport fillSubreport, JRBaseFiller subreportFiller) {
        return new JRTxThreadSubreportRunner(fillSubreport, subreportFiller);
    }

}


