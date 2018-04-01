package edu.cmu.cs.vbc.prog.prevayler.demos.scalability.prevayler;

import edu.cmu.cs.vbc.prog.prevayler.demos.scalability.QueryConnection;

import java.util.List;

class PrevaylerQueryConnection implements QueryConnection {

	private final QuerySystem querySystem;


	PrevaylerQueryConnection(QuerySystem querySystem) {
		this.querySystem = querySystem;
	}


	public List queryByName(String name) {
		return querySystem.queryByName(name);
	}
}
