package com.nedap.archie.adl14;

import com.nedap.archie.aom.Archetype;
import com.nedap.archie.base.terminology.TerminologyCode;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Map;

//TODO: add a configuration here that supports converting terminology codes to URIs
public class ADL14ConversionUtil {

    public URI convertToUri(TerminologyCode value) throws URISyntaxException {
        //TODO: this should convert known terminology schemes into their corresponding standard URIs
        if(value.getUri() != null) {
            return value.getUri();
        } else if(value.getTerminologyVersion() != null) {
            return new URI(MessageFormat.format("http://{0}.org/ver/{1}/id/{2}", value.getTerminologyId(), value.getTerminologyVersion(), value.getCodeString()));
        }
        return new URI(MessageFormat.format("http://{0}.org/id/{1}", value.getTerminologyId(), value.getCodeString()));
    }

    public static String findExistingTermBinding(Archetype archetype, URI uri, Map<String, URI> termBindingsMap) {
        for(Map.Entry<String, URI> existingBinding:termBindingsMap.entrySet()) {
            if(existingBinding.getValue().equals(uri) && !existingBinding.getKey().startsWith("/")) {
                return existingBinding.getKey();
            }
        }
        return null;
    }
}
