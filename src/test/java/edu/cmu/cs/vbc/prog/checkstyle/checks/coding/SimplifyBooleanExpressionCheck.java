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

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * <p>
 * Checks for overly complicated boolean expressions. Currently finds code like
 * <code>if (b == true)</code>, <code>b || true</code>, <code>!false</code>,
 * etc.
 * </p>
 * <p>
 * Rationale: Complex boolean logic makes code hard to understand and maintain.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="SimplifyBooleanExpression"/&gt;
 * </pre>
 * @author lkuehne
 */
public class SimplifyBooleanExpressionCheck
        extends Check
{
	@VConditional
	private static boolean SimplifyBooleanExpression = true;
	
	@Override
	public boolean isEnabled() {
		return SimplifyBooleanExpression;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "simplify.expression";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.LITERAL_TRUE, TokenTypes.LITERAL_FALSE};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        // Return empty list to prevent user changing tokens in the
        // configuration.
        return new int[] {};
    }

    @Override
    public int[] getRequiredTokens()
    {
        return new int[] {TokenTypes.LITERAL_TRUE, TokenTypes.LITERAL_FALSE};
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        final DetailAST parent = ast.getParent();
        switch (parent.getType()) {
            case TokenTypes.NOT_EQUAL:
            case TokenTypes.EQUAL:
            case TokenTypes.LNOT:
            case TokenTypes.LOR:
            case TokenTypes.LAND:
                log(parent.getLineNo(), parent.getColumnNo(),
                    MSG_KEY);
                break;
            default:
                break;
        }
    }
}
