////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package edu.cmu.cs.vbc.prog.checkstyle.checks.javadoc;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailNode;
import edu.cmu.cs.vbc.prog.checkstyle.api.JavadocTokenTypes;

/**
 * Checks that a JavaDoc block which can fit on a single line and doesn't
 * contain at-clauses. Javadoc comment that contains at least one at-clause
 * should be formatted in few lines.
 *
 * Default configuration:
 * <pre>
 * &lt;module name=&quot;SingleLineJavadoc&quot;/&gt;
 * </pre>
 *
 * @author baratali
 * @author maxvetrenko
 *
 */
public class SingleLineJavadocCheck extends AbstractJavadocCheck
{
	@VConditional
	private static boolean SingleLineJavadoc = true;
	@Override
	public boolean isEnabled() {
		return SingleLineJavadoc;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "singleline.javadoc";

    @Override
    public int[] getDefaultJavadocTokens()
    {
        return new int[] {
            JavadocTokenTypes.JAVADOC,
        };
    }

    @Override
    public void visitJavadocToken(DetailNode ast)
    {
        if (isSingleLineJavadoc()
                && (hasJavadocTags(ast) || hasJavadocInlineTags(ast)))
        {
            log(ast.getLineNumber(), "singleline.javadoc");
        }
    }

    /**
     * Checks if comment is single line comment.
     *
     * @return true, if comment is single line comment.
     */
    private boolean isSingleLineJavadoc()
    {
        final DetailAST blockCommentStart = getBlockCommentAst();
        final DetailAST blockCommentEnd = blockCommentStart.getLastChild();

        return blockCommentStart.getLineNo() == blockCommentEnd.getLineNo();
    }

    /**
     * Checks if comment has javadoc tags.
     *
     * @param javadocRoot javadoc root node.
     * @return true, if comment has javadoc tags.
     */
    private boolean hasJavadocTags(DetailNode javadocRoot)
    {
        final DetailNode javadocTagSection =
                JavadocUtils.findFirstToken(javadocRoot, JavadocTokenTypes.JAVADOC_TAG);
        return javadocTagSection != null;
    }

    /**
     * Checks if comment has in-line tags tags.
     *
     * @param javadocRoot javadoc root node.
     * @return true, if comment has in-line tags tags.
     */
    private boolean hasJavadocInlineTags(DetailNode javadocRoot)
    {
        return JavadocUtils.branchContains(javadocRoot, JavadocTokenTypes.JAVADOC_INLINE_TAG);
    }
}
