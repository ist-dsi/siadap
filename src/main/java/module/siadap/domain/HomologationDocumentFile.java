package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.util.ReportUtils;
import module.workflow.domain.WorkflowLog;
import module.workflow.domain.WorkflowProcess;
import module.workflow.util.ClassNameBundle;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.lang.StringUtils;
import org.fenixedu.bennu.core.i18n.BundleUtil;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.joda.time.DateTime;

@ClassNameBundle(bundle = "resources/SiadapResources")
public class HomologationDocumentFile extends HomologationDocumentFile_Base {

    public HomologationDocumentFile() {
        super();
    }

    private HomologationDocumentFile(String displayName, String filename, byte[] content) {
        super();
        this.setDisplayName(filename);
        init(displayName, filename, content);
    }

    @Override
    public void validateUpload(WorkflowProcess workflowProcess) {
        if (!(workflowProcess instanceof SiadapProcess)) {
            throw new SiadapException("must.only.be.used.with.siadap.processes");
        }
    }

    public HomologationDocumentFile(PersonSiadapWrapper personSiadapWrapper) {
        this(BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING,
                "SiadapProcessDocument.motive.homologation.displayName"), "SIADAP_homologacao_"
                + StringUtils.replaceEach(personSiadapWrapper.getSiadap().getProcess().getProcessNumber() + ".pdf", new String[] {
                        "\\", "/" }, new String[] { "_", "_" }), generateHomologationDocument(personSiadapWrapper,
                BundleUtil.getString(Siadap.SIADAP_BUNDLE_STRING, "SiadapProcessDocument.motive.homologation")));
        personSiadapWrapper.getSiadap().getProcess().addFiles(this);
    }

    public static byte[] generateHomologationDocument(PersonSiadapWrapper personSiadapWrapper, String generationMotive)
            throws SiadapException {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        SiadapProcess process = personSiadapWrapper.getSiadap().getProcess();

        final ResourceBundle resourceBundle = ResourceBundle.getBundle(Siadap.SIADAP_BUNDLE_STRING);
        paramMap.put("process", process);
        // joantune: NOTE: the PersonSiadapWrapper can be very handy because of
        // some methods, but it will most likely decrease performance on the
        // generation of the document
        paramMap.put("personSiadapWrapper", personSiadapWrapper);
        paramMap.put("documentGeneratedDate", new DateTime());
        paramMap.put("generationMotive", generationMotive);
        paramMap.put("siadap", process.getSiadap());
        ArrayList<WorkflowLog> orderedExecutionLogs = new ArrayList<WorkflowLog>(process.getExecutionLogs());
        Collections.sort(orderedExecutionLogs, WorkflowLog.COMPARATOR_BY_WHEN);
        paramMap.put("logs", orderedExecutionLogs);
        paramMap.put("logoFilename", "Logo_" + CoreConfiguration.getConfiguration().applicationUrl() + ".png");

        try {
            return ReportUtils.exportToPdfFileAsByteArray("/reports/siadapProcessDocument.jasper", paramMap, resourceBundle,
                    personSiadapWrapper.getAllObjEvaluationWrapperBeansOfDefaultEval());
        } catch (JRException e) {
            e.printStackTrace();
            throw new SiadapException(e, "SiadapProcessDocument.error.creating.document");
        }
    }

    @Override
    public boolean shouldFileContentAccessBeLogged() {
        return true;
    }

    @Override
    public boolean isPossibleToArchieve() {
        return false;
    }

}
