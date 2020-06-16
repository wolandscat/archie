package com.nedap.archie.rm.ehr;

import com.nedap.archie.rm.archetyped.*;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.security.AccessControlSettings;
import com.nedap.archie.rm.support.identification.UIDBasedId;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Objects;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="EHR_ACCESS")
public class EhrAccess extends Locatable {
    @Nullable
    private AccessControlSettings settings;


    public EhrAccess() {
    }

    public EhrAccess(String archetypeNodeId, DvText name, @Nullable AccessControlSettings settings) {
        super(archetypeNodeId, name);
        this.settings = settings;
    }

    public EhrAccess(@Nullable UIDBasedId uid, String archetypeNodeId, DvText name, @Nullable Archetyped archetypeDetails, @Nullable FeederAudit feederAudit, @Nullable List<Link> links, @Nullable Pathable parent, @Nullable String parentAttributeName, @Nullable AccessControlSettings settings) {
        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit, links, parent, parentAttributeName);
        this.settings = settings;
    }

    @Nullable
    public AccessControlSettings getSettings() {
        return settings;
    }

    public void setSettings(@Nullable AccessControlSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EhrAccess ehrAccess = (EhrAccess) o;
        return Objects.equals(settings, ehrAccess.settings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), settings);
    }
}
