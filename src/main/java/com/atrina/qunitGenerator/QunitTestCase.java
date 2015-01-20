package com.atrina.qunitGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atrina.executionTracer.AstInstrumenter.variableUsageType;
import com.atrina.oracle.AccessedDOMNode;
import com.atrina.oracle.FunctionPoint;
import com.atrina.oracle.Oracle;
import com.atrina.oracle.Variable;
import com.atrina.qunitGenerator.QunitAssertion.AssertionType;
import com.google.common.collect.Sets;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class QunitTestCase {
	
	private String testCaseCode="";
	private String testCaseName="";
	private String functionName="";
	private FunctionPoint functionEntryPoint;
	private List<QunitAssertion> qunitAssertions=new ArrayList<QunitAssertion>();
	List<Oracle> oracles=new ArrayList<Oracle>();
	public QunitTestCase(List<Oracle> oracleList, FunctionPoint functionEntry, String funcName){
		
		this.functionName=funcName;
		oracles=oracleList;
		if(oracleList.size()>0){
		
			Set<Variable> exitVars;
			Set<AccessedDOMNode> domNodes;
			if(oracles.size()>1){
				createQunitTestCaseWithCombinedAssertions(oracleList, functionEntry, funcName);
			}
			else{ 
				
				boolean thisKeyWordInBodyOfFunction=false;
				exitVars=oracles.get(0).getVariables();
				domNodes=oracles.get(0).getAccessedDomNodes();			
				functionEntryPoint=functionEntry;
				String[] funcAndScope=funcName.split("\\.");
				functionName=funcAndScope[funcAndScope.length-1];
				testCaseName="\"" + "Testing " + this.functionName  + "\"";
				
				ArrayList<Variable> entryVars=functionEntry.getVariables();
				testCaseCode+="\t";
				for(Variable entryVar:entryVars){
			
					if(entryVar.getVariableUsage().equals(variableUsageType.global.toString()) ||
							entryVar.getVariableUsage().equals(variableUsageType.inputParam.toString())){
						if(entryVar.getVariableName().equals("this")){
							if(entryVar.getValue().contains("evaluate")){
								thisKeyWordInBodyOfFunction=true;
								String entryVarVal=entryVar.getValue().replaceFirst("\"", "").replaceAll("\\\\\"", "\"");
								String entryVarName="var thisVar=";
								testCaseCode+= entryVarName + entryVarVal.substring(0,entryVarVal.length()-1) + ";" + "\n" +"\t";
								testCaseCode+="thisVar.on("+"\"click\""+ "," + this.functionName + ");" + "\n" + "\t";
								
							}
						}
						else{
							if(entryVar.getVariableUsage().equals(variableUsageType.inputParam.toString())){
								if(entryVar.getType().contains("object")){
									testCaseCode+= "var " + entryVar.getVariableName() + "= " + "JSON.retrocycle(JSON.parse("  +
											entryVar.getValue() + ")" + ")" + ";" + "\n" +"\t";
								}
								else{
									testCaseCode+= "var " + entryVar.getVariableName() + "= " +
											entryVar.getValue() + ";" + "\n" +"\t";
								}
								
							}
							else{
								if(entryVar.getType().contains("object")){
						
									testCaseCode+= entryVar.getVariableName() + "= " + "JSON.retrocycle(JSON.parse("  +
										entryVar.getValue() + ")" + ")" + ";" + "\n" +"\t";
							
								}
								else{
									testCaseCode+= entryVar.getVariableName() + "= " +
											entryVar.getValue() + ";" + "\n" +"\t";
								
								}
							}
						}
					}
				}
				testCaseCode+="var result= ";
				
				if(thisKeyWordInBodyOfFunction){
					testCaseCode+="thisVar" + "." + "trigger" + "(" + "\"click\"" + ");";
				}
				else{
					
					testCaseCode+=this.functionName;
					
				}
				testCaseCode += "\n" + "\t";
				
				
	
				for(Variable exitVar:exitVars){
					String varUsage=exitVar.getVariableUsage();
					
						
					if(varUsage.equals(variableUsageType.returnVal.toString())){
						if(exitVar.getValue().equals("[]")){
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
							qunitAssertionForValueChecking.makeQunitAssertionForVariable("result.length==0","0", AssertionType.ok);
							qunitAssertions.add(qunitAssertionForValueChecking);
						}
						else{
							
							QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
							String newVarName= exitVar.getVariableName() + "_returnedVal";
							testCaseCode+="var " + newVarName + "= " + 	exitVar.getValue() + ";" + "\n" + "\t";
							if(exitVar.getType().contains("object")){

								testCaseCode+="var " + "actual_returnedVal" + "= " + "JSON.stringify(JSON.decycle(result),includeFunctions);"  + "\n" + "\t";
								qunitAssertionForValueChecking.makeQunitAssertionForVariable("actual_returnedVal", newVarName, AssertionType.deepEqual);
							}
							else{
							
								qunitAssertionForValueChecking.makeQunitAssertionForVariable("result", newVarName, AssertionType.deepEqual);
							}
							qunitAssertions.add(qunitAssertionForValueChecking);			
							
						}
							
						String actualType="getType(result)" + " == " + "'" + exitVar.getType() + "'"; 
						QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
						qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType, exitVar.getType(), AssertionType.ok);
						qunitAssertions.add(qunitAssertionForTypeChecking);
					}
					else{
							
							
						if(varUsage.equals(variableUsageType.global.toString())){
								
							String actual=exitVar.getVariableName();
							if(exitVar.getValue().equals("[]")){
								actual+= ".length==0";
								QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
								qunitAssertionForValueChecking.makeQunitAssertionForVariable(actual ,"0", AssertionType.ok);
								qunitAssertions.add(qunitAssertionForValueChecking);
							}
							else{
								
									
								String newVarName= exitVar.getVariableName() + "_exitVar";
								QunitAssertion qunitAssertionForValueChecking=new QunitAssertion();
								testCaseCode+="var " + newVarName + "= " +	exitVar.getValue() + ";" + "\n" + "\t";
								if(exitVar.getType().contains("object")){

									String newActualVarName=exitVar.getVariableName() + "_actual_exitVal";
									testCaseCode+="var " + newActualVarName + "= " + "JSON.stringify(JSON.decycle(actual),includeFunctions);"  + "\n" + "\t";
									qunitAssertionForValueChecking.makeQunitAssertionForVariable(newActualVarName, newVarName, AssertionType.deepEqual);
								}
								
								else{
									qunitAssertionForValueChecking.makeQunitAssertionForVariable(actual, newVarName, AssertionType.deepEqual);
									
								}
								
								qunitAssertions.add(qunitAssertionForValueChecking);
								
							
							}
							actual=exitVar.getVariableName();	
							String actualType="getType"+ "(" + actual + ")" + " == " + "'" + exitVar.getType() + "'"; 
							QunitAssertion qunitAssertionForTypeChecking=new QunitAssertion();
							qunitAssertionForTypeChecking.makeQunitAssertionForVariable(actualType,exitVar.getType(), AssertionType.ok);
							qunitAssertions.add(qunitAssertionForTypeChecking);
						}
							
					}
				}
					
				int counter=0;
				for(AccessedDOMNode domNode:domNodes){
					counter++;
					QunitAssertion qunitAssertionForDomChecking=new QunitAssertion();
					qunitAssertionForDomChecking.makeQunitAssertionForDomNode(domNode, counter);
					qunitAssertions.add(qunitAssertionForDomChecking);
				}
					
					
					
				int numberofExpectedAssertions=0;
				for(QunitAssertion qunitAssertion:qunitAssertions){
					String assertionCode=qunitAssertion.getAssertionCodeForVariable() + "\n" + "\t";
					assertionCode+=qunitAssertion.getAssertionCodeForDom();
					testCaseCode+=assertionCode;
					testCaseCode+="\n" + "\t";
					numberofExpectedAssertions+=qunitAssertion.getTotalNumberOfAssertions();
					
				}
				
				
				String testCodeSetup="test"+"(" + testCaseName + "," + numberofExpectedAssertions + ","
						+ "function()" +"{" +"\n" + "\t";
				if(!functionEntry.getDomHtml().equals("")){
					String domHtml=functionEntry.getDomHtml();
					if(domHtml.startsWith("[\"") && domHtml.endsWith("\"]")){
						domHtml=domHtml.substring(2, domHtml.length()-2);
					}
					String qunitFixture="var fixture = $(\"#qunit-fixture\");" + "\n" + "\t";
					qunitFixture+="fixture.append"+ "(" + "\""  + domHtml  + "\"" + ")"+ ";" + "\n";
					testCodeSetup+=qunitFixture;
				}
				testCaseCode=testCodeSetup.concat(testCaseCode);
				testCaseCode+= "});";
			}
		}
		
	}
	
	public String getTestCaseCode(){
		return testCaseCode;
	}
	
	public String getTestCaseName(){
		return testCaseName;
	}
	
	public String getfunctionName(){
		return functionName;
	}
	
	public List<QunitAssertion> getQunitAssertions(){
		return qunitAssertions;
	}
	
	public List<Oracle> getOracles(){
		return oracles;
	}
	
	public FunctionPoint getFunctionEntryPoint(){
		return functionEntryPoint;
	}
	
	private Set<Variable> getCommonVariablesAmongOracleList(){
		
		Set<Variable> commonVars=oracles.get(0).getVariables();
	
		for(int i=1;i<oracles.size();i++){
			Set<Variable> varList=oracles.get(i).getVariables();
			commonVars=com.google.common.collect.Sets.intersection(commonVars, varList);
		}
		
		return commonVars;
		
	}
	
	private Set<AccessedDOMNode> getCommonAccessedDomAmongOracleList(){
	
		Set<AccessedDOMNode> commonAccessedDom=oracles.get(0).getAccessedDomNodes();
	
		for(int i=1;i<oracles.size();i++){
			Set<AccessedDOMNode> accessedDomList=oracles.get(i).getAccessedDomNodes();
			commonAccessedDom=com.google.common.collect.Sets.intersection(commonAccessedDom, accessedDomList);
		}
		
		return commonAccessedDom;
		
	}
	
	private void createQunitTestCaseWithCombinedAssertions(List<Oracle> oracleList, FunctionPoint functionEntry, String funcName){
		boolean thisKeyWordInBodyOfFunction=false;
		functionEntryPoint=functionEntry;
		String[] funcAndScope=funcName.split("\\.");
		functionName=funcAndScope[funcAndScope.length-1];
		testCaseName="\"" + "Testing " + this.functionName  + "\"";
		
		ArrayList<Variable> entryVars=functionEntry.getVariables();
		testCaseCode+="\t";
		for(Variable entryVar:entryVars){
			if(entryVar.getVariableUsage().equals(variableUsageType.global.toString()) ||
					entryVar.getVariableUsage().equals(variableUsageType.inputParam.toString())){
				
				
				if(entryVar.getVariableName().equals("this")){
					if(entryVar.getValue().contains("evaluate")){
						thisKeyWordInBodyOfFunction=true;
						String entryVarVal=entryVar.getValue().replaceFirst("\"", "").replaceAll("\\\\\"", "\"");
						String entryVarName="var thisVar=";
						testCaseCode+= entryVarName + entryVarVal.substring(0,entryVarVal.length()-1) + ";" + "\n" +"\t";
						testCaseCode+="thisVar.on("+"\"click\""+ "," + this.functionName + ");" + "\n" + "\t";
					}
				}
				else{
					if(entryVar.getVariableUsage().equals(variableUsageType.inputParam.toString())){
						testCaseCode+= "var " + entryVar.getVariableName() + "= " + entryVar.getValue() + ";" + "\n" +"\t";
					}
					
					else
						testCaseCode+= entryVar.getVariableName() + "= " + entryVar.getValue() + ";" + "\n" +"\t";
				
					}
			}
		}
		testCaseCode+="var result= ";
		if(thisKeyWordInBodyOfFunction){
			testCaseCode+="thisVar" + "." + "trigger" + "(" + "\"click\"" + ");";
		}
		else{
			testCaseCode+=this.functionName + "(";
			for(Variable entryVar:entryVars){
				String varUsage=entryVar.getVariableUsage();
				if(varUsage.equals(variableUsageType.inputParam.toString())){
					String varName=entryVar.getVariableName();
					testCaseCode+=varName + ", ";
				}
			}
			if(testCaseCode.endsWith(", ")){
				testCaseCode=testCaseCode.substring(0, testCaseCode.length()-2);
			}
	
			testCaseCode+=")" + ";";
		}
		testCaseCode += "\n" + "\t";
		
		
		CombinedAssertions combinedAssertions=new CombinedAssertions();
		for(Oracle oracle:oracleList){
				Set<Variable> exitVars=oracle.getVariables();
				Set<AccessedDOMNode> accessedDomNodes=oracle.getAccessedDomNodes();
				IndividualAssertions individualAssertion=new IndividualAssertions();
			for(Variable exitVar:exitVars){
				String varUsage=exitVar.getVariableUsage();
				
				if(varUsage.equals(variableUsageType.returnVal.toString())){
					if(exitVar.getValue().equals("[]")){
						
						individualAssertion.addIndividualAssertions("result.length","0", accessedDomNodes);
	
					}
					else{
					
						if(exitVar.getType().contains("array")){
							String newVarName= exitVar.getVariableName() + "Array_qunitTest";
							testCaseCode+="var " + newVarName + "= "+ exitVar.getValue() + ";" +"\n" + "\t";
							String actual="true";
							String expected="areEqualArrays" + "(result, " + newVarName + ")";
							individualAssertion.addIndividualAssertions(expected, actual, accessedDomNodes);
						}
						else{
							individualAssertion.addIndividualAssertions("result", exitVar.getValue(), accessedDomNodes);
						}
					}
						
					String actualType="getType(result)"; 
					String expected="'" + exitVar.getType() + "'";
					individualAssertion.addIndividualAssertions(actualType, expected, accessedDomNodes);
	
				}
				else{
						
						
					if(varUsage.equals(variableUsageType.global.toString())){
							
						String actual=exitVar.getVariableName();
						if(exitVar.getValue().equals("[]")){
							actual+= ".length";
							individualAssertion.addIndividualAssertions(actual, "0", accessedDomNodes);
	
						}
						else{
							
							
							if(exitVar.getType().contains("array")){
								String newVarName= exitVar.getVariableName() + "Array_qunitTest";
								testCaseCode+="var " + newVarName + "= "+ exitVar.getValue() + ";" + "\n" +"\t";
								String actualVal="true";
								String expected="areEqualArrays" + "(" + exitVar.getVariableName() +", " + newVarName + ")";
								individualAssertion.addIndividualAssertions(expected, actualVal, accessedDomNodes);
							}
							else
								individualAssertion.addIndividualAssertions(actual, exitVar.getValue(), accessedDomNodes);
	
						}
							
						String actualType="getType"+ "(" + actual + ")";
						String expected= "'" + exitVar.getType() + "'";
						individualAssertion.addIndividualAssertions(actualType, expected, accessedDomNodes);
	
					}
						
				}
				
			}
			combinedAssertions.addIndividualAssertions(individualAssertion);

			
			
		}
		
		QunitAssertion combinedQunitAssertion=new QunitAssertion();
		combinedQunitAssertion.makeCombinedQunitAssertion(combinedAssertions);
		qunitAssertions.add(combinedQunitAssertion);
		
		int numberofExpectedAssertions=0;
		for(QunitAssertion qunitAssertion:qunitAssertions){
			String assertionCode=qunitAssertion.getAssertionCodeForVariable() + "\n" + "\t";
			assertionCode+=qunitAssertion.getAssertionCodeForDom();
			testCaseCode+=assertionCode;
			testCaseCode+="\n" + "\t";
			numberofExpectedAssertions+=qunitAssertion.getTotalNumberOfAssertions();
			
		}
		
		
		String testCodeSetup="test"+"(" + testCaseName + "," + numberofExpectedAssertions + ","
				+ "function()" +"{" +"\n" + "\t";
		if(!functionEntry.getDomHtml().equals("")){
			String domHtml=functionEntry.getDomHtml();
			if(domHtml.startsWith("[\"") && domHtml.endsWith("\"]")){
				domHtml=domHtml.substring(2, domHtml.length()-2);
			}
			String qunitFixture="var fixture = $(\"#qunit-fixture\");" + "\n" + "\t";
			qunitFixture+="fixture.append"+ "(" + "\""  + domHtml  + "\"" + ")"+ ";" + "\n";
			testCodeSetup+=qunitFixture;
		}
		testCaseCode=testCodeSetup.concat(testCaseCode);
		testCaseCode+= "});";
	}
	
	

}
