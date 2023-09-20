package sensorDataReader;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import sensorDataReader.BeaconMqttPushCallback.BeaconObject;
import sensorDataReader.MqttConnNotify.ConnectionNotify;

public class BeaconPannel extends JPanel implements MqttConnNotify {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String CFG_MQTT_SRV_URL = "MqttSrvUrl";
	private final static String CFG_MQTT_USR_NAME = "MqttUserName";
	private final static String CFG_MQTT_USR_PASSWORD = "MqttPassword";
	private final static String CFG_SENSOR_TYPE= "SensorType";
	private final static String CFG_SENSOR_DIRECTION= "SensorDirection";
	private final static String CFG_SENSOR_START_POS= "SensorStartPos";
	private final static String CFG_SENSOR_READ_COUNT= "SensorReadCount";

	BeaconMqttClient mMqttClient; // mqtt connection

	private JLabel labelMqttSrv, labelGwID, labelDevList, labelMqttUser, labelMqttPwd;
	private JButton buttonConn, buttonClear, buttonReadHistory;
	private JTextField textMqttSrv, textGwID, textDevMac, textMqttUser, textMqttPwd, 
		textSensorType, textDirection, textStartPos, textMaxCount;
	private JTextArea textLogInfo;
	private JPanel pannelMqttSrv, pannelGwID, pannelDevList, pannelUser, pannelPwd, pannelLogin, pannelLogInfo, pannelReadHistory;
	int mReadSequence = 1;
	
	public BeaconPannel() {
		mMqttClient = new BeaconMqttClient(this);

		this.labelMqttSrv = new JLabel("MQTT Broker");
		this.labelGwID = new JLabel("Gateway MAC");
		this.labelDevList = new JLabel("Beacon MAC");
		this.labelMqttUser = new JLabel("MQTT Name");
		this.labelMqttPwd = new JLabel("MQTT password");

		this.textMqttSrv = new JTextField(30);
		this.textGwID = new JTextField(30);
		this.textDevMac = new JTextField(30);
		this.textMqttUser = new JTextField(10);
		this.textMqttPwd = new JTextField(10);

		this.textLogInfo = new JTextArea(18, 56);
		JScrollPane scroll = new JScrollPane(textLogInfo);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.buttonConn = new JButton("Connect");
		this.buttonClear = new JButton("Clear Log");

		this.setLayout(new GridLayout(2, 1)); // 网格式布局
		JPanel upper = new JPanel();
		upper.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.setLayout(new GridLayout(7, 1)); // 网格式布局

		this.pannelMqttSrv = new JPanel();
		upper.add(pannelMqttSrv);
		this.pannelGwID = new JPanel();
		upper.add(pannelGwID);
		this.pannelDevList = new JPanel();
		upper.add(pannelDevList);
		this.pannelUser = new JPanel();
		upper.add(pannelUser);
		this.pannelPwd = new JPanel();
		upper.add(pannelPwd);
		this.pannelLogin = new JPanel();
		upper.add(pannelLogin);
		this.pannelReadHistory = new JPanel();
		this.pannelReadHistory.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelReadHistory);
		
		this.add(upper);

		this.pannelLogInfo = new JPanel();
		this.add(pannelLogInfo);

		this.pannelMqttSrv.add(this.labelMqttSrv);
		this.pannelMqttSrv.add(this.textMqttSrv);
		this.pannelMqttSrv.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.pannelGwID.add(this.labelGwID);
		this.pannelGwID.add(this.textGwID);
		String strGwList = BeaconConfig.getPropertyValue("GWList", null);
		textGwID.setText(strGwList);
		this.pannelGwID.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		this.pannelDevList.add(this.labelDevList);
		this.pannelDevList.add(this.textDevMac);
		String strDevList = BeaconConfig.getPropertyValue("DevList", null);
		if (strDevList != null){
			textDevMac.setText(strDevList);
		}
		this.pannelDevList.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.pannelUser.add(this.labelMqttUser);
		this.pannelUser.add(this.textMqttUser);
		this.pannelUser.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.pannelPwd.add(this.labelMqttPwd);
		this.pannelPwd.add(this.textMqttPwd);
		this.pannelPwd.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.pannelLogin.add(this.buttonConn);
		this.pannelLogin.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.pannelLogin.add(this.buttonClear);
		this.pannelLogin.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel labelTitle = new JLabel("SensorType");
		this.pannelReadHistory.add(labelTitle);
		textSensorType = new JTextField(3);
		this.textSensorType.setText(BeaconConfig.getPropertyValue(CFG_SENSOR_TYPE,""));
		this.pannelReadHistory.add(textSensorType);
		
		labelTitle = new JLabel("Direction(0~2)");
		this.pannelReadHistory.add(labelTitle);
		textDirection = new JTextField(3);
		this.textDirection.setText(BeaconConfig.getPropertyValue(CFG_SENSOR_DIRECTION,""));
		this.pannelReadHistory.add(textDirection);
		
		labelTitle = new JLabel("StartPos");
		this.pannelReadHistory.add(labelTitle);
		textStartPos = new JTextField(6);
		this.textStartPos.setText(BeaconConfig.getPropertyValue(CFG_SENSOR_START_POS,""));
		this.pannelReadHistory.add(textStartPos);
		
		labelTitle = new JLabel("MaxReadCount");
		this.pannelReadHistory.add(labelTitle);
		textMaxCount = new JTextField(6);
		this.textMaxCount.setText(BeaconConfig.getPropertyValue(CFG_SENSOR_READ_COUNT,""));
		this.pannelReadHistory.add(textMaxCount);
		buttonReadHistory = new JButton("Start Read");
		buttonReadHistory.setEnabled(false);
		this.pannelReadHistory.add(buttonReadHistory);

		//log info
		this.pannelLogInfo.add(scroll);
		this.pannelLogInfo.setLayout(new FlowLayout(FlowLayout.LEFT));

		String strMqttSrv = BeaconConfig.getPropertyValue(CFG_MQTT_SRV_URL,
				mMqttClient.getHostAddr());
		this.textMqttSrv.setText(strMqttSrv);

		String strMqttUserName = BeaconConfig.getPropertyValue(
				CFG_MQTT_USR_NAME, mMqttClient.getUserName());
		this.textMqttUser.setText(strMqttUserName);

		String strMqttUserPassword = BeaconConfig.getPropertyValue(
				CFG_MQTT_USR_PASSWORD, mMqttClient.getPassword());
		this.textMqttPwd.setText(strMqttUserPassword);
		addClickListener();
		
		appendLog("Mark: \n Sensor Type: 2: humidity 8:Cutoff, 16:PIR, 32:light\n"
				+ "Direction: 0: order read ,1: Reverse read, 2: Read new\n"
				+ "StartPos: The starting position of the record to be read. If a new record is to be read, this field can be any value.\n");
	}

	private void addClickListener() {
		buttonConn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startConnectCloud();
			}
		});

		buttonClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mMqttClient.mBeaconCallback != null)
				{
					mMqttClient.mBeaconCallback.clearAllDevice();
				}
				textLogInfo.setText("");
			}
		});
		
		buttonReadHistory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startReadHistory();
			}
		});
		
	}


	FileHistory mHistoryFile = null;


	public void startReadHistory() {
		String strSensorType = textSensorType.getText().toString();
		String strDirection = textDirection.getText().toString();
		String strStartPos = textStartPos.getText().toString();
		String strReadMaxCount = textMaxCount.getText().toString();
		if (!Utils.isNumber(strSensorType)
				|| !Utils.isNumber(strDirection)
				|| !Utils.isNumber(strStartPos)
				|| !Utils.isNumber(strReadMaxCount))
		{
			appendLog("input data not number");
			return;
		}
		
		int sensorType = Integer.valueOf(strSensorType);
		int sensorDiectrion = Integer.valueOf(strDirection);
		if (sensorDiectrion > 2)
		{
			appendLog("direciton invalid (0 ~ 2)");
			return;
		}
		int startPos = Integer.valueOf(strStartPos);
		int maxCount = Integer.valueOf(strReadMaxCount);
		
		//save config
		BeaconConfig.savePropertyValue(CFG_SENSOR_TYPE,strSensorType);
		BeaconConfig.savePropertyValue(CFG_SENSOR_READ_COUNT,strReadMaxCount);
		BeaconConfig.savePropertyValue(CFG_SENSOR_START_POS,strStartPos);
		BeaconConfig.savePropertyValue(CFG_SENSOR_DIRECTION,strDirection);
		
		//message
		byte readHistory[] = new byte[10];
		int index = 0;
		readHistory[index++] = 0x2;
		readHistory[index++] = (byte)sensorType;
		readHistory[index++] = (byte)((startPos >> 24) & 0xFF);
		readHistory[index++] = (byte)((startPos >> 16) & 0xFF);
		readHistory[index++] = (byte)((startPos >> 8) & 0xFF);
		readHistory[index++] = (byte)(startPos & 0xFF);
		
		readHistory[index++] = (byte)((maxCount >> 8) & 0xFF);
		readHistory[index++] = (byte)(maxCount & 0xFF);

		readHistory[index++] = (byte)sensorDiectrion;
		readHistory[index++] = (byte)0x14;


		JSONObject msg = new JSONObject();
		msg.put("msg", "dData");
		msg.put("mac", textDevMac.getText().toString());
		msg.put("seq", mReadSequence++);
		msg.put("auth1", "0000000000000000");
		msg.put("dType", "hex");
		String msgData = Utils.bytesToHex(readHistory, false);
		msg.put("data", msgData);
		
		String strTopic = "kbeacon/subaction/" + textGwID.getText().toString();
		if (this.mMqttClient.pubCommand2Gateway(strTopic, msg.toString()))
		{
			mHistoryFile = new FileHistory(strSensorType, textDevMac.getText().toString());
			appendLog("start read device history");
		}
		else
		{
			mHistoryFile = null;
		}
		
		return;
	}
	
	@Override
	public int handleSubscribeRsp(String strTopic, JSONObject cmdReqAgent) {
		// TODO Auto-generated method stub
		if (mHistoryFile == null)
		{
			return -1;
		}
		
		if (!cmdReqAgent.has("cause"))
		{
			appendLog("Error: no cause in message");
			return -1;
		}
		int cause = cmdReqAgent.getInt("cause");
		if (cause != 0 && cause != 1)
		{
			appendLog("Error: cause is" + cause);
			return -1;
		}
		
		if (!cmdReqAgent.has("idx"))
		{
			appendLog("Receive gateway ack");
			return 0;
		}

		if (!cmdReqAgent.has("utcOffset"))
		{
			appendLog("Error no utc offset");
			return -1;
		}

		if (!cmdReqAgent.has("data"))
		{
			appendLog("No record data");
			return -1;
		}
		
		if (!cmdReqAgent.has("dType") || !cmdReqAgent.getString("dType").equals("hex"))
		{
			appendLog("history data not hex");
			return -1;
		}
		
		int index = cmdReqAgent.getInt("idx");
		int utcOffset = cmdReqAgent.getInt("utcOffset");
		StringBuffer strBuffer = new StringBuffer();
		
		if (cmdReqAgent.has("data"))
		{
			strBuffer.append("Read response, index:");
			strBuffer.append(index);
			strBuffer.append(", UTC offset:");
			strBuffer.append(utcOffset);
			
			int record_count = mHistoryFile.parseSensorData(cmdReqAgent.getString("data"));
			if (record_count > 0)
			{

				strBuffer.append(", read count:");
				strBuffer.append(record_count);
				strBuffer.append(", total count:");
				strBuffer.append(mHistoryFile.readDataCount());
			}
			else
			{
				strBuffer.append(",Read failed");
				strBuffer.append(record_count);
			}
			appendLog(strBuffer.toString());
		}
		
		if (cause == 0)
		{
			appendLog("All data read complete, count:" + mHistoryFile.readDataCount());
			mHistoryFile = null;
		}
		
		return 0;
	}


	@Override
	public void connectionNotify(MqttConnNotify.ConnectionNotify connNtf) {
		// TODO Auto-generated method stub
		if (connNtf == ConnectionNotify.CONN_NTF_CONNECED) {
			buttonConn.setEnabled(false);
			buttonReadHistory.setEnabled(true);
			appendLog("Mqtt connected");
		} else if (connNtf == ConnectionNotify.CONN_NTF_DISCONNECTED) {
			buttonConn.setEnabled(true);
			buttonReadHistory.setEnabled(false);
			
			buttonConn.setEnabled(true);
			appendLog("Mqtt disconnected, wait reconnect");
			//wait and reconnect
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {				
				@Override
				public void run() {
					startConnectCloud();
				}
			}, 10000);
			
		} else if (connNtf == ConnectionNotify.CONN_SHAKE_SUCCESS) {
			appendLog("Gateway shake success");
		}
	}
	
	public void startConnectCloud()
	{
		String strMqttSrvAddr = textMqttSrv.getText();
		String strGwMac = textGwID.getText();
		String strMqttUser = textMqttUser.getText();
		String strMqttPwd = textMqttPwd.getText();
		String strDevMac = textDevMac.getText();
		
		if (!Utils.isMacAddressValid(strDevMac)
				|| !Utils.isMacAddressValid(strGwMac))
		{
			appendLog("mac address invalid");
			return;
		}

		if (!mMqttClient.isConnected()) {
			BeaconConfig.savePropertyValue(CFG_MQTT_USR_PASSWORD,
					strMqttPwd);
			BeaconConfig.savePropertyValue(CFG_MQTT_USR_NAME,
					strMqttUser);
			BeaconConfig.savePropertyValue(CFG_MQTT_SRV_URL,
					strMqttSrvAddr);
			BeaconConfig.savePropertyValue("GWList", strGwMac);
			BeaconConfig.savePropertyValue("DevList", strDevMac);
			
			appendLog("Start connect gateway and monitor device:" + strDevMac);

			mMqttClient.setConnectinInfo(strMqttSrvAddr, strGwMac, strDevMac,
					strMqttUser, strMqttPwd);
			mMqttClient.connect();
		}
	}
	
	public void appendLog(String strLog)
	{
		textLogInfo.append(Utils.getCurrentTime());
		textLogInfo.append(strLog + "\r\n");
	}
	
}