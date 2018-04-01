package edu.cmu.cs.vbc.prog.checkstyle.gui;

import edu.cmu.cs.vbc.prog.checkstyle.api.DetailAST;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CodeSelector
{
    private final DetailAST ast;
    private final JTextArea editor;
    private final List<Integer> lines2position;

    public CodeSelector(final DetailAST ast, final JTextArea editor,
                        final List<Integer> lines2position)
    {
        this.ast = ast;
        this.editor = editor;
        this.lines2position = lines2position;
    }

    public void select() {
        int start = lines2position.get(ast.getLineNo()) + ast.getColumnNo();
        int end = findLastPosition(ast);

        editor.setSelectedTextColor(Color.blue);
        editor.requestFocusInWindow();
        editor.setSelectionStart(start);
        editor.setSelectionEnd(end);
        editor.transferFocusBackward();
    }

    private int findLastPosition(final DetailAST ast)
    {
        if (ast.getChildCount() == 0)
        {
            return lines2position.get(ast.getLineNo()) + ast.getColumnNo()
                + ast.getText().length();
        }
        else
        {
            return findLastPosition(ast.getLastChild());
        }
    }
}
