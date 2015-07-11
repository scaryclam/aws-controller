package uk.co.bluesuntech.export;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class Exporter {
	private JSONObject exportExploration() throws JSONException {
		JSONObject configuration = new JSONObject();
		configuration.put("foo", "bar");
		return configuration;
	}
}
