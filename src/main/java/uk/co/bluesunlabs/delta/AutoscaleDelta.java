package uk.co.bluesunlabs.delta;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class AutoscaleDelta {
	public JSONObject getAlarmDelta(JSONObject currentConfig, JSONObject existingConfig) throws JSONException {
		JSONObject alarmDelta = new JSONObject();
		return alarmDelta;
	}
}
