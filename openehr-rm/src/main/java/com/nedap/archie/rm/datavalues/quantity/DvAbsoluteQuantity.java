package com.nedap.archie.rm.datavalues.quantity;

import javax.xml.bind.annotation.XmlType;

/**
 * Created by pieter.bos on 04/11/15.
 */
@XmlType(name="DV_ABSOLUTE_QUANTITY")
public abstract class DvAbsoluteQuantity<AccuracyType extends DvAmount, MagnitudeType  extends Comparable> extends  DvQuantified<AccuracyType,MagnitudeType> {
}
