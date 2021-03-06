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
 * Checks if array initialization contains optional trailing comma.
 * </p>
 * <p>
 * Rationale: Putting this comma in make is easier to change the
 * order of the elements or add new elements on the end.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="ArrayTrailingComma"/&gt;
 * </pre>
 * @author o_sukhodolsky
 */
public class ArrayTrailingCommaCheck extends Check
{
	@VConditional
	private static boolean ArrayTrailingComma = true;
	
	@Override
	public boolean isEnabled() {
		return ArrayTrailingComma;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "array.trailing.comma";

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.ARRAY_INIT};
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.ARRAY_INIT};
    }

    @Override
    public void visitToken(DetailAST arrayInit)
    {
        final DetailAST rcurly = arrayInit.findFirstToken(TokenTypes.RCURLY);

        // if curlys are on the same line
        // or array is empty then check nothing
        if ((arrayInit.getLineNo() == rcurly.getLineNo())
            || (arrayInit.getChildCount() == 1))
        {
            return;
        }

        final DetailAST prev = rcurly.getPreviousSibling();
        if (prev.getType() != TokenTypes.COMMA) {
            log(rcurly.getLineNo(), MSG_KEY);
        }
    }
}
