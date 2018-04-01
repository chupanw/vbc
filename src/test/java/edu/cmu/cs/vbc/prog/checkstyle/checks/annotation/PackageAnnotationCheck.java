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
package edu.cmu.cs.vbc.prog.checkstyle.checks.annotation;

import edu.cmu.cs.varex.annotation.VConditional;
import edu.cmu.cs.vbc.prog.checkstyle.api.AnnotationUtility;
import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

/**
 * This check makes sure that all package annotations are in the
 * package-info.java file.
 *
 * <p>
 * According to the Java JLS 3rd ed.
 * </p>
 *
 * <p>
 * The JLS does not enforce the placement of package annotations.
 * This placement may vary based on implementation.  The JLS
 * does highly recommend that all package annotations are
 * placed in the package-info.java file.
 *
 * See <a
 * href="http://java.sun.com/docs/books/jls/third_edition/html/j3TOC.html">
 * Java Language specification, sections 7.4.1.1</a>.
 * </p>
 * @author Travis Schneeberger
 */
public class PackageAnnotationCheck extends Check
{
	@VConditional
	private static boolean PackageAnnotation = true;
	
	@Override
	public boolean isEnabled() {
		return PackageAnnotation;
	}
    /** {@inheritDoc} */
    @Override
    public int[] getDefaultTokens()
    {
        return this.getRequiredTokens();
    }

    /** {@inheritDoc} */
    @Override
    public int[] getRequiredTokens()
    {
        return new int[] {
            TokenTypes.PACKAGE_DEF,
        };
    }

    /** {@inheritDoc} */
    @Override
    public int[] getAcceptableTokens()
    {
        return this.getRequiredTokens();
    }

    /** {@inheritDoc} */
    @Override
    public void visitToken(final DetailAST ast)
    {
        final boolean containsAnnotation =
            AnnotationUtility.containsAnnotation(ast);
        final boolean inPackageInfo =
            this.getFileContents().inPackageInfo();

        if (containsAnnotation && !inPackageInfo) {
            this.log(ast.getLine(), "annotation.package.location");
        }
    }
}
