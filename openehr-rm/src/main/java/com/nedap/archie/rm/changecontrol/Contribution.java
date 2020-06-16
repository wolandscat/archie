package com.nedap.archie.rm.changecontrol;

import com.nedap.archie.rm.RMObject;
import com.nedap.archie.rm.generic.AuditDetails;
import com.nedap.archie.rm.support.identification.HierObjectId;
import com.nedap.archie.rm.support.identification.ObjectRef;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlType(name = "CONTRIBUTION", propOrder = {
        "uid",
        "versions",
        "audit"
})
@XmlAccessorType(XmlAccessType.FIELD)
public class Contribution extends RMObject {

    private HierObjectId uid;
    private List<ObjectRef> versions = new ArrayList<>();
    private AuditDetails audit;

    public Contribution() {
    }

    public Contribution(HierObjectId uid, List<ObjectRef> versions, AuditDetails audit) {
        this.uid = uid;
        this.versions = versions;
        this.audit = audit;
    }

    public HierObjectId getUid() {
        return uid;
    }

    public void setUid(HierObjectId uid) {
        this.uid = uid;
    }

    public List<ObjectRef> getVersions() {
        return versions;
    }

    public void setVersions(List<ObjectRef> versions) {
        this.versions = versions;
    }

    public AuditDetails getAudit() {
        return audit;
    }

    public void setAudit(AuditDetails audit) {
        this.audit = audit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contribution that = (Contribution) o;

        return Objects.equals(uid, that.uid) &&
                Objects.equals(versions, that.versions) &&
                Objects.equals(audit, that.audit);

    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, versions, audit);
    }
}
