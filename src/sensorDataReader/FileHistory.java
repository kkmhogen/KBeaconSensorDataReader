package sensorDataReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class FileHistory {
    private static final SimpleDateFormat mRecordTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String MAC_FILE_NAME = "_history_data.csv";
    private String mFilePath;
    public int mReadCount = 0;
    
    public FileHistory(String strSensorType, String strMac)
    {
    	SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm");
		String fileNamePrefex =formatter.format(new Date(System.currentTimeMillis()));
		mFilePath = fileNamePrefex + "_" + strMac+"_"+strSensorType + MAC_FILE_NAME;
    }
    
    public static long bytesTo4Long( byte[] array, int offset )
    {
        long nData = (array[offset] & 0xFF);
        nData = nData << 8;
        nData += (array[offset + 1] & 0xFF);
        nData = nData << 8;
        nData += (array[offset + 2] & 0xFF);
        nData = nData << 8;
        nData += (array[offset + 3] & 0xFF);

        return nData;
    }
    
    public static float signedBytes2Float(byte byHeight, byte byLow)
    {
        int combine = ((byHeight & 0xFF) << 8) + (byLow & 0xFF);
        if (combine >= 0x8000)
        {
            combine = combine - 0x10000;
        }

        float fResult = (float)(combine / 256.0);
        BigDecimal bigTemp = new BigDecimal(fResult);
        return bigTemp.setScale(2,   BigDecimal.ROUND_HALF_UP).floatValue();
    }
    
    public int parseHTRecord(RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
    	throws IOException
    {
    	if (nDataLength % 8 != 0)
    	{
    		return -1;
    	}
    	int nTotalRecordNum = nDataLength / 8;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = bytesTo4Long(sensorDataRsp, index);
			index += 4;
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);
			
			float temperature = signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
			index += 2;

	        float humidity = signedBytes2Float(sensorDataRsp[index], sensorDataRsp[index+1]);
	        index += 2;
	        
	    	randomFile.writeBytes(strForamtTime + "," + utc_time + "," + temperature + "," + humidity + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseButtonRecord(RandomAccessFile randomFile, int nDataLength, byte[] sensorDataRsp, int index) 
        	throws IOException
    {
    	if (nDataLength % 8 != 0)
    	{
    		return -1;
    	}
    	int nTotalRecordNum = nDataLength / 8;
		for (int i = 0; i < nTotalRecordNum; i++)
		{
			long utc_time = bytesTo4Long(sensorDataRsp, index);
			index += 4;
			String strForamtTime = mRecordTimeFormat.format(utc_time * 1000);
			
			byte btn_evt = sensorDataRsp[index];
			index += 4;

	    	randomFile.writeBytes(strForamtTime + "," + utc_time + "," + btn_evt + "\r\n");
		}
		mReadCount += nTotalRecordNum;
		
		return nTotalRecordNum;
    }
    
    public int parseSensorData(String strSensorData) 
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
	    	
	    	byte[] sensorDataRsp = Utils.hexStringToBytes(strSensorData);
	    	
	    	//msg type
	    	int index = 0;
	    	if (sensorDataRsp[index++] != 0x2)
	    	{
	    		return -1;
	    	}
	    	
	    	//msg length
	    	int msg_len = (int)((sensorDataRsp[index++] & 0xFF) << 8);
	    	msg_len += (int)(sensorDataRsp[index++] & 0xFF);
	    	if (sensorDataRsp.length != msg_len + 3)
	        {
	            return -2;
	        }
	    	
	    	//sensor type
	    	int sensor_type = sensorDataRsp[index++];
	    	if (sensor_type != 2 && sensor_type != 4)
	    	{
	    		return -3;
	    	}
	    	
	    	//next record
	    	index += 4;
	    	
	    	int nDataLength = sensorDataRsp.length - index;
	    	if (sensor_type == 2)
	    	{
	    		nTotalRecordNum = parseHTRecord(randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	else if (sensor_type == 4)
	    	{
	    		nTotalRecordNum = parseButtonRecord(randomFile, nDataLength, sensorDataRsp, index);
	    	}
	    	
	    	randomFile.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    		return -6;
    	}
    	
    	return nTotalRecordNum;
	}
    
    public int readDataCount()
    {
    	return mReadCount;
    }

}
