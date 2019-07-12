package com.nedap.archie.flattener;

import com.nedap.archie.aom.CAttribute;
import com.nedap.archie.aom.CObject;
import com.nedap.archie.aom.CPrimitiveObject;
import com.nedap.archie.aom.primitives.COrdered;
import com.nedap.archie.aom.primitives.CString;
import com.nedap.archie.base.Interval;

import java.util.List;
import java.util.Objects;

/**
 * Utils that fills assumed values everywhere where just one value is possible
 */
public class AssumedValueFiller {

    public void fillAssumedValues(CObject cObject) {
        if(cObject instanceof CPrimitiveObject) {
            fillAssumedValue((CPrimitiveObject) cObject);
        } else {
            for(CAttribute attribute:cObject.getAttributes()) {
                for(CObject child:attribute.getChildren()) {
                    fillAssumedValues(child);
                }
            }
        }
    }

    private void fillAssumedValue(CPrimitiveObject cObject) {
        if(cObject instanceof COrdered) {
            fillAssumedValueForOrdered((COrdered) cObject);
        } else if (cObject instanceof CString) {
            fillAssumedValueForString((CString) cObject);
        }
    }

    private void fillAssumedValueForString(CString cObject) {
        CString cString = cObject;
        List<String> constraint = cString.getConstraint();
        if(constraint != null && constraint.size() == 1) {
            String singleConstraint = constraint.get(0);
            if(!CString.isRegexConstraint(singleConstraint)) {
                cString.setAssumedValue(singleConstraint);
            }
        }
    }

    private void fillAssumedValueForOrdered(COrdered cObject) {
        COrdered cOrdered = cObject;
        List<Interval> constraint = cOrdered.getConstraint();
        if(constraint != null && constraint.size() == 1) {
            Interval interval = constraint.get(0);
            if(!interval.isLowerUnbounded() && !interval.isUpperUnbounded() &&
                    interval.isLowerIncluded() && interval.isUpperIncluded()
                    && Objects.equals(interval.getLower(), interval.getUpper())) {
                cOrdered.setAssumedValue(interval.getLower());
            }
        }
    }
}
