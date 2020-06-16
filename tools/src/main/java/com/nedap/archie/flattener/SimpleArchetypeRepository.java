package com.nedap.archie.flattener;

import com.nedap.archie.aom.Archetype;

import java.util.ArrayList;
import java.util.List;

/**
 * In memory ConcurrentHashMap-based archetyperepository. Thread-safe
 * Created by pieter.bos on 21/10/15.
 */
public class SimpleArchetypeRepository implements ArchetypeRepository, MutableArchetypeRepository {

    private ArchetypeHRIDMap<Archetype> archetypes = new ArchetypeHRIDMap<>();

    @Override
    public Archetype getArchetype(String archetypeId) {
        return archetypes.getLatestVersion(archetypeId);
    }

    @Override
    public void addArchetype(Archetype archetype) {
        archetypes.put(archetype.getArchetypeId(), archetype);
    }

    public List<Archetype> getAllArchetypes() {
        return new ArrayList<>(archetypes.values());
    }

}
