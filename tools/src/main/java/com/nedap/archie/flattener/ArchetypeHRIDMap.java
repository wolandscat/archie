package com.nedap.archie.flattener;

import com.nedap.archie.aom.ArchetypeHRID;
import com.github.zafarkhaja.semver.Version;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ArchetypeHRIDMap<T> extends ConcurrentHashMap<ArchetypeHRID,T> {

    public T getLatestVersion(String archetypeId) throws IllegalArgumentException {
        return getLatestVersion(new ArchetypeHRID(archetypeId));
    }

    public T getLatestVersion(ArchetypeHRID archetypeHRID){

        //Early return for fully defined version
        if (archetypeHRID.getPatchVersion() != null) {
            return this.get(archetypeHRID);
        }

        //Filter if version is not fully defined
        List<ArchetypeHRID> keys = this.keySet().stream().
//                filter(id -> (id.getNamespace() == null && archetypeHRID.getNamespace() == null) ||
//                        ((id.getNamespace() != null && archetypeHRID.getNamespace() != null) &&
//                                id.getNamespace().equals(archetypeHRID.getNamespace()))).
//                filter(id -> id.getRmPublisher().equals(archetypeHRID.getRmPublisher())).
//                filter(id -> id.getRmPackage().equals(archetypeHRID.getRmPackage())).
//                filter(id -> id.getRmClass().equals(archetypeHRID.getRmClass())).
//                filter(id -> id.getConceptId().equals(archetypeHRID.getConceptId())).
                filter(id -> id.getIdUpToConcept().equals(archetypeHRID.getIdUpToConcept())).
                filter(id -> (archetypeHRID.getMajorVersion() == null) || id.getMajorVersion().equals(archetypeHRID.getMajorVersion())).
                filter(id -> (archetypeHRID.getMinorVersion() == null) || id.getMinorVersion().equals(archetypeHRID.getMinorVersion())).
                collect(Collectors.toList());

        //Sort in ascending order
        keys.sort(Comparator.comparing(o -> Version.valueOf(o.getReleaseVersion())));

        //Return latest version
        return (keys.size() == 0) ? null : this.get(keys.get(keys.size() - 1));

    }

}
