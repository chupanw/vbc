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

import com.google.common.collect.Sets;
import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.FullIdent;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;
import edu.cmu.cs.vbc.prog.checkstyle.checks.CheckUtils;

import java.util.Set;

/**
 * <p>Checks that if a class defines a covariant method equals,
 * then it defines method equals(java.lang.Object).
 * Inspired by findbugs,
 * http://findbugs.sourceforge.net/bugDescriptions.html#EQ_SELF_NO_OBJECT
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="CovariantEquals"/&gt;
 * </pre>
 * @author Rick Giles
 * @version 1.0
 */
public class CovariantEqualsCheck extends Check
{
	@VConditional
	private static boolean CovariantEquals = true;
	
	@Override
	public boolean isEnabled() {
		return CovariantEquals;
	}
		
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "covariant.equals";

    /** Set of equals method definitions */
    private final Set<DetailAST> equalsMethods = Sets.newHashSet();

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.LITERAL_NEW, };
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.LITERAL_NEW, };
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        equalsMethods.clear();
        boolean hasEqualsObject = false;

        // examine method definitions for equals methods
        final DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock != null) {
            DetailAST child = objBlock.getFirstChild();
            while (child != null) {
                if (child.getType() == TokenTypes.METHOD_DEF
                        && CheckUtils.isEqualsMethod(child))
                {
                    if (hasObjectParameter(child)) {
                        hasEqualsObject = true;
                    }
                    else {
                        equalsMethods.add(child);
                    }
                }
                child = child.getNextSibling();
            }

            // report equals method definitions
            if (!hasEqualsObject) {
                for (DetailAST equalsAST : equalsMethods) {
                    final DetailAST nameNode = equalsAST
                            .findFirstToken(TokenTypes.IDENT);
                    log(nameNode.getLineNo(), nameNode.getColumnNo(),
                            MSG_KEY);
                }
            }
        }
    }

    /**
     * Tests whether a method definition AST has exactly one
     * parameter of type Object.
     * @param ast the method definition AST to test.
     * Precondition: ast is a TokenTypes.METHOD_DEF node.
     * @return true if ast has exactly one parameter of type Object.
     */
    private boolean hasObjectParameter(DetailAST ast)
    {
        // one parameter?
        final DetailAST paramsNode = ast.findFirstToken(TokenTypes.PARAMETERS);
        if (paramsNode.getChildCount() != 1) {
            return false;
        }

        // parameter type "Object"?
        final DetailAST paramNode =
            paramsNode.findFirstToken(TokenTypes.PARAMETER_DEF);
        final DetailAST typeNode = paramNode.findFirstToken(TokenTypes.TYPE);
        final FullIdent fullIdent = FullIdent.createFullIdentBelow(typeNode);
        final String name = fullIdent.getText();
        return ("Object".equals(name) || "java.lang.Object".equals(name));
    }
}
