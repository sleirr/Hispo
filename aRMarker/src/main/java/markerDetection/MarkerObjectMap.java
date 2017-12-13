package markerDetection;

import java.util.HashMap;

import gl.MarkerObject;



public class MarkerObjectMap extends HashMap<Integer, MarkerObject> {

	public void put(MarkerObject markerObject) {
		put(markerObject.getMyId(), markerObject);
	}

}
