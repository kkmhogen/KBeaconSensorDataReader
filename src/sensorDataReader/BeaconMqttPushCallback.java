package sensorDataReader;


import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import sensorDataReader.MqttConnNotify.ConnectionNotify;


public class BeaconMqttPushCallback implements MqttCallback {  
    BeaconMqttClient mClient;
    MqttConnNotify mMqttNotify;
    
    public static final int ERR_INVALID_INPUT = 1;
   	public static final int ERR_PARSE_SUCCESS = 0;
   	
   	public static final int EDDY_TLM_EXTEND = 0x21;
   	
   	private FileUtils mFileUtils;
	public static final String DEF_BEACON_TYPE = "0";

   	public class BeaconObject
   	{
   		String mMacAddress;    //device id
   		String mAdvData;       //adv data
   		int mRssi;
   		String uuid;
   		int mReferancePower;
   		long mLastUpdateMsec;  //report time
   		long mCommandCause;
   	};
   	private HashMap<String, BeaconObject> mDeviceMap = new HashMap<>();
    
    BeaconMqttPushCallback(BeaconMqttClient conn, MqttConnNotify mqttNotify){
    	mClient = conn;
    	mMqttNotify = mqttNotify;
    	mFileUtils = new FileUtils();
    	
    }

    public void connectionLost(Throwable cause) {  
        //connection lost, now reconnect
        System.err.println("MQTT client connection disconnected");
        mClient.setConnected(false);
        mDeviceMap.clear();
        
        mMqttNotify.connectionNotify(ConnectionNotify.CONN_NTF_DISCONNECTED);
    }  
    
    public void deliveryComplete(IMqttDeliveryToken token) {
        
    }
    
    public void clearAllDevice()
    {
    	mDeviceMap.clear();
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面  
        handleMqttMsg(topic, new String(message.getPayload()));
    }  
    
    
    protected void handleMqttMsg(String topic, String strMqttInfo)  {
		// TODO Auto-generated method stub	
		//parse jason object
		if (strMqttInfo == null){
			System.out.println("Receive invalid null data");
			return;
		}

		parseJsonReq(topic, strMqttInfo);
	}
		
	
	public int parseJsonReq(String topic, String strMqttInfo)
	{
		try 
		{
			JSONObject cmdReq = JSONObject.fromObject(strMqttInfo);
			if (cmdReq == null)
			{
				System.out.println("Connection to Mqtt server failed");
				return 0;
			}
			
			//message type
			String strDataType = cmdReq.getString("msg");
			if (strDataType.equalsIgnoreCase("advdata"))
			{
				return handleBeaconRpt(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("alive"))
			{
				return handleShakeReq(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("dAck"))
			{
				return mMqttNotify.handleSubscribeRsp(topic, cmdReq);
			}
			
			return ERR_PARSE_SUCCESS;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return ERR_INVALID_INPUT;
		}
	}
	
	public int handleShakeReq(JSONObject cmdReqAgent)
	{
		return ERR_PARSE_SUCCESS;
	}

	 public int handleBeaconRpt(JSONObject cmdReqAgent)
	{		
		try 
		{		
			//mac address
			String strGwAddress = cmdReqAgent.getString("gmac");
			strGwAddress = strGwAddress.toUpperCase();
			if (!Utils.isMacAddressValid(strGwAddress)){
				System.out.println("beacon mqtt input invalid error");
				return ERR_INVALID_INPUT;
			}
					
			//obj list
			JSONArray objArray = cmdReqAgent.getJSONArray("obj");
			if (objArray == null)
			{
				System.out.println("unknown obj data");
				return ERR_INVALID_INPUT;
			}
			
			//update mac
			for (int i = 0; i < objArray.size(); i++)
			{
				JSONObject obj = objArray.getJSONObject(i);
		
				//device mac address
				String strDevMac = obj.getString("dmac");
				
				BeaconMqttClient.DevAdvInfo advInfo = mClient.isDevSubscribe(strDevMac);
				if (advInfo != null)
				{
					int nRssi = obj.getInt("rssi");
					String strTime = obj.getString("time");
					
					Double temp = null;
					Double humidity = null;
					Integer voltage = null;
					if (obj.has("temp"))
					{
						temp = obj.getDouble("temp");
					}
					if (obj.has("hum"))
					{
						humidity = obj.getDouble("hum");
					}
					if (obj.has("vbatt"))
					{
						voltage = obj.getInt("vbatt");
					}
					
					String data1 = null;
					if (obj.has("data1"))
					{
						data1 = obj.getString("data1");
						
						//get temperature
						int prefexIndex = data1.indexOf("0616F6FF");
						if (prefexIndex > 0)
						{
							prefexIndex += 8;
							String strTemp = data1.substring(prefexIndex);
							if (strTemp != null && strTemp.length() == 6)
							{
								voltage = Integer.valueOf(strTemp.substring(0, 2), 16);
								temp = Integer.valueOf(strTemp.substring(2, 4), 16).doubleValue();
								humidity = Integer.valueOf(strTemp.substring(4, 6), 16).doubleValue();
							}
						}
					}
					
					
					advInfo.advNum++;
					if (advInfo.advNum % 100 == 1)
					{
						this.mMqttNotify.appendLog("GW:"+ strGwAddress + ",Dev:" + strDevMac + ",num:" + advInfo.advNum + ",rssi:" + nRssi);
					}
					if (voltage != null)
					{
						mFileUtils.addMacAddressToFile(strTime, strDevMac, strGwAddress, nRssi, temp, humidity, voltage);
					}
					else
					{
						mFileUtils.addMacAddressToFile(strTime, strDevMac, strGwAddress, nRssi, temp, humidity, data1);
					}
				}
			}
		} 
		catch (Exception e) 
		{
			return ERR_INVALID_INPUT;
		}

		return ERR_PARSE_SUCCESS;
	}
}