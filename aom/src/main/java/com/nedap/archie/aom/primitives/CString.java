package com.nedap.archie.aom.primitives;

import com.google.common.collect.Lists;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.CPrimitiveObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Created by pieter.bos on 15/10/15.
 */
@XmlType(name="C_STRING")
@XmlAccessorType(XmlAccessType.FIELD)
public class CString extends CPrimitiveObject<String, String> {

    @XmlElement(name="assumed_value")
    private String assumedValue;
    private List<String> constraint = new ArrayList<>();

    public CString() {

    }

    public CString(String constraint) {
        this.constraint = Lists.newArrayList(constraint);
    }

    @Override
    public String getAssumedValue() {
        return assumedValue;
    }

    @Override
    public void setAssumedValue(String assumedValue) {
        this.assumedValue = assumedValue;
    }

    @Override
    public List<String> getConstraint() {
        return constraint;
    }

    @Override
    public void setConstraint(List<String> constraint) {
        this.constraint = constraint;
    }

    @Override
    public void addConstraint(String constraint) {
        this.constraint.add(constraint);
    }

    public boolean isValidValue(String value) {
        if(getConstraint().isEmpty()) {
            return true;
        }
        for(String constraint:getConstraint()) {
            if(constraint.length() > 1 &&
                    isRegexConstraint(constraint)) {
                //regexp. Strip first and last character and match. If you want to input
                //data starting and ending with '/', you cannot in the AOM, although ADL lets you express if just fine.
                //perhaps we should make the constraint object something more expressive than a String?
                if(matchesRegexp(value, constraint)) {
                    return true;
                }
            } else {
                //TODO: does case matter here?
                if(Objects.equals(value, constraint)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesRegexp(String value, String constraint) {
        return value.matches(constraint.substring(1).substring(0, constraint.length()-2));
    }

    public static boolean isRegexConstraint(String constraint) {
        return (constraint.startsWith("/") && constraint.endsWith("/")) ||
                (constraint.startsWith("^") && constraint.endsWith("^"));
    }

    @Override
    public boolean cConformsTo(CObject other, BiFunction<String, String, Boolean> rmTypesConformant) {
        if(!super.cConformsTo(other, rmTypesConformant)) {
            return false;
        }
        //now guaranteed to be the same class

        CString otherString = (CString) other;
        if(otherString.constraint.isEmpty()) {
            return true;
        }

        for(String constraint:constraint) {
            if(!hasMatchingConstraint(constraint, otherString)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasMatchingConstraint(String constraint, CString otherString) {
        boolean isRegexp = CString.isRegexConstraint(constraint);

        for(String otherConstraint:otherString.constraint) {
            boolean otherIsRegexp = CString.isRegexConstraint(otherConstraint);
            if(otherIsRegexp && !isRegexp) {

                if(matchesRegexp(constraint, otherConstraint)) {
                    return true;
                }
            } else if (otherIsRegexp && isRegexp) {
                //this is very hard to validate. Don't allow it until the spec gets updated to allow this
                return false;
            } else if(otherConstraint.equals(constraint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CString cString = (CString) o;
        return Objects.equals(assumedValue, cString.assumedValue) &&
                Objects.equals(constraint, cString.constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assumedValue, constraint);
    }
}

