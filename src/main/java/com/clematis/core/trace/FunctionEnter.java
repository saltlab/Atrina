package com.clematis.core.trace;

import java.util.Vector;

import org.codehaus.jackson.annotate.JsonSetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FunctionEnter extends FunctionTrace {
	private String TargetFunction;
	private JSONArray args = new JSONArray();
	private Vector<Integer> relatedAssertions = new Vector<Integer>();
	String scopeName;

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String sn) {
		this.scopeName = sn;
	}

	public String getTargetFunction() {
		String justName = TargetFunction.trim().replaceAll(" ", "");

		if (justName.length() == 0) {
			return "anonymous";
		} else {
			return justName;
		}
	}

	public void setTargetFunction(String targetFunction) {
		TargetFunction = targetFunction;
	}

	public String getArgs() {
		return args == null ? null : args.toString();
	}

	@JsonSetter("args")
	public void setArgs(String args_string) {
		try {
			this.args = new JSONArray(args_string);
		} catch (JSONException e) {
			System.out.println("Exception constructing JSONObject from string " + args_string);
			e.printStackTrace();
		}
	}

	public String getArgsString() {

		String arguments = "";

		return arguments;
/*		for (int i = 0; i < args.length(); i++) {
			try {
				String labels[] = JSONObject.getNames(args.getJSONObject(i));
				if (labels != null && labels.length > 0) {

					arguments +=
							", "
									+ args.getJSONObject(i).get(labels[0]).toString()
									.replaceAll("\"", "");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return arguments.replaceFirst(", ", "");*/
	}

	public String getArgsLabels() {
		if (args == null || true) {
			return "";
		} else {
			String argumentLabels = "";

			for (int i = 0; i < args.length(); i++) {
				try {
					String labels[] = JSONObject.getNames(args.getJSONObject(i));

					if (labels != null && labels.length > 0) {
						argumentLabels += ", " + labels[0].toString().replaceAll("\"", "");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			return argumentLabels.replaceFirst(", ", "");
		}
	}

	public void addAssertion(int a) {
		this.relatedAssertions.add(a);
	}

	public Vector<Integer> getAssertions() {
		return this.relatedAssertions;
	}

	public String getAssertionsString() {
		String returnMe = "";
		for (int j = 0; j < relatedAssertions.size(); j++) {
			returnMe += relatedAssertions.get(j) + (j+1 < relatedAssertions.size()?" ":"");
		}
		return returnMe;
	}
}
