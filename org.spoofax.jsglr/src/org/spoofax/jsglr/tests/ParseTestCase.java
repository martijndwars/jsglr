/*
 * Created on 13.des.2005
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU Lesser General Public License, v2.1
 */
package org.spoofax.jsglr.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.spoofax.jsglr.FatalException;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.Tools;

import aterm.ATerm;
import aterm.pure.PureFactory;

public abstract class ParseTestCase extends TestCase {

    SGLR sglr;
    String suffix;
    // shared by all tests
    static final PureFactory pf = new PureFactory();

    public void setUp(String grammar, String suffix) throws FileNotFoundException, IOException, FatalException, InvalidParseTableException {
        this.suffix = suffix;

        sglr = new SGLR(pf, "tests/grammars/" + grammar + ".tbl");
    }

    protected void tearDown()
      throws Exception {
        super.tearDown();

        sglr.clear();
    }

    final static boolean doCompare = true;
    public void doParseTest(String s) throws FileNotFoundException, IOException {
        Tools.setOutput("tests/jsglr-full-trace-" + s);

        SGLR.forceGC();
        sglr.bootstrapTemporaryObjectsOnce();

        long parseTime = System.nanoTime();
        ATerm parsed = sglr.parse(new FileInputStream("tests/data/" + s + "." + suffix));
        parseTime = System.nanoTime() - parseTime;
        Tools.logger("Parsing ", s, " took " + parseTime/1000/1000, " millis.");
        System.out.println("Parsing " + s + " took " + parseTime/1000/1000 + " millis.");
        assertNotNull(parsed);

        // When running performance this is in the way due to the extra garbage created.
        if(doCompare) {
            ATerm loaded = sglr.getFactory().readFromFile("tests/data/" + s + ".trm");

            assertNotNull(loaded);

            if(parsed.match(loaded) == null) {
                PrintWriter printWriter = new PrintWriter(new FileOutputStream("tests/data/" + s + ".trm.parsed"));
                printWriter.print(parsed.toString());
                printWriter.flush();
                assertTrue(false);
            }
        }
    }
}
