package edu.cmu.cs.vbc.prog.prevayler.demos.scalability.prevayler;

import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.Record;
import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.TransactionConnection;
import org.prevayler.Prevayler;

class PrevaylerTransactionConnection implements TransactionConnection {

	private final Prevayler	prevayler;

	PrevaylerTransactionConnection(Prevayler prevayler) {
		this.prevayler = prevayler;
	}

	public void performTransaction(Record recordToInsert, Record recordToUpdate, long idToDelete) {
		try {

			prevayler.execute(new TestTransaction(recordToInsert, recordToUpdate, idToDelete));

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Unexpected Exception: " + ex);
		}
	}
}
