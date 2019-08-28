package com.nedap.archie.rm.datastructures;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.nedap.archie.rminfo.RMPropertyIgnore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * added constraint is that this contains only one item
 * Created by pieter.bos on 04/11/15.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ITEM_SINGLE", propOrder = {
        "item"
})
public class ItemSingle extends ItemStructure<Element> {

    private Element item;

    public Element getItem() {
        return item;
    }

    public void setItem(Element item) {
        this.item = item;
    }

    @Override
    @RMPropertyIgnore
    @JsonIgnore
    @XmlTransient
    public List<Element> getItems() {
        return Lists.newArrayList(item);
    }
}
