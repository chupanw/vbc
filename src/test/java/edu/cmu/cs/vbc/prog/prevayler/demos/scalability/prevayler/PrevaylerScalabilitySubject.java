package edu.cmu.cs.vbc.prog.prevayler.demos.scalability.prevayler;

import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.ScalabilityTestSubject;
import org.prevayler.Prevayler;

abstract class PrevaylerScalabilitySubject implements ScalabilityTestSubject {

	protected Prevayler prevayler;

	
	{System.gc();}


	public String name() { return "Prevayler"; }


	public void replaceAllRecords(int records) {
		try {

			prevayler.execute(new AllRecordsReplacement(records));

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException("Unexpected Exception: " + ex);
		}
	}

}
