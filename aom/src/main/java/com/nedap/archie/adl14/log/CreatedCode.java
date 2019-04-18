package com.nedap.archie.adl14.log;

import com.nedap.archie.base.terminology.TerminologyCode;

public class CreatedCode {

    private String generatedCode;
    private String pathCreated;
    private String rmTypeName;
    private ReasonForCodeCreation reasonForCreation;
    private TerminologyCode originalTerm;

    public CreatedCode() {

    }

    public CreatedCode(String generatedCode, ReasonForCodeCreation reason) {
        this.generatedCode = generatedCode;
        this.reasonForCreation = reason;
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public void setGeneratedCode(String generatedCode) {
        this.generatedCode = generatedCode;
    }

    public String getPathCreated() {
        return pathCreated;
    }

    public void setPathCreated(String pathCreated) {
        this.pathCreated = pathCreated;
    }

    public String getRmTypeName() {
        return rmTypeName;
    }

    public void setRmTypeName(String rmTypeName) {
        this.rmTypeName = rmTypeName;
    }

    public ReasonForCodeCreation getReasonForCreation() {
        return reasonForCreation;
    }

    public void setReasonForCreation(ReasonForCodeCreation reasonForCreation) {
        this.reasonForCreation = reasonForCreation;
    }

    public void setOriginalTerm(TerminologyCode termCode) {
        this.originalTerm = termCode;
    }
}