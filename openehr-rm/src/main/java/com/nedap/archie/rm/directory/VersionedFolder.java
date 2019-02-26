package com.nedap.archie.rm.directory;

import com.nedap.archie.rm.changecontrol.VersionedObject;

import javax.xml.bind.annotation.XmlType;

/**
 * Created by pieter.bos on 08/07/16.
 */
@XmlType(name="VERSIONED_FOLDER")
public class VersionedFolder extends VersionedObject<Folder> {
}
