package com.atrina.executionTracer;

public class DOMOutPut {
	
	private String node;
	private String line;
	private String value;
	
	public DOMOutPut(String node, String line, String value){
		this.node=node;
		this.line=line;
		this.value=value;
	}
	
	public String getNode(){
		return node;
	}
	public String getLine(){
		return line;
	}
	public String getValue(){
		return value;
	}

}
