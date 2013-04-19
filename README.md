# SIADAP

An end user application that implements the Portuguese public administration evaluation system (Sistema Integrado de Gestão e Avaliação do Desempenho na Adiministração Pública [SIADAP] )

More info (in Portuguese) can be found at the [SIADAP's official page](http://www.dgap.gov.pt/index.cfm?OBJID=83ddd323-6047-46db-b137-6a732c8c2202)

---
##Change Log:

### v1.1.0 

Release milestone and issues can be found [here](https://github.com/ist-dsi/siadap/issues?milestone=1&state=closed)

** New Features: **
	
- SIADAP can now be Biennial;
- Possibility to add a SIADAP2 process for curricular ponderation purposes only (immediately set as not evaluated);
- ExtendSiadapStructure - script that extends the SIADAP structure (people and units) from one year to the other, now also takes into account the information in the system regarding their actual worker status. If the person isn't working anymore, it does not create the process and removes the working and harmonization accountabilites;
- ExtendSiadapStructure - doesn't extend nulled processes;
- A more conservative version of the 'Remove from structure' has been implemented, that does not remove eventual responsabilites (harmonization or evaluation) that that person might have;

** Refactor: **

 - Applied all the ~2012/2013 changes stated in the law to the SIADAP3, specifically:
 	- By default, SIADAP3 processes are Biennial;
 	- Not possible anymore to create SIADAP2 processes (due to the incompatibility of the changes with the current system [unless it's for curricular ponderation purposes]);
 	- A Maximum of 7 objectives per biennial process;
 	- A maximum of 3 indicators per objective;
 	
** Bug Fixes: **

 - Creating a new SIADAP process for a user with an already existing Harmonization accountability now closes the previous harm. accountability (if it's not the appropriate type) when creating the process, and sets the new one to the working unit (if there's any already defined);
 - Misc. interfaces - Removed the obsolete 'Create' link to create a SIADAP process (it wasn't working);
 - SIADAP - printed version of the process (and homologation document) - now has the evaluator stated as well as the evaluated person (it had an incorrect label where listed the evaluated as evaluator)

### v1.0.0

** New Features: **

 - Implements all of the SIADAP2 and SIADAP3 requirements, i.e.:
 	- Definition of objectives and competences;
 	- Acknowledgement of objectives and competences;
 	- Self-Evaluation;
 	- Evaluation;
 	- Harmonization (helper for the validation done by the CCA - that allows to split the whole of the employees into smaller units - Harmonization units - and lets the harmonization responsibles give hints and suggestions to the CCA for the validation process)
 	- Validation;
 	- Acknowledgement of the Validation;
 	- Revision of the grade;
 	- Curricular ponderation;
 	- Homologation;
 
 - Support interfaces:
 	- Statistics - a view of the units with the number of people that have the processes in each given possible state - and has the 'live' summary board with all the info usually requested by the superior public administration offices (government ministries);
 	- Configuration - configure quotas, harmonization restrictions, dates, ACLs for the different major interfaces/functionalities, etc.
 	- Personnel management - add/remove/change details of a person inside the SIADAP structure - e.g. change working unit, SIADAP type, competences type, individual evaluation relations, etc.
 	- Unit management - allows you to manage harmonization unit's responsibles;
 	
- Data export:
	- Export to excel of:
		- All the info including the grades (only available for CCA);
		- The harmonization structure (i.e. which units are under which harmonization units);
		- Information of evaluators/evaluted people grouped by main working unit;
 	
	