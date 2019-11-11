package com.nedap.archie.serializer.adl;


import com.nedap.archie.base.MultiplicityInterval;

/**
 * @author Marko Pipan
 */
public class ArchetypeSerializeUtils {
    public static void buildOccurrences(ADLStringBuilder builder, MultiplicityInterval occ) {
        if(occ == null) {
            builder.append("*");
        }
        else if (occ.isLowerUnbounded() && occ.isUpperUnbounded()) {
            builder.append("*");
        } else if (occ.isLowerUnbounded()) {
            builder.append(0);
            builder.append("..");
            builder.append(getUpper(occ));
        } else if (occ.isUpperUnbounded()) {
            builder.append(getLower(occ));
            builder.append("..");
            builder.append("*");
        } else if (occ.getLower() != null && occ.isLowerIncluded() && occ.isUpperIncluded() && occ.getLower().equals(occ.getUpper())) {
            builder.append(getLower(occ));
        } else {
            builder.append(occ.getLower() != null ? occ.getLower() : 0);
            builder.append("..");
            builder.append(occ.getUpper() != null ? occ.getUpper() : "*");
        }
    }

    /**
     * fault-tolerant lower serialization class. Even works if lowerUnbounded=false, and lower==null
     * automatically takes into account lower included and upper included
     * @param occ
     * @return
     */
    private static String getLower(MultiplicityInterval occ) {
        if(occ.getLower() == null) {
            return "0";
        }
        if(!occ.isLowerIncluded()) {
            return ">" + occ.getLower();
        }
        return occ.getLower().toString();
    }

    private static String getUpper(MultiplicityInterval occ) {
        if(occ.getUpper() == null) {
            return "*";
        }
        if(!occ.isUpperIncluded()) {
            return "<" + occ.getUpper();
        }
        return occ.getUpper().toString();
    }
}
