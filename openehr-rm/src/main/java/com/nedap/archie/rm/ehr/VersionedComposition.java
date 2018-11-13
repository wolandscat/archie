package com.nedap.archie.rm.ehr;

import com.nedap.archie.rm.changecontrol.VersionedObject;
import com.nedap.archie.rm.composition.Composition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="VERSIONED_COMPOSITION")
public class VersionedComposition extends VersionedObject<Composition> {
}
