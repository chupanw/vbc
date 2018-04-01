/*
 * AddTask.java
 * Copyright (c) 2004 The Prevayler Team
 * 
 * See distributed LICENSE file for licensing details.
 */
package edu.cmu.cs.vbc.prog.prevayler.tutorial;

import org.prevayler.TransactionWithQuery;

import java.util.Date;

// START SNIPPET: addtask
public class AddTask implements TransactionWithQuery {

    private final Task task;

    public AddTask(String description, int priority) {
        this.task = new Task(description, priority);
    }

    public Object executeAndQuery(Object prevalentSystem, Date executionTime) throws Exception {

        TaskList system = (TaskList) prevalentSystem;
        system.addTask(task);

        return task;
    }
}
// END SNIPPET: addtask
