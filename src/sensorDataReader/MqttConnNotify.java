package sensorDataReader;

import net.sf.json.JSONObject;

public abstract interface MqttConnNotify {
	public enum ConnectionNotify
	{
		CONN_NTF_CONNECED,    //conn mqtt srv success
		CONN_SHAKE_SUCCESS,   //shake with gateway success
		CONN_NTF_DISCONNECTED,
	};

	
	public abstract int handleSubscribeRsp(String strTopic, JSONObject cmdReqAgent);
	
	public abstract void connectionNotify(ConnectionNotify connNtf);
			
	public abstract void appendLog(String strLog);
}
