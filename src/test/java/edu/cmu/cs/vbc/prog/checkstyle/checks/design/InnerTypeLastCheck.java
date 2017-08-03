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
package edu.cmu.cs.vbc.prog.checkstyle.checks.design;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.ScopeUtils;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * <p>
 * Check nested (internal) classes/interfaces are declared at the bottom of the
 * class after all method and field declarations.
 * </p>
 *
 * @author <a href="mailto:ryly@mail.ru">Ruslan Dyachenko</a>
 */
public class InnerTypeLastCheck extends Check
{
	@VConditional
	private static boolean InnerTypeLast = true;
	
	@Override
	public boolean isEnabled() {
		return InnerTypeLast;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "arrangement.members.before.inner";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.INTERFACE_DEF};
    }

    /** Meet a root class. */
    private boolean rootClass = true;

    @Override
    public void visitToken(DetailAST ast)
    {
        /** First root class */
        if (rootClass) {
            rootClass = false;
        }
        else {
            DetailAST nextSibling = ast.getNextSibling();
            while (null != nextSibling) {
                if (!ScopeUtils.inCodeBlock(ast)
                    && (nextSibling.getType() == TokenTypes.VARIABLE_DEF
                        || nextSibling.getType() == TokenTypes.METHOD_DEF))
                {
                    log(nextSibling.getLineNo(), nextSibling.getColumnNo(),
                        MSG_KEY);
                }
                nextSibling = nextSibling.getNextSibling();
            }
        }
    }

    @Override
    public void leaveToken(DetailAST ast)
    {
        /** Is this a root class */
        if (null == ast.getParent()) {
            rootClass = true;
        }
    }
}
