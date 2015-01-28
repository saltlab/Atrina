package com.atrina.core.trace;

import java.util.ArrayList;

public class VariableRead extends RWOperation {
	private String value;
	private String definingFunction;
	public String type = "Read";
	private ArrayList<RWOperation> dataDependencies = new ArrayList<RWOperation>();

	public String getValue() {
		return value;
	}

	public void setValue(String o) {
		value = o;
	}
	
	public String getDefiningFunction() {
		return definingFunction;
	}

	public void setDefiningFunction(String o) {
		definingFunction = o;
	}
	
	public void addDataDependency(RWOperation a) {
		dataDependencies.add(a);
	}

	public void addDataDependencies(ArrayList<RWOperation> as) {
		dataDependencies.addAll(as);
	}

	public ArrayList<RWOperation> getDataDependencies(String o) {
		return dataDependencies;
	}
}
