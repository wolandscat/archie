package com.nedap.archie.adl14;

import com.nedap.archie.adl14.log.ADL2ConversionLog;
import com.nedap.archie.adl14.treewalkers.ADL14Listener;
import com.nedap.archie.adlparser.antlr.Adl14Lexer;
import com.nedap.archie.adlparser.antlr.Adl14Parser;
import com.nedap.archie.antlr.errors.ANTLRParserErrors;
import com.nedap.archie.antlr.errors.ArchieErrorListener;
import com.nedap.archie.aom.Archetype;
import com.nedap.archie.aom.utils.ArchetypeParsePostProcesser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * Parses ADL files to Archetype objects.
 *
 */
public class ADL14Parser {

    private ANTLRParserErrors errors;

    private Lexer lexer;
    private Adl14Parser parser;
    private ADL14Listener listener;
    private ParseTreeWalker walker;
    private Adl14Parser.AdlContext tree;
    public ArchieErrorListener errorListener;

    /**
     * If true, write errors to the console, if false, do not
     */
    private boolean logEnabled = true;

    public ADL14Parser() {

    }

    public ADL2ConversionResult parse(String adl, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) throws IOException {
        return parse(CharStreams.fromString(adl), conversionConfiguration, previousConversion);
    }

    public ADL2ConversionResult parse(InputStream stream, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) throws IOException {
        return parse(CharStreams.fromStream(new BOMInputStream(stream), Charset.availableCharsets().get("UTF-8")), conversionConfiguration, previousConversion);
    }

    public ADL2ConversionResult parse(CharStream stream, ADL14ConversionConfiguration conversionConfiguration, ADL2ConversionLog previousConversion) {

        errors = new ANTLRParserErrors();
        errorListener = new ArchieErrorListener(errors);
        errorListener.setLogEnabled(logEnabled);

        lexer = new Adl14Lexer(stream);
        lexer.addErrorListener(errorListener);
        parser = new Adl14Parser(new CommonTokenStream(lexer));
        parser.addErrorListener(errorListener);
        tree = parser.adl(); // parse

        ADL14Listener listener = new ADL14Listener(errors, conversionConfiguration);
        walker= new ParseTreeWalker();
        walker.walk(listener, tree);
        Archetype result = listener.getArchetype();

        new ADL14DescriptionConverter().convert(result);

        ADL14NodeIDConverter adl14NodeIDConverter = new ADL14NodeIDConverter(result, conversionConfiguration, previousConversion);
        ADL2ConversionLog convert = adl14NodeIDConverter.convert();//fixes archetype in place

        setCorrectVersions(result);
        //set some values that are not directly in ODIN or ADL
        ArchetypeParsePostProcesser.fixArchetype(result);

        return new ADL2ConversionResult(result, convert);

    }

    private void setCorrectVersions(Archetype result) {
        result.setAdlVersion("2.0.6");
        result.setRmRelease("1.0.4");
    }

    public ANTLRParserErrors getErrors() {
        return errors;
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setLexer(Lexer lexer) {
        this.lexer = lexer;
    }

    public Adl14Parser getParser() {
        return parser;
    }

    public void setParser(Adl14Parser parser) {
        this.parser = parser;
    }

    public ADL14Listener getListener() {
        return listener;
    }

    public void setListener(ADL14Listener listener) {
        this.listener = listener;
    }

    public ParseTreeWalker getWalker() {
        return walker;
    }

    public void setWalker(ParseTreeWalker walker) {
        this.walker = walker;
    }

    public Adl14Parser.AdlContext getTree() {
        return tree;
    }

    public void setTree(Adl14Parser.AdlContext tree) {
        this.tree = tree;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }
}