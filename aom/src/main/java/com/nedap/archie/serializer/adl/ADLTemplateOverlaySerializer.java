package com.nedap.archie.serializer.adl;

import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.TemplateOverlay;

import java.util.function.Function;

/**
 * @author markopi
 */
class ADLTemplateOverlaySerializer extends ADLArchetypeSerializer<TemplateOverlay> {


    public ADLTemplateOverlaySerializer(TemplateOverlay archetype, Function<String, Archetype> flatArchetypeProvider) {
        super(archetype, flatArchetypeProvider);
    }

    @Override
    protected String headTag() {
        return "template_overlay";
    }

    @Override
    protected void appendHeaderAttributes() {
    }

    @Override
    protected void appendLanguage() {
    }

    @Override
    protected void appendDescription() {
    }

    @Override
    protected void appendRules() {

    }
}
