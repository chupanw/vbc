package edu.cmu.cs.vbc.prog.prevayler.demos.scalability.prevayler;

import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.RecordIterator;

interface ScalabilitySystem extends java.io.Serializable {

	void replaceAllRecords(RecordIterator newRecords);

}
