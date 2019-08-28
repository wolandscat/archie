package com.nedap.archie.rm.demographic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nedap.archie.rm.archetyped.Locatable;
import com.nedap.archie.rm.datastructures.ItemStructure;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rminfo.RMPropertyIgnore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="PARTY_IDENTITY")
public class PartyIdentity extends Locatable {

    private ItemStructure details;

    @JsonIgnore
    @XmlTransient
    @RMPropertyIgnore
    public DvText getPurpose() {
        return getName();
    }

    public ItemStructure getDetails() {
        return details;
    }

    public void setDetails(ItemStructure details) {
        this.details = details;
        setThisAsParent(details, "details");
    }
}
