package sensorDataReader;
import java.util.ArrayList;
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
    
    public void setConnectinInfo(String strHost, String strGwMac, String strDevMac, String usrPwd, String password)
    {
    	mHostAddr = strHost;
    	mSubscribeGwMac = strGwMac;
    	mUserName = usrPwd;
    	mPassWord = password;
    	mDevMap.put(strDevMac, new DevAdvInfo());
    	
    	
        try {  
            // hostΪ��������clientid������MQTT�Ŀͻ���ID��һ����Ψһ��ʶ����ʾ��MemoryPersistence����clientid�ı�����ʽ��Ĭ��Ϊ���ڴ汣��  
        	clientid = clientid + Math.random();
        	mClient = new MqttClient(mHostAddr, clientid, new MemoryPersistence());  
             
            // MQTT����������  
            options = new MqttConnectOptions();  
            
            // �����Ƿ����session,�����������Ϊfalse��ʾ�������ᱣ���ͻ��˵����Ӽ�¼����������Ϊtrue��ʾÿ�����ӵ������������µ��������  
            options.setCleanSession(true);  
            
            // �������ӵ��û���  
            options.setUserName(mUserName);  
            
            // �������ӵ�����  
            options.setPassword(mPassWord.toCharArray()); 
            
            // ���ó�ʱʱ�� ��λΪ��  
            options.setConnectionTimeout(30);  
            
            // ���ûỰ����ʱ�� ��λΪ�� ��������ÿ��1.5*20���ʱ����ͻ��˷��͸���Ϣ�жϿͻ����Ƿ����ߣ������������û�������Ļ���  
            options.setKeepAliveInterval(20);  
            
            // ���ûص�  
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
	        
	        //������Ϣ  
	        mClient.subscribe("kbeacon/publish/" + mSubscribeGwMac, 0); 
        	mClient.subscribe("kbeacon/pubaction/" + mSubscribeGwMac, 0); 
        	
	        mQttNotify.appendLog("subscribe topic to server complete");
	        
	    } catch (Exception e) {  
	    	System.out.println("Connection to Mqtt server failed");
	        e.printStackTrace();
	    }
    }
    
    
    public synchronized boolean pubCommand2Gateway(String strTopic, String msg) 
    {   
    	if (IsConnected)
    	{
	        MqttMessage message = new MqttMessage(msg.getBytes());  
	        message.setQos(0);  
	        message.setRetained(false);  
	        try
	        {
	        	mClient.publish(strTopic, message);  
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
