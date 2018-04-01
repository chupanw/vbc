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
package edu.cmu.cs.vbc.prog.checkstyle.checks.metrics;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

import java.math.BigInteger;

/**
 * Checks the npath complexity against a specified limit (default = 200).
 * The npath metric computes the number of possible execution paths
 * through a function. Similar to the cyclomatic complexity but also
 * takes into account the nesting of conditional statements and
 * multi-part boolean expressions.
 *
 * @author <a href="mailto:simon@redhillconsulting.com.au">Simon Harris</a>
 * @author o_sukhodolsky
 * TODO: For every or: _value += (_orCount * (nestedValue - 1));
 * TODO: For every and: ???
 */
public final class NPathComplexityCheck extends AbstractComplexityCheck
{

	@VConditional
	private static boolean NPathComplexity = true;
	
	@Override
	public boolean isEnabled() {
		return NPathComplexity;
	}
	
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "npathComplexity";

    /** Default allowed complexity. */
    private static final int DEFAULT_MAX = 200;

    /** Creates new instance of the check. */
    public NPathComplexityCheck()
    {
        super(DEFAULT_MAX);
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.STATIC_INIT,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_ELSE,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.QUESTION,
        };
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.STATIC_INIT,
            TokenTypes.INSTANCE_INIT,
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_FOR,
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_ELSE,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_CASE,
            TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.QUESTION,
        };
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        if (ast.getType() == TokenTypes.LITERAL_WHILE ||
                ast.getType() == TokenTypes.LITERAL_DO ||
                ast.getType() == TokenTypes.LITERAL_FOR ||
                ast.getType() == TokenTypes.LITERAL_IF ||
                ast.getType() == TokenTypes.QUESTION ||
                ast.getType() == TokenTypes.LITERAL_TRY ||
                ast.getType() == TokenTypes.LITERAL_SWITCH) {
            visitMultiplyingConditional();
        } else if (ast.getType() == TokenTypes.LITERAL_ELSE ||
                ast.getType() == TokenTypes.LITERAL_CATCH ||
                ast.getType() == TokenTypes.LITERAL_CASE) {
            visitAddingConditional();
        } else {
            super.visitToken(ast);
        }
    }

    @Override
    public void leaveToken(DetailAST ast)
    {
        if (ast.getType() == TokenTypes.LITERAL_WHILE ||
                ast.getType() == TokenTypes.LITERAL_DO ||
                ast.getType() == TokenTypes.LITERAL_FOR ||
                ast.getType() == TokenTypes.LITERAL_IF ||
                ast.getType() == TokenTypes.QUESTION ||
                ast.getType() == TokenTypes.LITERAL_TRY ||
                ast.getType() == TokenTypes.LITERAL_SWITCH) {
            leaveMultiplyingConditional();
        } else if (ast.getType() == TokenTypes.LITERAL_ELSE ||
                ast.getType() == TokenTypes.LITERAL_CATCH ||
                ast.getType() == TokenTypes.LITERAL_CASE) {
            leaveAddingConditional();
        } else {
            super.leaveToken(ast);
        }
    }

    @Override
    protected String getMessageID()
    {
        return MSG_KEY;
    }

    /** Visits else, catch or case. */
    private void visitAddingConditional()
    {
        pushValue();
    }

    /** Leaves else, catch or case. */
    private void leaveAddingConditional()
    {
        setCurrentValue(
                getCurrentValue().subtract(BigInteger.ONE).add(popValue()));
    }

    /** Visits while, do, for, if, try, ? (in ?::) or switch. */
    private void visitMultiplyingConditional()
    {
        pushValue();
    }

    /** Leaves while, do, for, if, try, ? (in ?::) or switch. */
    private void leaveMultiplyingConditional()
    {
        setCurrentValue(
                getCurrentValue().add(BigInteger.ONE).multiply(popValue()));
    }
}
