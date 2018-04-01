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
package edu.cmu.cs.vbc.prog.checkstyle.checks;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

import java.util.regex.Pattern;

/**
 * <p>
 * A check for TODO comments. To check for other patterns in Java comments, set
 * property format.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 *
 * <pre>
 * &lt;module name="TodoComment"/&gt;
 * </pre>
 * <p>
 * An example of how to configure the check for comments that contain
 * <code>WARNING</code> is:
 * </p>
 *
 * <pre>
 * &lt;module name="TodoComment"&gt;
 *    &lt;property name="format" value="WARNING"/&gt;
 * &lt;/module&gt;
 * </pre>
 * @author Oliver Burn
 * @author Baratali Izmailov
 * @version 1.0
 */
public class TodoCommentCheck
        extends Check
{
	@VConditional
	private static boolean TodoComment = true;
	
	@Override
	public boolean isEnabled() {
		return TodoComment;
	}
    /**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY = "todo.match";

    /**
     * Format of todo comment.
     */
    private String format = "TODO:";

    /**
     * Regular expression pattern compiled from format.
     */
    private Pattern regexp = Pattern.compile(format);

    @Override
    public boolean isCommentNodesRequired()
    {
        return true;
    }

    /**
     * Setter for todo comment format.
     * @param format format of todo comment.
     */
    public void setFormat(String format)
    {
        this.format = format;
        regexp = Pattern.compile(format);
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.COMMENT_CONTENT };
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return new int[] {TokenTypes.COMMENT_CONTENT };
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        final String[] lines = ast.getText().split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (regexp.matcher(lines[i]).find()) {
                log(ast.getLineNo() + i, MSG_KEY, format);
            }
        }
    }
}
