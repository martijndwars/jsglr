package org.spoofax.jsglr2.benchmark.jsglr2;

import java.io.IOException;
import java.net.URISyntaxException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.JSGLR2Variants;
import org.spoofax.jsglr2.benchmark.BaseBenchmark;
import org.spoofax.jsglr2.JSGLR2;
import org.spoofax.jsglr2.parser.IParser;
import org.spoofax.jsglr2.parser.ParseException;
import org.spoofax.jsglr2.parsetable.IParseTable;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.jsglr2.parsetable.ParseTableReader;
import org.spoofax.jsglr2.testset.Input;
import org.spoofax.jsglr2.testset.TestSet;
import org.spoofax.terms.ParseError;

public abstract class JSGLR2Benchmark extends BaseBenchmark {

	protected IParser<?, ?> parser; // Just parsing
    protected JSGLR2<?, ?, ?> jsglr2; // Parsing and imploding (including tokenization)

    protected JSGLR2Benchmark(TestSet testSet) {
		super(testSet);
	}
    
    @Param({"false", "true"})
    public boolean implode;
    
    @Param({"Null", "Basic", "Hybrid"})
    public JSGLR2Variants.ParseForestRepresentation parseForestRepresentation;
    
    @Param({"Basic", "Hybrid", "BasicElkhound", "HybridElkhound"})
    public JSGLR2Variants.StackRepresentation stackRepresentation;
    
    @Param({"Basic", "Elkhound"})
    public JSGLR2Variants.Reducing reducing;
    
    @Setup
    public void parserSetup() throws ParseError, ParseTableReadException, IOException, InvalidParseTableException, InterruptedException, URISyntaxException {
        IParseTable parseTable = ParseTableReader.read(testSetReader.getParseTableTerm());

        parser = JSGLR2Variants.getParser(parseTable, parseForestRepresentation, stackRepresentation, reducing);
        jsglr2 = JSGLR2Variants.getJSGLR2(parseTable, parseForestRepresentation, stackRepresentation, reducing);
    }
    
    @Benchmark
    public void benchmark(Blackhole bh) throws ParseException {
    		if (implode) {
    			if (parseForestRepresentation == JSGLR2Variants.ParseForestRepresentation.Null)
    				throw new IllegalStateException("imploding requires a parse forest");
    			
    			for (Input input : inputs)
    	            bh.consume(jsglr2.parseUnsafe(
    	                input.content,
    	                input.filename
    	            ));
    		} else {
    	        for (Input input : inputs)
    	            bh.consume(parser.parseUnsafe(
    	                input.content,
    	                input.filename
    	            ));
    		}
    }

}
