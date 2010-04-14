package module.siadap.domain.dto;

import java.io.Serializable;

import module.siadap.domain.CompetenceType;

public class CompetenceBean implements Serializable {

    private CompetenceType competenceType;
    private String name;
    private String description;

    public CompetenceBean(CompetenceType type) {
	super();
	setCompetenceType(type);
    }

    public void setCompetenceType(CompetenceType competenceType) {
	this.competenceType = competenceType;
    }

    public CompetenceType getCompetenceType() {
	return competenceType;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getDescription() {
	return description;
    }

}
