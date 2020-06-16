package com.nedap.archie.rm.composition;

import com.nedap.archie.rm.archetyped.*;
import com.nedap.archie.rm.datastructures.ItemStructure;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.datavalues.encapsulated.DvParsable;
import com.nedap.archie.rm.support.identification.UIDBasedId;

import javax.annotation.Nullable;

import javax.xml.bind.annotation.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Objects;


/**
 * Created by pieter.bos on 04/11/15.
 */
@XmlRootElement(name = "activity", namespace = "http://schemas.openehr.org/v1" )
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ACTIVITY", propOrder = {
        "description",
        "timing",
        "actionArchetypeId"
})
public class Activity extends Locatable {

    @XmlElement(name = "description", required = true)
    private ItemStructure description;

    @Nullable
    @XmlElement(name = "timing")
    private DvParsable timing;

    @XmlElement(name = "action_archetype_id")
    private String actionArchetypeId;

    public Activity() {
    }

    public Activity(String archetypeNodeId, DvText name, ItemStructure description, @Nullable DvParsable timing, @Nullable String actionArchetypeId) {
        super(archetypeNodeId, name);
        this.description = description;
        this.timing = timing;
        this.actionArchetypeId = actionArchetypeId;
    }

    public Activity(@Nullable UIDBasedId uid, String archetypeNodeId, DvText name, @Nullable Archetyped archetypeDetails, @Nullable FeederAudit feederAudit, @Nullable List<Link> links, @Nullable Pathable parent, @Nullable String parentAttributeName, ItemStructure description, @Nullable DvParsable timing, @Nullable String actionArchetypeId) {
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent, parentAttributeName);
        this.description = description;
        this.timing = timing;
        this.actionArchetypeId = actionArchetypeId;
    }

    public ItemStructure getDescription() {
        return description;
    }

    public void setDescription(ItemStructure description) {
        this.description = description;
    }

    public DvParsable getTiming() {
        return timing;
    }

    public void setTiming(DvParsable timing) {
        this.timing = timing;
    }

    @Nullable
    public String getActionArchetypeId() {
        return actionArchetypeId;
    }

    public void setActionArchetypeId(@Nullable String actionArchetypeId) {
        this.actionArchetypeId = actionArchetypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Activity activity = (Activity) o;
        return Objects.equals(description, activity.description) &&
                Objects.equals(timing, activity.timing) &&
                Objects.equals(actionArchetypeId, activity.actionArchetypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), description, timing, actionArchetypeId);
    }
}
