package sensorDataReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;  
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;  
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import sensorDataReader.MqttConnNotify.ConnectionNotify;


public class BeaconMqttClient 
{  
    private String mHostAddr = "tcp://api.ieasygroup.com:61613";  
    private String mSubscribeGwMac;  
    private  String mUserName = "kkmtest";
    private  String mPassWord = "testpassword";
    private String mSubTopic = "";
    private String mPubTopic = "";
        
    private MqttClient mClient;  
    private MqttConnectOptions options;  
    private String clientid = "clientCloudSave";  
    
    private boolean IsConnected = false;
    private MqttConnNotify mQttNotify;
    public BeaconMqttPushCallback mBeaconCallback = null;
    
    class DevAdvInfo
    {
    	DevAdvInfo()
    	{
    		advNum = 0;
    	}
    	int advNum;
    };
    private Map<String, DevAdvInfo> mDevMap = new HashMap<>();
    
    public boolean isConnected()
    {
    	return IsConnected;
    }
    
    public void setConnectinInfo(String strHost, 
    		String strGwMac, 
    		String usrPwd,
    		String password,
    		String strPubTopic,
    		String strSubTopic)
    {
    	mHostAddr = strHost;
    	mSubscribeGwMac = strGwMac;
    	mUserName = usrPwd;
    	mPassWord = password;
    	mPubTopic = strPubTopic;
    	mSubTopic = strSubTopic;
    	
        try {  
            // host为主机名，clientid即连接MQTT的客户端ID，一般以唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存  
        	clientid = clientid + Math.random();
        	mClient = new MqttClient(mHostAddr, clientid, new MemoryPersistence());  
             
            // MQTT的连接设置  
            options = new MqttConnectOptions();  
            
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接  
            options.setCleanSession(true);  
            
            // 设置连接的用户名  
            options.setUserName(mUserName);  
            
            // 设置连接的密码  
            options.setPassword(mPassWord.toCharArray()); 
            
            // 设置超时时间 单位为秒  
            options.setConnectionTimeout(30);  
            
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制  
            options.setKeepAliveInterval(20);  
            
            // 设置回调  
            mBeaconCallback = new BeaconMqttPushCallback(this, mQttNotify);
            mClient.setCallback(mBeaconCallback); 
            
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
    }
    
    public DevAdvInfo isDevSubscribe(String strDev)
    {
    	return mDevMap.get(strDev);
    }
  
    
    public String getHostAddr()
    {
    	return mHostAddr;
    }
    
    public String getPublishTopic()
    {
    	return mSubscribeGwMac;
    }
    
    public String getUserName()
    {
    	return mUserName;
    }
   
    public String getPassword()
    {
    	return mPassWord;
    }
    
    public String getSubTopic()
    {
    	return mSubTopic;
    }
    
    public String getPubTopic()
    {
    	return mPubTopic;
    }
    
    public void setConnected(boolean enable)
    {
    	IsConnected = enable;
    }
    
    public BeaconMqttClient( MqttConnNotify mqttNotify)
    {
    	
    	mQttNotify = mqttNotify;
    	
    	initParamaters();
    }
  
    private void initParamaters() 
    {  
 
    }   
    
    public synchronized void connect()
    {
    	try { 
    		mClient.connect(options); 
	        
	        mQttNotify.connectionNotify(ConnectionNotify.CONN_NTF_CONNECED);
	        
	        IsConnected = true;
	        System.out.println("Connect to server complete");
	        
	        //订阅消息  
	        mClient.subscribe("kbeacon/publish/" + mSubscribeGwMac, 0); 
        	mClient.subscribe(mPubTopic, 0); 
        	
	        mQttNotify.appendLog("subscribe topic to server complete");
	        
	    } catch (Exception e) {  
	    	System.out.println("Connection to Mqtt server failed");
	        e.printStackTrace();
	    }
    }
    
    
    public synchronized boolean pubCommand2Gateway(String strDevMac, String msg) 
    {   
    	if (IsConnected)
    	{
        	mDevMap.put(strDevMac, new DevAdvInfo());
	        MqttMessage message = new MqttMessage(msg.getBytes());  
	        message.setQos(0);  
	        message.setRetained(false);  
	        try
	        {
	        	mClient.publish(mSubTopic, message);  
	        	return true;
	        }
	        catch(Exception e)
	        {
		        e.printStackTrace();
		        return false;
	        }
    	}
    	
    	return false;
    }  
    
    public synchronized void disconnect()
    {
    	try { 
    		mClient.disconnect();
    		mClient.close();
    		
    		mBeaconCallback.connectionLost(null);
	    } catch (Exception e) {  
	    	mQttNotify.appendLog("disconnect from mqtt server");
	    }
    }
}
