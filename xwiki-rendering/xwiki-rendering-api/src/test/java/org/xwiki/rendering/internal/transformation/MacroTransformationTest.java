/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.transformation;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.test.AbstractXWikiComponentTestCase;

/**
 * Unit tests for {@link MacroTransformation}.
 * 
 * @version $Id$
 */
public class MacroTransformationTest extends AbstractXWikiComponentTestCase
{
    private Transformation transformation;
    
    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractXWikiComponentTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.transformation = getComponentManager().lookup(Transformation.class, "macro");
    }

    /**
     * Test that a simple macro is correctly evaluated.
     */
    public void testSimpleMacroTransform() throws Exception
    {
        String expected = "beginDocument\n"
        	+ "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testsimplemacro",
            Collections.<String, String>emptyMap(), false)));
        
        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        WikiPrinter printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);
        assertEquals(expected, printer.toString());
    }

    /**
     * Test that a macro can generate another macro.
     */
    public void testNestedMacroTransform() throws Exception
    {
        String expected = "beginDocument\n"
        	+ "beginMacroMarkerStandalone [testnestedmacro] []\n"
        	+ "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endMacroMarkerStandalone [testnestedmacro] []\n"
            + "endDocument";

        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testnestedmacro",
            Collections.<String, String>emptyMap(), false)));
    
        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        WikiPrinter printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);
        assertEquals(expected, printer.toString());
    }
    
    /**
     * Test that we have a safeguard against infinite recursive macros.
     */
    public void testInfiniteRecursionMacroTransform() throws Exception
    {
        String expected = "beginDocument\n"
        	+ StringUtils.repeat("beginMacroMarkerStandalone [testrecursivemacro] []\n", 1000)
            + "onMacroStandalone [testrecursivemacro] []\n"
            + StringUtils.repeat("endMacroMarkerStandalone [testrecursivemacro] []\n", 1000)
            + "endDocument";
        
        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testrecursivemacro",
            Collections.<String, String>emptyMap(), false)));
    
        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        WikiPrinter printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);
        assertEquals(expected, printer.toString());
    }
    
    /**
     * Test that macro priorities are working.
     */
    public void testPrioritiesMacroTransform() throws Exception
    {
        String expected = "beginDocument\n"
        	+ "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testprioritymacro] []\n"
            + "beginParagraph\n"
            + "onWord [word]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testprioritymacro] []\n"
            + "endDocument";

        // "testprioritymacro" has a highest priority than "testsimplemacro" and will be executed first.
        // This is verified as follows:
        // - "testprioritymacro" generates a WordBlock
        // - "testsimplemacro" outputs "simplemacro" followed by the number of WordBlocks that exist in the document
        // Thus if "testsimplemacro" is executed before "testprioritymacro" it would print "simplemacro0"
        XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testprioritymacro", Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        WikiPrinter printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);
        assertEquals(expected, printer.toString());
    }
    
    /**
     * Test that macro with same priorities execute in the order in which they are defined.
     */
    public void testMacroWithSamePriorityExecuteOnPageOrder() throws Exception
    {
        // Both macros have the same priorities and thus "testsimplemacro" should be executed first and generate
        // "simplemacro0".
    	XDOM dom = new XDOM(Arrays.<Block>asList(
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false),
            new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false)));

        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        WikiPrinter printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);

        String expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro0]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "endDocument";
        assertEquals(expected, printer.toString());

        // We must also test the other order ("testcontentmacro" before "testsimplemacro") to ensure for example that 
        // there's no lexical order on Macro class names for example.
    	dom = new XDOM(Arrays.<Block>asList(
			new MacroBlock("testcontentmacro", Collections.<String, String>emptyMap(), "content", false),
            new MacroBlock("testsimplemacro", Collections.<String, String>emptyMap(), false)));

        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        printer = new DefaultWikiPrinter();
        getComponentManager().lookup(BlockRenderer.class, Syntax.EVENT_1_0.toIdString()).render(dom, printer);

        expected = "beginDocument\n"
            + "beginMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "onWord [content]\n"
            + "endMacroMarkerStandalone [testcontentmacro] [] [content]\n"
            + "beginMacroMarkerStandalone [testsimplemacro] []\n"
            + "beginParagraph\n"
            + "onWord [simplemacro1]\n"
            + "endParagraph\n"
            + "endMacroMarkerStandalone [testsimplemacro] []\n"
            + "endDocument";
        assertEquals(expected, printer.toString());
    }
    
    /**
     * Verify that
     * <pre><code>{{macro}}
     * content
     * {{/macro}}</code></pre>
     * is equivalent to:
     * <pre><code>{{macro}}content{{/macro}}</code></pre>
     */
    public void testMacroTransformationWithLeadingAndTrailingNewLines() throws Exception
    {
        XDOM dom = new XDOM(Arrays.asList((Block) new MacroBlock("testcontentmacro",
            Collections.<String, String>emptyMap(), "\nhello\n", false)));
        
        this.transformation.transform(dom, new Syntax(SyntaxType.XWIKI, "2.0"));

        assertEquals("hello", dom.getChildrenByType(WordBlock.class, true).get(0).getWord());
    }
}
