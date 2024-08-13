package sensorDataReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class FileHistory {
    private static final SimpleDateFormat mRecordTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String MAC_FILE_NAME = "_history_data.csv";
    private String mFilePath;
    public int mReadCount = 0;
    public long mMinUtcSec = 0;
    public final static int MIN_UTC_TIME = 1610000000; //2021
    
    public final static int ERR_UTC_TIME_MIN = -1;
    public final static int ERR_TEMPERATURE_INVALID = -2;
    public final static int ERR_MSG_TYPE = -3;
    public final static int ERR_SENSOR_TYPE = -4;
    public final static int ERR_MSG_LENGTH = -5;
    

    public FileHistory(String strSensorType, String strMac)
    {
    	SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm_ss");
		String fileNamePrefex =formatter.format(new Date(System.currentTimeMillis()));
		mFilePath = fileNamePrefex + "_" + strMac+"_"+strSensorType + MAC_FILE_NAME;
    }
    
    public int parseCO2Record(String strMac, long utcOffset, RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
        	throws IOException
    {
    	if (nDataLength % 10 != 0)
    	{
    		System.out.println("sensor data length is not aligin with 8byte");
    		return ERR_MSG_LENGTH;
    	}
    	int nTotalRecordNum = nDataLength / 10;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = Utils.bytesTo4Long(sensorDataRsp, index);
			if (utc_time < MIN_UTC_TIME)
			{
				utc_time = utc_time + utcOffset;
			}
			index += 4;
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);
			
		      //co2
	        int co2 = ((sensorDataRsp[index] & 0xFF) << 8) +  (sensorDataRsp[index+1] & 0xFF);
			index += 2;
			
			//temperature
			float temperature = Utils.signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
			index += 2;
			if (temperature < -30 || temperature > 40)
			{
				return ERR_TEMPERATURE_INVALID;
			}
			
			//humidity
	        float humidity = Utils.signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
	        index += 2;
	        
	    	randomFile.writeBytes(strMac + "," + strForamtTime + "," + utc_time + 
	    			"," + temperature + "," + humidity + "," + co2 + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseVOCRecord(String strMac, long utcOffset, RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
        	throws IOException
    {
    	if (nDataLength % 8 != 0)
    	{
    		System.out.println("sensor data length is not aligin with 8byte");
    		return ERR_MSG_LENGTH;
    	}
    	int nTotalRecordNum = nDataLength / 8;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = Utils.bytesTo4Long(sensorDataRsp, index);
			if (utc_time < MIN_UTC_TIME)
			{
				utc_time = utc_time + utcOffset;
			}
			index += 4;
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);
			mMinUtcSec = utc_time;
			
		    //voc
	        int voc = ((sensorDataRsp[index] & 0xFF) << 8) +  (sensorDataRsp[index+1] & 0xFF);
			index += 2;
			
			//nox
	        int nox = ((sensorDataRsp[index] & 0xFF) << 8) +  (sensorDataRsp[index+1] & 0xFF);
			index += 2;
			

	        
	    	randomFile.writeBytes(strMac + "," + strForamtTime + "," + utc_time + 
	    			"," + voc + "," + nox + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseHTRecord(String strMac, long utcOffset, RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
    	throws IOException
    {
    	if (nDataLength % 8 != 0)
    	{
    		System.out.println("sensor data length is not aligin with 8byte");
    		return ERR_MSG_LENGTH;
    	}
    	int nTotalRecordNum = nDataLength / 8;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = Utils.bytesTo4Long(sensorDataRsp, index);
			if (utc_time < MIN_UTC_TIME)
			{
				utc_time = utc_time + utcOffset;
			}
			index += 4;
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);

			
			float temperature = Utils.signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
			index += 2;
			if (temperature < -30 || temperature > 40)
			{
				return ERR_TEMPERATURE_INVALID;
			}

	        float humidity = Utils.signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
	        index += 2;
	        
	    	randomFile.writeBytes(strMac + "," + strForamtTime + "," + utc_time + "," + temperature + "," + humidity + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseButtonRecord(String strMac, long utcOffset, RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
        	throws IOException
    {
    	if (nDataLength % 8 != 0)
    	{
    		return -1;
    	}
    	int nTotalRecordNum = nDataLength / 8;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = Utils.bytesTo4Long(sensorDataRsp, index);
			index += 4;
			
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);
			
			byte btn_evt = sensorDataRsp[index];
			index += 4;

	    	randomFile.writeBytes(strMac + "," + strForamtTime + "," + utc_time + "," + btn_evt + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseSensorData(int frm_idx, String strMac, int utcOffset, byte[] sensorDataRsp) 
    {
    	int nTotalRecordNum = 0;
    	try 
    	{

	    	// 打开一个随机访问文件流，按读写方式
	    	RandomAccessFile randomFile = new RandomAccessFile(mFilePath, "rw");
	    	// 文件长度，字节数
	    	long fileLength = randomFile.length();
	    	// 将写文件指针移到文件尾。
	    	randomFile.seek(fileLength);
	    	if (frm_idx == 0)
	    	{
	    		mMinUtcSec = 0;
	    	}
	    	
	    	
	    	//msg type
	    	int index = 0;
	    	if (sensorDataRsp[index++] != 0x2)
	    	{
	    		System.out.println("msg type is not 0x2");
	    		return ERR_MSG_TYPE;
	    	}
	    	
	    	//msg length
	    	int msg_len = (int)((sensorDataRsp[index++] & 0xFF) << 8);
	    	msg_len += (int)(sensorDataRsp[index++] & 0xFF);
	    	if (sensorDataRsp.length != msg_len + 3)
	        {
	    		System.out.println("msg length invalid");
	            return ERR_MSG_LENGTH;
	        }
	    	
	    	//sensor type
	    	int sensor_type = sensorDataRsp[index++];
	    	
	    	//next record
	    	index += 4;
	    	
	    	int nDataLength = sensorDataRsp.length - index;
	    	if (sensor_type == 2)
	    	{
	    		nTotalRecordNum = parseHTRecord(strMac, utcOffset, randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	else if (sensor_type == 4)
	    	{
	    		nTotalRecordNum = parseButtonRecord(strMac, utcOffset, randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	else if (sensor_type == 65)
	    	{
	    		nTotalRecordNum = parseCO2Record(strMac, utcOffset, randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	else if (sensor_type == 64)
	    	{
	    		nTotalRecordNum = parseVOCRecord(strMac, utcOffset, randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	
	    	randomFile.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    		return 0;
    	}
    	
    	return nTotalRecordNum;
	}
    
    public int readDataCount()
    {
    	return mReadCount;
    }

}
