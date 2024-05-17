package sensorDataReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.text.SimpleDateFormat;
public class FileUtils {




    private final static String MAC_FILE_NAME = "_TestMacList.txt";
    private String mFilePath;
    
    public FileUtils()
    {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
		String fileNamePrefex =formatter.format(new Date(System.currentTimeMillis()));
		mFilePath = fileNamePrefex + "_" + MAC_FILE_NAME;
    }
    
    public void addMacAddressToFile(String time, String mac, String gw, int nRssi, Double temp, 
    		Double humidity, 
    		Integer voltage) 
    {
    	try 
    	{

	    	// 打开一个随机访问文件流，按读写方式
	    	RandomAccessFile randomFile = new RandomAccessFile(mFilePath, "rw");
	    	// 文件长度，字节数
	    	long fileLength = randomFile.length();
	    	// 将写文件指针移到文件尾。
	    	randomFile.seek(fileLength);
	    	
			
	    	randomFile.writeBytes(time + "\t" + mac + "\t" + gw + "\t" + nRssi);
	    	if (temp != null)
	    	{
	    		randomFile.writeBytes("\t" + temp);
	    	}
	    	if (humidity != null)
	    	{
	    		randomFile.writeBytes("\t" + humidity);
	    	}
	    	if (voltage != null)
	    	{
	    		randomFile.writeBytes("\t" + voltage);
	    	}
	
	    	
	    	randomFile.writeBytes("\r\n");
	    	
	    	randomFile.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
  

    public void addMacAddressToFile(String time, String mac, String gw, int nRssi, Double temp, Double humidity, String data1) 
    {
    	try 
    	{

	    	// 打开一个随机访问文件流，按读写方式
	    	RandomAccessFile randomFile = new RandomAccessFile(mFilePath, "rw");
	    	// 文件长度，字节数
	    	long fileLength = randomFile.length();
	    	// 将写文件指针移到文件尾。
	    	randomFile.seek(fileLength);
	    	
			
	    	randomFile.writeBytes(time + "\t" + mac + "\t" + gw + "\t" + nRssi);
	    	if (temp != null)
	    	{
	    		randomFile.writeBytes("\t" + temp);
	    	}
	    	if (humidity != null)
	    	{
	    		randomFile.writeBytes("\t" + humidity);
	    	}
	    	
	    	randomFile.writeBytes("\r\n");
	    	
	    	randomFile.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
}

