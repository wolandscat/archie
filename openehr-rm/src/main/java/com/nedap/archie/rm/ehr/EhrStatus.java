package com.nedap.archie.rm.ehr;

/**
 * Created by pieter.bos on 08/07/16.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nedap.archie.rm.archetyped.Locatable;
import com.nedap.archie.rm.datastructures.ItemStructure;
import com.nedap.archie.rm.generic.PartySelf;
import com.nedap.archie.rminfo.RMProperty;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="EHR_STATUS")
public class EhrStatus extends Locatable {

    private PartySelf subject;
    @XmlElement(name="is_queryable")
    @RMProperty("is_queryable")
    private boolean isQueryable;
    @XmlElement(name="is_modifiable")
    @RMProperty("is_modifiable")
    private boolean isModifiable;
    @Nullable
    @XmlElement(name="other_details")
    private ItemStructure otherDetails;

    public PartySelf getSubject() {
        return subject;
    }

    public void setSubject(PartySelf subject) {
        this.subject = subject;
    }

    @JsonProperty(value = "is_queryable")
    public boolean isQueryable() {
        return isQueryable;
    }

    public void setQueryable(boolean queryable) {
        isQueryable = queryable;
    }

    @JsonProperty(value = "is_modifiable")
    public boolean isModifiable() {
        return isModifiable;
    }

    public void setModifiable(boolean modifiable) {
        isModifiable = modifiable;
    }

    @Nullable
    public ItemStructure getOtherDetails() {
        return otherDetails;
    }

    public void setOtherDetails(@Nullable ItemStructure otherDetails) {
        this.otherDetails = otherDetails;
        setThisAsParent(otherDetails, "other_details");
    }
}
