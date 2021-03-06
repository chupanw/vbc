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
package edu.cmu.cs.vbc.prog.checkstyle.checks.indentation;

import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * Handler for else blocks.
 *
 * @author jrichard
 */
public class ElseHandler extends BlockParentHandler
{
    /**
     * Construct an instance of this handler with the given indentation check,
     * abstract syntax tree, and parent handler.
     *
     * @param indentCheck   the indentation check
     * @param ast           the abstract syntax tree
     * @param parent        the parent handler
     */
    public ElseHandler(IndentationCheck indentCheck,
        DetailAST ast, ExpressionHandler parent)
    {
        super(indentCheck, "else", ast, parent);
    }

    @Override
    protected void checkToplevelToken()
    {
        // check if else is nested with rcurly of if:
        //
        //  } else ...

        final DetailAST ifAST = getMainAst().getParent();
        if (ifAST != null) {
            final DetailAST slist = ifAST.findFirstToken(TokenTypes.SLIST);
            if (slist != null) {
                final DetailAST lcurly = slist.getLastChild();
                if ((lcurly != null)
                    && (lcurly.getLineNo() == getMainAst().getLineNo()))
                {
                    // indentation checked as part of LITERAL IF check
                    return;
                }
            }
        }
        super.checkToplevelToken();
    }

    @Override
    protected DetailAST getNonlistChild()
    {
        return getMainAst().getFirstChild();
    }
}
