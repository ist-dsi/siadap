package module.siadap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import module.siadap.domain.exceptions.SiadapException;
import module.siadap.domain.wrappers.PersonSiadapWrapper;
import module.siadap.domain.wrappers.UnitSiadapWrapper;
import module.workflow.domain.AbstractWFDocsGroup;
import module.workflow.domain.ProcessDocumentMetaDataResolver;
import module.workflow.domain.WFDocsDefaultWriteGroup;
import module.workflow.domain.WorkflowLog;
import module.workflow.domain.WorkflowProcess;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import pt.ist.bennu.core.domain.VirtualHost;
import pt.ist.bennu.core.util.BundleUtil;
import pt.ist.bennu.core.util.ClassNameBundle;
import pt.ist.bennu.core.util.ReportUtils;

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
	this(BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING,
		"SiadapProcessDocument.motive.homologation.displayName"), "SIADAP_homologacao_"
		+ StringUtils.replaceEach(personSiadapWrapper.getSiadap().getProcess().getProcessNumber() + ".pdf", new String[] {
			"\\", "/" }, new String[] { "_", "_" }), generateHomologationDocument(personSiadapWrapper,
		BundleUtil.getStringFromResourceBundle(Siadap.SIADAP_BUNDLE_STRING, "SiadapProcessDocument.motive.homologation")));
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
	paramMap.put("logoFilename", "Logo_" + VirtualHost.getVirtualHostForThread().getHostname() + ".png");

	try {
	    return ReportUtils.exportToPdfFileAsByteArray("/reports/siadapProcessDocument.jasper", paramMap, resourceBundle,
		    personSiadapWrapper.getAllObjEvaluationWrapperBeansOfDefaultEval());
	} catch (JRException e) {
	    e.printStackTrace();
	    throw new SiadapException("SiadapProcessDocument.error.creating.document", e);
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

    public static class HomologationDocumentMetadataResolver extends ProcessDocumentMetaDataResolver<HomologationDocumentFile> {

	private final static String CAREER = "Carreira SIADAP";

	private final static String SIADAP_DEFAULT_UNIVERSE = "Universo SIADAP por omissão";

	private final static String EVALUATOR = "Avaliador";

	private final static String WORKING_UNIT = "Unidade de trabalho";

	@Override
	public @Nonnull
	Class<? extends AbstractWFDocsGroup> getWriteGroupClass() {
	    return WFDocsDefaultWriteGroup.class;
	}

	@Override
	public Map<String, String> getMetadataKeysAndValuesMap(HomologationDocumentFile processDocument) {
	    Map<String, String> metadataKeysAndValuesMap = super.getMetadataKeysAndValuesMap(processDocument);

	    SiadapProcess siadapProcess = (SiadapProcess) processDocument.getProcess();
	    metadataKeysAndValuesMap.put(CAREER, siadapProcess.getSiadap().getDefaultCompetenceType().getName());
	    metadataKeysAndValuesMap.put(SIADAP_DEFAULT_UNIVERSE, siadapProcess.getSiadap().getDefaultSiadapUniverse()
		    .getLocalizedName());
	    metadataKeysAndValuesMap.put(EVALUATOR, siadapProcess.getSiadap().getEvaluator().getPerson().getPresentationName());

	    PersonSiadapWrapper personSiadapWrapper = new PersonSiadapWrapper(siadapProcess.getSiadap().getEvaluated(),
		    siadapProcess.getSiadap().getYear());
	    UnitSiadapWrapper workingUnit = personSiadapWrapper.getWorkingUnit();
	    if (workingUnit != null && workingUnit.getUnit() != null) {
		metadataKeysAndValuesMap.put(WORKING_UNIT, workingUnit.getPresentationName());
	    }

	    return metadataKeysAndValuesMap;

	}
    }

}
