package module.siadap.domain;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import module.organization.domain.AccountabilityType;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import myorg.domain.User;
import myorg.domain.scheduler.ReadCustomTask;

import org.joda.time.LocalDate;

import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.fenixWebFramework.services.ServiceManager;
import pt.ist.fenixWebFramework.services.ServicePredicate;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

public class ImportSiadapStructure extends ReadCustomTask {

    Map<Integer, List<Evaluator>> avaliators = new HashMap<Integer, List<Evaluator>>();
    List<Responsible> responsibles = new ArrayList<Responsible>();
    public static Map<String, String> unknownUsersMap = new HashMap<String, String>();

    static {
	unknownUsersMap.put("ist25068", "Pedro Miguel Simões Coito");
	unknownUsersMap.put("ist25072", "Alvarinho Carvalho do Espirito Santo");
	unknownUsersMap.put("ist25079", "Adolfo Pereira Moura");
	unknownUsersMap.put("ist25095", "Marcelo Gurgel Figueiredo Moleiro");
	unknownUsersMap.put("ist25096", "Rute Catarina Panaças Guerreiro");
	unknownUsersMap.put("ist25104", "Teresa Jacinto de Oliveira Marques");
	unknownUsersMap.put("ist25106", "Miriam Mano Ferreira");
	unknownUsersMap.put("ist25109", "Elisabete Moreira de Oliveira Pino");
	unknownUsersMap.put("ist25110", "Dino Rodrigues Pereira das Neves");
	unknownUsersMap.put("ist25111", "Fábio André Duarte Morgado");
	unknownUsersMap.put("ist25112", "Hugo Alexandre Gonçalves Furtado");
	unknownUsersMap.put("ist25115", "Sandra Nazaré Gomes da Fonseca");
	unknownUsersMap.put("ist25118", "Joana Alves Lindinho Nunes de Castro");
	unknownUsersMap.put("ist25121", "Aurora Bonfim de Carvalho Oliveira");
	unknownUsersMap.put("ist25125", "Tiago Luís Ramos Silva Machado");
    }

    @Override
    public void doIt() {
	try {
	    FileInputStream fstream = new FileInputStream("/Users/ghost/siadap-import/siadap-list-istid.csv");
	    // Get the object of DataInputStream
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    // Read File Line By Line
	    while ((strLine = br.readLine()) != null) {
		processLine(strLine);
	    }
	    // Close the input stream
	    in.close();
	} catch (IOException e) {// Catch exception if any
	    System.err.println("Error: " + e.getMessage());
	}

	System.out.println(avaliators.size());

	for (Integer costCenter : avaliators.keySet()) {
	    List<Evaluator> evaluators = avaliators.get(costCenter);
	    String centerString = costCenter.toString();
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    Person responsible = null;
	    int max = 0;
	    for (Evaluator evaluator : evaluators) {
		User user = User.findByUsername(evaluator.istId);
		if (evaluator.size() > max) {
		    responsible = user.getPerson();
		    max = evaluator.size();
		}
	    }
	    responsibles.add(new Responsible(responsible.getUser(), c));
	}

	ServiceManager.execute(new ServicePredicate() {

	    @Override
	    public void execute() {
		migrateStuff(avaliators, responsibles);
	    }

	});

    }

    private void migrateStuff(Map<Integer, List<Evaluator>> avaliators2, List<Responsible> responsibles2) {
	System.out.println("STarting");
	LocalDate today = new LocalDate();
	SiadapYearConfiguration configuration = SiadapYearConfiguration.getSiadapYearConfiguration(today.getYear());
	AccountabilityType evaluationRelation = configuration.getEvaluationRelation();
	AccountabilityType workingRelation = configuration.getWorkingRelation();

	for (Responsible responsible : responsibles) {
	    Unit unit = responsible.getCenter().getUnit();
	    Person person = responsible.getUser().getPerson();
	    if (!unit.getChildPersons(evaluationRelation).contains(person)) {
		person.addParent(unit, evaluationRelation, today, null);
	    }
	}

	for (Integer costCenter : avaliators.keySet()) {
	    List<Evaluator> evaluators = avaliators.get(costCenter);
	    String centerString = costCenter.toString();
	    if (centerString.length() == 1) {
		centerString = "000" + centerString;
	    }
	    CostCenter c = (CostCenter) CostCenter.findUnitByCostCenter(centerString);
	    Unit unit = c.getUnit();

	    for (Evaluator evaluator : evaluators) {
		Person evaluatorPerson = evaluator.getUser().getPerson();
		boolean isResponsible = evaluatorPerson.getParentUnits(evaluationRelation).contains(unit);
		for (Evaluated evaluated : evaluator.evaluated) {
		    User user = evaluated.getUser();
		    if (user == null) {
			System.out.println(evaluated.istId + " NO USER");
			user = new User(evaluated.istId);
		    }
		    Person evaluatedPerson = user.getPerson();
		    if (evaluatedPerson == null) {
			System.out.println(evaluated.istId + " NO PERSON");
			String personName = ImportSiadapStructure.unknownUsersMap.get(evaluated.istId);
			if (personName == null) {
			    System.out.println("Found no user in table");
			}
			evaluatedPerson = Person.create(MultiLanguageString.i18n().add("pt", personName).finish(), Person
				.getPartyTypeInstance());
		    }

		    if (!unit.getChildPersons(workingRelation).contains(evaluatedPerson)) {
			evaluatedPerson.addParent(unit, workingRelation, today, null);
		    }
		    if (!isResponsible) {
			if (!evaluatorPerson.getChildPersons(evaluationRelation).contains(evaluatedPerson)) {
			    evaluatedPerson.addParent(evaluatorPerson, evaluationRelation, today, null);
			}
		    }
		}
	    }
	}
	System.out.println("DONE!");
    }

    private void processLine(String strLine) {
	String[] values = strLine.split(",");
	if (values.length != 3) {
	    return;
	}
	String cc = values[0].trim();
	Integer ccNumber = Integer.valueOf(cc);
	String evaluatorId = values[2].trim();
	String evaluatedId = values[1].trim();

	List<Evaluator> evaluators = avaliators.get(ccNumber);
	if (evaluators == null) {
	    System.out.println("Starting evalutors for " + cc);
	    evaluators = new ArrayList<Evaluator>();
	    avaliators.put(ccNumber, evaluators);
	}
	boolean added = false;
	for (Evaluator evaluator : evaluators) {
	    if (evaluator.match(evaluatorId)) {
		added = true;
		System.out.println("Adding existing evaluator " + evaluatorId + " for " + cc);
		evaluator.addEvaluated(evaluatedId);
	    }
	}
	if (!added) {
	    System.out.println("New evaluator " + evaluatorId + " for " + cc);
	    Evaluator evaluator = new Evaluator(evaluatorId);
	    evaluator.addEvaluated(evaluatedId);
	    evaluators.add(evaluator);
	}
    }

    public static class Evaluator {
	String istId;
	List<Evaluated> evaluated;

	public Evaluator(String istId) {
	    this.istId = istId.trim();
	    evaluated = new ArrayList<Evaluated>();
	}

	public void addEvaluated(String istId) {
	    evaluated.add(new Evaluated(istId));
	}

	public boolean match(String istId) {
	    return this.istId.equals(istId);
	}

	public int size() {
	    return evaluated.size();
	}

	public User getUser() {
	    return User.findByUsername(istId);
	}
    }

    public static class Evaluated {
	String istId;

	public Evaluated(String istId) {
	    this.istId = istId.trim();
	}

	public User getUser() {
	    return User.findByUsername(istId);
	}
    }

    public static class Responsible {
	User user;
	CostCenter center;

	public Responsible(User user, CostCenter center) {
	    this.user = user;
	    this.center = center;
	}

	public User getUser() {
	    return user;
	}

	public CostCenter getCenter() {
	    return center;
	}

    }
}
