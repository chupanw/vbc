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
package edu.cmu.cs.vbc.prog.checkstyle.checks.coding;

import antlr.collections.AST;
import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * <p>Checks that string literals are not used with
 * <code>==</code> or <code>&#33;=</code>.
 * </p>
 * <p>
 * Rationale: Novice Java programmers often use code like
 * <code>if (x == &quot;something&quot;)</code> when they mean
 * <code>if (&quot;something&quot;.equals(x))</code>.
 * </p>
 *
 * @author Lars K&uuml;hne
 */
public class StringLiteralEqualityCheck extends Check
{
	@VConditional
	private static boolean StringLiteralEquality = true;
	
	@Override
	public boolean isEnabled() {
		return StringLiteralEquality;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "string.literal.equality";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.EQUAL, TokenTypes.NOT_EQUAL};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.EQUAL, TokenTypes.NOT_EQUAL};
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        // no need to check for nulls here, == and != always have two children
        final AST firstChild = ast.getFirstChild();
        final AST secondChild = firstChild.getNextSibling();

        if ((firstChild.getType() == TokenTypes.STRING_LITERAL)
                || (secondChild.getType() == TokenTypes.STRING_LITERAL))
        {
            log(ast.getLineNo(), ast.getColumnNo(),
                    MSG_KEY, ast.getText());
        }
    }
}
