/*
 * TaskList.java
 * Copyright (c) 2004 The Prevayler Team
 * 
 * See distributed LICENSE file for licensing details.
 */
package edu.cmu.cs.vbc.prog.prevayler.tutorial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// START SNIPPET: tasklist
public class TaskList implements Serializable {

    private List tasks = new ArrayList();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public List getTasks() {
        return Collections.unmodifiableList(tasks);
    }
}
// END SNIPPET: tasklist