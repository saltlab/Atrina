package com.clematis.core.episode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonSetter;
import org.json.JSONObject;

import com.clematis.core.trace.DOMElementAccess;
import com.clematis.core.trace.DOMElementValueTrace;
import com.clematis.core.trace.DOMEventTrace;
import com.clematis.core.trace.DOMMutationTrace;
import com.clematis.core.trace.TimeoutCallback;
import com.clematis.core.trace.TimeoutSet;
import com.clematis.core.trace.TimingTrace;
import com.clematis.core.trace.TraceObject;
import com.clematis.core.trace.XMLHttpRequestOpen;
import com.clematis.core.trace.XMLHttpRequestResponse;
import com.clematis.core.trace.XMLHttpRequestSend;
import com.clematis.core.trace.XMLHttpRequestTrace;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonAutoDetect
@JsonRootName("Story")
@JsonPropertyOrder({ "domEventTraces", "functionTraces", "timingTraces", "xhrTraces",
	"orderedTraceList", "episodes",
	"timeoutSets", "timeoutCallbacks", "xhrOpens", "xhrSends", "xhrResponses", "domEvents",
	"domMutations", "domElementValues" })
@XmlRootElement
public class Story {
	private ArrayList<TraceObject> domEventTraces;
	private ArrayList<TraceObject> domMutationTraces;
	private ArrayList<TraceObject> functionTraces;
	private ArrayList<TraceObject> timingTraces;
	private ArrayList<TraceObject> xhrTraces;
	private ArrayList<TraceObject> orderedTraceList;
	private ArrayList<TraceObject> seleniumAssertions;
	private ArrayList<Episode> episodes;

	private HashMap<Integer, TimeoutSet> timeoutSets; // by to_id
	private HashMap<Integer, TimeoutCallback> timeoutCallbacks;
	private HashMap<Integer, XMLHttpRequestOpen> xhrOpens;
	private HashMap<Integer, XMLHttpRequestSend> xhrSends;
	private HashMap<Integer, XMLHttpRequestResponse> xhrResponses;
	private HashMap<Integer, DOMEventTrace> domEvents;
	private HashMap<Integer, DOMMutationTrace> domMutations;
	private HashMap<Integer, DOMElementValueTrace> domElementValues;

	// private HashMap<TraceObject, Episode> traceObjectToEpisodeMap;

	public Story() {
		// Mapper is calling this constructor
		System.out.println("Story constructor:");


	}


	public Story(
			Collection<TraceObject> domEventCollection,
			Collection<TraceObject> functionCollection,
			Collection<TraceObject> timingCollection,
			Collection<TraceObject> xhrCollection,
			ArrayList<TraceObject> seleniumAssertions) {
		domEventTraces = new ArrayList<TraceObject>(domEventCollection);
		functionTraces = new ArrayList<TraceObject>(functionCollection);
		timingTraces = new ArrayList<TraceObject>(timingCollection);
		xhrTraces = new ArrayList<TraceObject>(xhrCollection);
		domMutationTraces = new ArrayList<TraceObject>();
		orderedTraceList = new ArrayList<TraceObject>();
		episodes = new ArrayList<Episode>();
		this.seleniumAssertions = seleniumAssertions;

		timeoutSets = new HashMap<Integer, TimeoutSet>();
		timeoutCallbacks = new HashMap<Integer, TimeoutCallback>();
		linkTimeoutComponents();

		xhrOpens = new HashMap<Integer, XMLHttpRequestOpen>();
		xhrSends = new HashMap<Integer, XMLHttpRequestSend>();
		xhrResponses = new HashMap<Integer, XMLHttpRequestResponse>();
		linkXhrComponents();

		domEvents = new HashMap<Integer, DOMEventTrace>();
		domMutations = new HashMap<Integer, DOMMutationTrace>();
		domElementValues = new HashMap<Integer, DOMElementValueTrace>();
		linkDomComponents();


	}

	// Linking different components of timeouts
	private void linkTimeoutComponents() {
		for (TraceObject to : timingTraces) {
			if (to instanceof TimeoutSet) {
				timeoutSets.put(((TimeoutSet) to).getTimeoutId(), (TimeoutSet) to);
			} else if (to instanceof TimeoutCallback) {
				timeoutCallbacks.put(((TimeoutCallback) to).getTimeoutId(), (TimeoutCallback) to);
			} else {
				System.err.println("invalid timing trace");
			}
		}
	}

	public TimeoutCallback getTimeoutCallback(TimeoutSet timeoutSet) {
		return timeoutCallbacks.get(timeoutSet.getTimeoutId());
	}

	public TimeoutSet getTimeoutSet(TimeoutCallback timeoutCallback) {
		return timeoutSets.get(timeoutCallback.getTimeoutId());
	}

	// Linking different components of xhrs
	private void linkXhrComponents() {
		for (TraceObject to : xhrTraces) {
			if (to instanceof XMLHttpRequestOpen) {
				xhrOpens.put(((XMLHttpRequestOpen) to).getXhrId(), (XMLHttpRequestOpen) to);
			} else if (to instanceof XMLHttpRequestSend) {
				xhrSends.put(((XMLHttpRequestSend) to).getXhrId(), (XMLHttpRequestSend) to);
			} else if (to instanceof XMLHttpRequestResponse) {
				xhrResponses.put(((XMLHttpRequestResponse) to).getXhrId(),
						(XMLHttpRequestResponse) to);
			} else {
				System.err.println("invalid xhr trace");
			}
		}
	}

	public XMLHttpRequestOpen getXMLHttpRequestOpen(XMLHttpRequestSend xhrSend) {
		return xhrOpens.get(xhrSend.getXhrId());
	}

	public XMLHttpRequestOpen getXMLHttpRequestOpen(XMLHttpRequestResponse xhrResponse) {
		return xhrOpens.get(xhrResponse.getXhrId());
	}

	public XMLHttpRequestSend getXMLHttpRequestSend(XMLHttpRequestOpen xhrOpen) {
		return xhrSends.get(xhrOpen.getXhrId());
	}

	public XMLHttpRequestSend getXMLHttpRequestSend(XMLHttpRequestResponse xhrResponse) {
		return xhrSends.get(xhrResponse.getXhrId());
	}

	public XMLHttpRequestResponse getXMLHttpRequestResponse(XMLHttpRequestOpen xhrOpen) {
		return xhrResponses.get(xhrOpen.getXhrId());
	}

	public XMLHttpRequestResponse getXMLHttpRequestResponse(XMLHttpRequestSend xhrSend) {
		return xhrResponses.get(xhrSend.getXhrId());
	}

	// Get information about an episode

	public TraceObject getEpisodeSource(Episode e) {
		return e.getSource();
	}

	public EpisodeTrace getEpisodeTrace(Episode e) {
		return e.getTrace();
	}

	public ArrayList<TraceObject> getEpisodeDom(Episode e) {
		return e.getDom();
	}

	// Get information about the trace
	/*
	public TraceObject getNextTraceObject(TraceObject to) {
		for (int i = 0; i < orderedTraceList.size(); i++) {
			if (orderedTraceList.get(i) == to) { // copy constructor (?)
				if (i + 1 < orderedTraceList.size())
					return orderedTraceList.get(i + 1);
			}
		}
		return null;
	}
	 */
	public Episode getEpisode(TraceObject to) {
		for (Episode e : episodes) {
			if (e.getTrace().getTrace().contains(to))
				return e;
		}
		return null;
	}

	public ArrayList<TraceObject> getDomEventTraces() {
		return domEventTraces;
	}

	@JsonSetter("domEventTraces")
	public void setDomEventTraces(ArrayList<TraceObject> domEventTraces) {
		this.domEventTraces = domEventTraces;
	}

	public ArrayList<TraceObject> getFunctionTraces() {
		return functionTraces;
	}

	@JsonSetter("functionTraces")
	public void setFunctionTraces(ArrayList<TraceObject> functionTraces) {
		this.functionTraces = functionTraces;
	}

	public ArrayList<TraceObject> getTimingTraces() {
		return timingTraces;
	}

	@JsonSetter("timingTraces")
	public void setTimingTraces(ArrayList<TraceObject> timingTraces) {
		this.timingTraces = timingTraces;
	}

	public ArrayList<TraceObject> getXhrTraces() {
		return xhrTraces;
	}

	@JsonSetter("xhrTraces")
	public void setXhrTraces(ArrayList<TraceObject> xhrTraces) {
		this.xhrTraces = xhrTraces;
	}

	public ArrayList<TraceObject> getOrderedTraceList() {
		return orderedTraceList;
	}

	@JsonSetter("orderedTraceList")
	public void setOrderedTraceList(ArrayList<TraceObject> orderedTraceList) {
		this.orderedTraceList = orderedTraceList;
	}

	public ArrayList<Episode> getEpisodes() {
		return episodes;
	}

	@JsonSetter("episodes")
	public void setEpisodes(ArrayList<Episode> episodes) {
		this.episodes = episodes;
	}

	public HashMap<Integer, DOMEventTrace> getDomEvents() {
		return domEvents;
	}

	@JsonSetter("domEvents")
	public void setDomEvents(HashMap<Integer, DOMEventTrace> domEvents) {
		this.domEvents = domEvents;
	}

	public HashMap<Integer, DOMMutationTrace> getDomMutations() {
		return domMutations;
	}

	@JsonSetter("domMutations")
	public void setDomMutations(HashMap<Integer, DOMMutationTrace> domMutations) {
		this.domMutations = domMutations;
	}

	// Linking the different DOM components (Events and Mutations)
	private void linkDomComponents() {
		System.out.println("[linkDomComponents]");
		for (TraceObject to : domEventTraces) {
			if (to instanceof DOMEventTrace) {
				domEvents.put(((DOMEventTrace) to).getCounter(), (DOMEventTrace) to);
			} else if (to instanceof DOMMutationTrace) {
				domMutations.put(((DOMMutationTrace) to).getCounter(), (DOMMutationTrace) to);
				domMutationTraces.add(to);
			} else if (to instanceof DOMElementValueTrace) {
				domElementValues.put(((DOMElementValueTrace) to).getCounter(),
						(DOMElementValueTrace) to);
				domMutationTraces.add(to);
			} else if (to instanceof DOMElementAccess) {


			} else {
				System.err.println("invalid DOM trace");
				// TODO: Add support for 'com.clematis.core.trace.DOMElementAccess'
				System.out.println(to.getClass());
			}
		}
		// Remove all mutations from collection of DOM events
		System.out.println("Before size: " + domEventTraces.size());
		domEventTraces.removeAll(domMutationTraces);
		System.out.println("After size: " + domEventTraces.size());

		System.out.println(domEvents.size());
		System.out.println(domMutations.size());
		System.out.println(domElementValues.size());		
	}

	// todo todo todo
	public void removeUselessEpisodes() {
		ArrayList<Episode> uselessEpisodes = new ArrayList<Episode>();

		for (Episode episode : this.getEpisodes()) {
			if (episode.getSource() instanceof DOMEventTrace) {
				String eventType = ((DOMEventTrace)episode.getSource()).getEventType();
				//				System.out.println(eventType);
				if (eventType.equals("mouseover") || eventType.equals("mousemove") || eventType.equals("mouseout") || eventType.equals("mousedown")) {
					boolean uselessEpisode = true;
					// todo oooooooooooooooooooooooooooooooooooooooooo
					/*					for (TraceObject traceObject : episode.getTrace().getTrace()) {
						if (traceObject instanceof TimingTrace || traceObject instanceof XMLHttpRequestTrace) {
							System.out.println("episode not useless, contains " + traceObject.getClass());
							uselessEpisode = false;
						}
					}
					if(((DOMEventTrace)episode.getSource()).getTargetElement().contains("bookmarkButton")) {
						System.out.println("++++++++++++++ " + ((DOMEventTrace)episode.getSource()).getTargetElement());
						uselessEpisode = false;
					}
					 */					if (uselessEpisode)
						 uselessEpisodes.add(episode);
				}
			}
		}
		System.out.println("# of useless episodes: " + uselessEpisodes.size());
		this.getEpisodes().removeAll(uselessEpisodes);
		// TODO reset story's ordered trace list
		resetOrderedTraceList();
	}

	public void removeUselessEpisodes(Collection<Episode> uselessEpisodes) {
		this.episodes.removeAll(uselessEpisodes);
	}

	private void resetOrderedTraceList() {
		orderedTraceList.clear();
		for (int i = 0; i < episodes.size(); i ++) {
			for (int j = 0; j < episodes.get(i).getTrace().getTrace().size(); j ++) {
				orderedTraceList.add(episodes.get(i).getTrace().getTrace().get(j));
			}
		}
	}

	public ArrayList<TraceObject> getMutationsAsTraceObjects () {
		return domMutationTraces;
	}

	// TODO: Test this, not sure if accesses are saved in "domEventTraces"
	public ArrayList<TraceObject> getDOMAccesses() {
		ArrayList<TraceObject> returnMe = new ArrayList<TraceObject>();
		Iterator<TraceObject> domIterator = domEventTraces.iterator();
		TraceObject currentDOM;

		while (domIterator.hasNext()) {
			currentDOM = domIterator.next();
			if (currentDOM instanceof DOMElementAccess) {
				returnMe.add(currentDOM);
			}
		}

		return returnMe;
	}
	
	
	
	
	
	
	// Test case support
	public ArrayList<TraceObject> getSeleniumAssertions() {
		return seleniumAssertions;
	}

	@JsonSetter("seleniumAssertions")
	public void setSeleniumAssertions(ArrayList<TraceObject> seleniumAssertions) {
		this.seleniumAssertions = seleniumAssertions;
	}

}
