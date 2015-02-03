package com.clematis.core.trace;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

public class CandidateDOMElement {
	private TraceObject candidateDOMElement;
	private int numberOfAccesses=0;
	public CandidateDOMElement(TraceObject domElem){
		candidateDOMElement=domElem;
	}
	public TraceObject getCandidateDOMElement(){
		return candidateDOMElement;
	}
	public int getNumberOfAccesses(){
		return numberOfAccesses;
	}
	public void setNumberOfAccesses(int numberOfAccesses){
		this.numberOfAccesses=numberOfAccesses;
	}
	@SuppressWarnings("unchecked")
	public Vector<TraceObject> getRelevantMutations(Collection<TraceObject> traceMutations) throws Exception {
		Vector<TraceObject> returnMe = new Vector<TraceObject>();
		JSONObject domAccess=new JSONObject(((DOMElementAccess)candidateDOMElement).getElement());
		Iterator<String> elKeys = domAccess.keys();
		JSONObject currentEl;
		Collection<TraceObject> allMutations = traceMutations;
		Iterator<TraceObject> mutationIterator;
		TraceObject currentMutation;
		int accessCounter = -1;

		//ArrayList<TraceObject> allAccesses = s1.getDOMAccesses();
		int m = 0;
		while (elKeys.hasNext()){
			currentEl = new JSONObject(domAccess.getJSONObject(elKeys.next()).toString());
			
			accessCounter = currentEl.getInt("counter");
			currentEl.remove("counter");

			mutationIterator = allMutations.iterator();

			while (mutationIterator.hasNext()) {
				currentMutation = mutationIterator.next();
				if (currentMutation.getCounter() > accessCounter) {
					// Ignore those mutations after the assertion
					continue;
				}


				//TODO: Current comparison is lazy, should compare as objects instead of strings
				if (currentMutation instanceof DOMMutationTrace && 
						commpareJSON(((DOMMutationTrace)currentMutation).getParentNode(), currentEl)) {
					returnMe.add(currentMutation);
				} else if (currentMutation instanceof DOMMutationTrace &&
						((DOMMutationTrace)currentMutation).getParentNode().has("id") &&
						(currentEl).has("id") &&
						((DOMMutationTrace)currentMutation).getParentNode().getString("id").equals(currentEl.getString("id"))) {
					returnMe.add(currentMutation);

				} else if (currentMutation instanceof DOMMutationTrace &&
						((DOMMutationTrace)currentMutation).getParentNode().has("xPath") &&
						(currentEl).has("xPath") &&
						((DOMMutationTrace)currentMutation).getParentNode().getString("xPath").equals(currentEl.getString("xPath"))) {
					returnMe.add(currentMutation);

				}
			}
		}
		System.out.println("returning size:  " + returnMe.size());
		return returnMe;
	}

	@SuppressWarnings("unchecked")
	private static boolean commpareJSON(JSONObject jso1, JSONObject jso2) {
		boolean returnMe = true;
		Iterator<String> jso1keys = jso1.sortedKeys();
		Iterator<String> jso2keys = jso2.sortedKeys();
		String key1;
		String key2;		
		try {
			while (jso1keys.hasNext()) {
				if (!jso2keys.hasNext()) {
					// Number of keys between two object is not even
					returnMe = false;
					break;
				}
				key1 = jso1keys.next();
				key2 = jso2keys.next();
				if (jso1.get(key1) instanceof JSONObject && jso2.get(key2) instanceof JSONObject) {
					// JSON objects, special case, recusively call this method (commpareJSON)
					if (!commpareJSON(jso1.getJSONObject(key1), jso2.getJSONObject(key2))) {
						returnMe = false;
						break;
					}
				} else if (jso1.get(key1) instanceof String && jso2.get(key2) instanceof String) {

					// value is String
					if (!jso1.get(key1).equals(jso2.get(key2))) {
						returnMe = false;
						break;
					}
				} else {
					// value is some other primitive (Integer, Double)
					if (jso1.get(key1) != jso2.get(key2)) {
						returnMe = false;
						break;
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return returnMe;
	}
}
