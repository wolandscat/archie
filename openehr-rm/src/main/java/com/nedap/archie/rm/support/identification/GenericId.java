package com.nedap.archie.rm.support.identification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GENERIC_ID", propOrder = {"scheme"})
public class GenericId extends ObjectId {

    private String scheme;

    public GenericId() {
    }

    public GenericId(String value, String scheme) {
        super(value);
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GenericId genericId = (GenericId) o;

        return scheme != null ? scheme.equals(genericId.scheme) : genericId.scheme == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
        return result;
    }
}
