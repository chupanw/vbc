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

import edu.cmu.cs.vbc.prog.checkstyle.api.Check;
import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;
import edu.cmu.cs.vbc.prog.checkstyle.api.ScopeUtils;
import edu.cmu.cs.vbc.prog.checkstyle.api.TokenTypes;

import java.util.*;

/**
 * Abstract class for checks which need to collect information about
 * declared members/parameters/variables.
 *
 * @author o_sukhodolsky
 */
public abstract class DeclarationCollector extends Check
{
    /**
     * Tree of all the parsed frames
     */
    private Map<DetailAST, LexicalFrame> frames;

    /**
     * Frame for the currently processed AST
     */
    private LexicalFrame current;

    @Override
    public void beginTree(DetailAST rootAST)
    {
        final LinkedList<LexicalFrame> frameStack = new LinkedList<>();
        frameStack.add(new GlobalFrame());

        frames = new HashMap<>();

        DetailAST curNode = rootAST;
        while (curNode != null) {
            collectDeclarations(frameStack, curNode);
            DetailAST toVisit = curNode.getFirstChild();
            while (curNode != null && toVisit == null) {
                endCollectingDeclarations(frameStack, curNode);
                toVisit = curNode.getNextSibling();
                if (toVisit == null) {
                    curNode = curNode.getParent();
                }
            }
            curNode = toVisit;
        }
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        if (ast.getType() == TokenTypes.CLASS_DEF ||
                ast.getType() == TokenTypes.INTERFACE_DEF ||
                ast.getType() == TokenTypes.ENUM_DEF ||
                ast.getType() == TokenTypes.ANNOTATION_DEF ||
                ast.getType() == TokenTypes.SLIST ||
                ast.getType() == TokenTypes.METHOD_DEF ||
                ast.getType() == TokenTypes.CTOR_DEF) {
            this.current = this.frames.get(ast);
        } else {
            // do nothing
        }
    } // end visitToken

    /**
     * Parse the next AST for declarations
     *
     * @param frameStack Stack containing the FrameTree being built
     * @param ast AST to parse
     */
    private void collectDeclarations(LinkedList<LexicalFrame> frameStack,
        DetailAST ast)
    {
        final LexicalFrame frame = frameStack.peek();
        if (ast.getType() == TokenTypes.VARIABLE_DEF) {
            final String name =
                    ast.findFirstToken(TokenTypes.IDENT).getText();
            if (frame instanceof ClassFrame) {
                final DetailAST mods =
                        ast.findFirstToken(TokenTypes.MODIFIERS);
                if (ScopeUtils.inInterfaceBlock(ast)
                        || mods.branchContains(TokenTypes.LITERAL_STATIC))
                {
                    ((ClassFrame) frame).addStaticMember(name);
                }
                else {
                    ((ClassFrame) frame).addInstanceMember(name);
                }
            }
            else {
                frame.addName(name);
            }
        } else if (ast.getType() == TokenTypes.PARAMETER_DEF) {
            final DetailAST nameAST = ast.findFirstToken(TokenTypes.IDENT);
            frame.addName(nameAST.getText());
        } else if (ast.getType() == TokenTypes.CLASS_DEF ||
                ast.getType() == TokenTypes.INTERFACE_DEF ||
                ast.getType() == TokenTypes.ENUM_DEF ||
                ast.getType() == TokenTypes.ANNOTATION_DEF) {
            final DetailAST nameAST = ast.findFirstToken(TokenTypes.IDENT);
            frame.addName(nameAST.getText());
            frameStack.addFirst(new ClassFrame(frame));
        } else if (ast.getType() == TokenTypes.SLIST) {
            frameStack.addFirst(new BlockFrame(frame));
        } else if (ast.getType() == TokenTypes.METHOD_DEF) {
            final String name = ast.findFirstToken(TokenTypes.IDENT).getText();
            if (frame instanceof ClassFrame) {
                final DetailAST mods =
                        ast.findFirstToken(TokenTypes.MODIFIERS);
                if (mods.branchContains(TokenTypes.LITERAL_STATIC)) {
                    ((ClassFrame) frame).addStaticMethod(name);
                }
                else {
                    ((ClassFrame) frame).addInstanceMethod(name);
                }
            }
            frameStack.addFirst(new MethodFrame(frame));
        } else if (ast.getType() == TokenTypes.CTOR_DEF) {
            frameStack.addFirst(new MethodFrame(frame));
        } else {
            // do nothing
        }
    }


    /**
     * End parsing of the AST for declarations.
     *
     * @param frameStack Stack containing the FrameTree being built
     * @param ast AST that was parsed
     */
    private void endCollectingDeclarations(LinkedList<LexicalFrame> frameStack,
        DetailAST ast)
    {
        if (ast.getType() == TokenTypes.CLASS_DEF ||
                ast.getType() == TokenTypes.INTERFACE_DEF ||
                ast.getType() == TokenTypes.ENUM_DEF ||
                ast.getType() == TokenTypes.ANNOTATION_DEF ||
                ast.getType() == TokenTypes.SLIST ||
                ast.getType() == TokenTypes.METHOD_DEF ||
                ast.getType() == TokenTypes.CTOR_DEF) {
            this.frames.put(ast, frameStack.poll());
        } else {
            // do nothing
        }
    }

    /**
     * Check if given name is a name for class field in current environment.
     * @param name a name to check
     * @return true is the given name is name of member.
     */
    protected final boolean isClassField(String name)
    {
        final LexicalFrame frame = findFrame(name);
        return (frame instanceof ClassFrame)
                && ((ClassFrame) frame).hasInstanceMember(name);
    }

    /**
     * Check if given name is a name for class method in current environment.
     * @param name a name to check
     * @return true is the given name is name of method.
     */
    protected final boolean isClassMethod(String name)
    {
        final LexicalFrame frame = findFrame(name);
        return (frame instanceof ClassFrame)
                && ((ClassFrame) frame).hasInstanceMethod(name);
    }

    /**
     * Find frame containing declaration
     * @param name name of the declaration to find
     * @return LexicalFrame containing declaration or null
     */
    private LexicalFrame findFrame(String name)
    {
        if (current != null) {
            return current.getIfContains(name);
        }
        else {
            return null;
        }
    }

    /**
     * A declaration frame.
     * @author Stephen Bloch
     * June 19, 2003
     */
    private abstract static class LexicalFrame
    {
        /** Set of name of variables declared in this frame. */
        private final Set<String> varNames;
        /**
         * Parent frame.
         */
        private final LexicalFrame parent;

        /**
         * constructor -- invokable only via super() from subclasses
         *
         * @param parent parent frame
         */
        protected LexicalFrame(LexicalFrame parent)
        {
            this.parent = parent;
            varNames = new HashSet<>();
        }

        /** add a name to the frame.
         * @param nameToAdd  the name we're adding
         */
        void addName(String nameToAdd)
        {
            varNames.add(nameToAdd);
        }

        /** check whether the frame contains a given name.
         * @param nameToFind  the name we're looking for
         * @return whether it was found
         */
        boolean contains(String nameToFind)
        {
            return varNames.contains(nameToFind);
        }

        /** check whether the frame contains a given name.
         * @param nameToFind  the name we're looking for
         * @return whether it was found
         */
        LexicalFrame getIfContains(String nameToFind)
        {
            if (contains(nameToFind)) {
                return this;
            }
            else if (parent != null) {
                return parent.getIfContains(nameToFind);
            }
            else {
                return null;
            }
        }
    }

    /**
     * The global frame; should hold only class names.
     * @author Stephen Bloch
     */
    private static class GlobalFrame extends LexicalFrame
    {

        /**
         * Constructor for the root of the FrameTree
         */
        protected GlobalFrame()
        {
            super(null);
        }
    }

    /**
     * A frame initiated at method definition; holds parameter names.
     * @author Stephen Bloch
     */
    private static class MethodFrame extends LexicalFrame
    {
        /**
         * @param parent parent frame
         */
        protected MethodFrame(LexicalFrame parent)
        {
            super(parent);
        }
    }

    /**
     * A frame initiated at class definition; holds instance variable
     * names.  For the present, I'm not worried about other class names,
     * method names, etc.
     * @author Stephen Bloch
     */
    private static class ClassFrame extends LexicalFrame
    {
        /** Set of name of instance members declared in this frame. */
        private final Set<String> instanceMembers;
        /** Set of name of instance methods declared in this frame. */
        private final Set<String> instanceMethods;
        /** Set of name of variables declared in this frame. */
        private final Set<String> staticMembers;
        /** Set of name of static methods declared in this frame. */
        private final Set<String> staticMethods;

        /**
         * Creates new instance of ClassFrame
         * @param parent parent frame
         */
        public ClassFrame(LexicalFrame parent)
        {
            super(parent);
            instanceMembers = new HashSet<>();
            instanceMethods = new HashSet<>();
            staticMembers = new HashSet<>();
            staticMethods = new HashSet<>();
        }

        /**
         * Adds static member's name.
         * @param name a name of static member of the class
         */
        public void addStaticMember(final String name)
        {
            staticMembers.add(name);
        }

        /**
         * Adds static method's name.
         * @param name a name of static method of the class
         */
        public void addStaticMethod(final String name)
        {
            staticMethods.add(name);
        }

        /**
         * Adds instance member's name.
         * @param name a name of instance member of the class
         */
        public void addInstanceMember(final String name)
        {
            instanceMembers.add(name);
        }

        /**
         * Adds instance method's name.
         * @param name a name of instance method of the class
         */
        public void addInstanceMethod(final String name)
        {
            instanceMethods.add(name);
        }

        /**
         * Checks if a given name is a known instance member of the class.
         * @param name a name to check
         * @return true is the given name is a name of a known
         *         instance member of the class
         */
        public boolean hasInstanceMember(final String name)
        {
            return instanceMembers.contains(name);
        }

        /**
         * Checks if a given name is a known instance method of the class.
         * @param name a name to check
         * @return true is the given name is a name of a known
         *         instance method of the class
         */
        public boolean hasInstanceMethod(final String name)
        {
            return instanceMethods.contains(name);
        }

        @Override
        boolean contains(String nameToFind)
        {
            return super.contains(nameToFind)
                    || instanceMembers.contains(nameToFind)
                    || instanceMethods.contains(nameToFind)
                    || staticMembers.contains(nameToFind)
                    || staticMethods.contains(nameToFind);
        }
    }

    /**
     * A frame initiated on entering a statement list; holds local variable
     * names.  For the present, I'm not worried about other class names,
     * method names, etc.
     * @author Stephen Bloch
     */
    private static class BlockFrame extends LexicalFrame
    {

        /**
         * @param parent parent frame
         */
        protected BlockFrame(LexicalFrame parent)
        {
            super(parent);
        }
    }
}
