package edu.cmu.cs.vbc.prog.prevayler.demos.scalability.prevayler;

import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.RecordIterator;
import org.prevayler.Transaction;

import java.util.Date;

class AllRecordsReplacement implements Transaction {

	private static final long serialVersionUID = 6283032417365727408L;
	private final int _records;

	AllRecordsReplacement(int records) { _records = records; }

	public void executeOn(Object system, Date ignored) {
		((ScalabilitySystem)system).replaceAllRecords(new RecordIterator(_records));
	}
}
