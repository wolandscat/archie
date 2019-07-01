package com.nedap.archie.adl14.aom14;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nedap.archie.aom.primitives.CInteger;
import com.nedap.archie.aom.primitives.CReal;
import com.nedap.archie.aom.primitives.CString;
import com.nedap.archie.base.Interval;

public class CDVQuantityItem {

    private String units;
    private Interval<Double> magnitude;
    private Interval<Long> precision;

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Interval<Double> getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(Interval<Double> magnitude) {
        this.magnitude = magnitude;
    }

    public Interval<Long> getPrecision() {
        return precision;
    }

    public void setPrecision(Interval<Long> precision) {
        this.precision = precision;
    }

    @JsonIgnore
    public CString getUnitsAdl2() {
        if(units == null) {
            return null;
        }
        CString result = new CString();
        result.addConstraint(units);
        return result;
    }

    @JsonIgnore
    public CReal getMagnitudeAdl2() {
        if(magnitude == null) {
            return null;
        }
        CReal result = new CReal();
        result.addConstraint(magnitude);
        return result;
    }

    @JsonIgnore
    public CInteger getPrecisionAdl2() {
        if(precision == null) {
            return null;
        }
        CInteger result = new CInteger();
        result.addConstraint(precision);
        return result;
    }
}
