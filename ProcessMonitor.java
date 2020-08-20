import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;  
import java.text.DateFormat;  

class ProcessMonitor extends TimerTask
{
	static String logPath;
	static String ErrorlogPath;
	static String applicationName;
	static boolean prevStatus;
	static boolean error=false;
	static String timeformat;
	static long prevStatusTime=0;
	@Override
	public void run() {
		boolean curStatus=checkApplicationStatus(applicationName);
		if(prevStatus!=curStatus)
		{
			long curTime=curtimeToSec();
			long ranseconds=curTime-prevStatusTime;
			String convertTOHours=convertTOHours(ranseconds);
			System.out.println(curTime+"-"+prevStatusTime);
			fileWriter("STATUS CHANGED AT "+curTime()+": Monitoring "+applicationName+" Status:"+boolToStatus(curStatus)+" Ran For:"+convertTOHours,logPath);
			fileWriterCSV(boolToStatus(curStatus));
			prevStatus=curStatus;
			prevStatusTime=curtimeToSec();
		}else
		{
			fileWriter("Status Remain Same "+curTime()+": Monitoring "+applicationName+" Status:"+boolToStatus(curStatus),logPath);
		}
	}
	
	private static void fileWriter(String content,String logPath)
	{
		try(FileOutputStream fs=new FileOutputStream(logPath+"\\"+curDate()+".txt",true);)
		{
			fs.write((content+"\n").getBytes());
		}catch(Exception e)
		{
			if(error)
			{
				e.printStackTrace();
				System.exit(0);
			}
			error=true;
			fileWriter("Error on "+applicationName+" : At "+curTime()+" : Reason "+e.toString(),ErrorlogPath);
			error=false;
		}
	}
	private static void fileWriterCSV(String status)
	{
		String heading="";
		File tempFile = new File(logPath+"\\"+curDate()+".csv");
		long curTime=curtimeToSec();
		long ranseconds=curTime-prevStatusTime;
		String convertTOHours=convertTOHours(ranseconds);
		if(!tempFile.exists()){
			heading="processname,time,status,rantime\n";
		}
		String content=""+applicationName+","+curTime()+","+status+","+convertTOHours+"\n";
		try(FileOutputStream fs=new FileOutputStream(logPath+"\\"+curDate()+".csv",true);)
		{
			fs.write(((heading!=""?heading+content:content)).getBytes());
		}catch(Exception e)
		{
			fileWriter("Error on "+applicationName+" : At "+curTime()+" : Reason "+e.toString(),ErrorlogPath);
		}
	}
	public static void main(String args[]) throws Exception
	{
		try{
			if(args.length<0||args[0]==null||args[0].equals(""))
			{
				System.err.println("Need process name");
			}else if(args[0].equals("\\?")||args[0].equals("\\h")||args[0].equals("help"))
			{
				displayHelp();
				System.exit(0);
			}
			applicationName=args[0];
			System.out.println(Arrays.toString(args));
			if(args.length<2||args[1]==null||args[1].equals(""))
			{
				logPath=System.getProperty("user.dir");
				System.out.println("USING "+logPath+" FOR LOG");
			}
			else
			{
				logPath=args[1];
			}
			if(args.length<3||args[2]==null||args[2].equals(""))
			{
				ErrorlogPath=System.getProperty("user.dir");
				System.out.println("USING "+ErrorlogPath+" FOR ERRORLOG");
			}
			else
			{
				ErrorlogPath=args[2];
			}	
			if(args.length<4||args[3]==null||args[3].equals(""))
			{
				timeformat="dd hh:mm:ss";
				System.out.println("USING "+timeformat+" FOR TIMEFORMAT");
			}
			else
			{
				timeformat=args[3];
			}	
			ProcessMonitor p=new ProcessMonitor();
			Timer t=new Timer();
			prevStatus=checkApplicationStatus(applicationName);
			prevStatusTime=curtimeToSec();
			try
			{
				fileWriter("Application Started at "+curTime()+": Monitoring "+applicationName+" Status:"+boolToStatus(prevStatus),logPath);
			}catch(Exception e)
			{
				e.printStackTrace();
				return ;
			}
			try
			{
				fileWriterCSV(boolToStatus(prevStatus));
			}catch(Exception e)
			{
				e.printStackTrace();
				return ;
			}
			t.scheduleAtFixedRate(p, 0,5*1000);
		}catch(Exception e)
		{
			fileWriter("Error on "+applicationName+" : At "+curTime()+" : Reason "+e.toString(),ErrorlogPath);
		}
	}
	private static Long curtimeToSec() {
		java.time.LocalDate.now();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");  
		Date date = new Date(); 
		long seconds = date.getTime() / 1000L;
		return new Long(seconds);
	}
	private static String curDate() {return String.valueOf(java.time.LocalDate.now());}
	private static String boolToStatus(boolean status) {return (status?"Running":"Not Running");}
	private static String curTime() {return String.valueOf(java.time.LocalTime.now());}
    private static boolean checkApplicationStatus(String processname)
	{
		try{
			String line;
			String pidInfo ="";
			
			Process p =Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe /FI \"IMAGENAME eq "+processname+".exe\"");
			
			BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ((line = input.readLine()) != null) {
				pidInfo+=line; 
			}
			
			input.close();
			System.out.println("Checking");
			return pidInfo.contains(processname+".exe");
		}
		catch(Exception e)
		{
			fileWriter("Error on "+applicationName+" : At "+curTime()+" : Reason "+e.toString(),ErrorlogPath);
			return false;
		}
	}
	private static void displayHelp(){
		String content="";
		content="\tProcess Montior help menu\n\n";
		content+="\t\targs <ProcessName> <logpath> <errorlogpath> <timeformat>\n\n";
		content+="\t\tExample:\n";
		content+="\t\tjava ProcessMonitor mstsc D:\\Vish\\MSTSC\\ D:\\Vish\\MSTSC\\error d-hh:mm:ss";
		System.out.println(content);
	}
	private static String convertTOHours(long ranseconds)
	{ 
		long day = ranseconds / (long)(24 * 3600); 
	
		ranseconds = ranseconds % (long)(24 * 3600); 
		long hour = ranseconds / (long)3600; 
	
		ranseconds %= (long)3600; 
		long minutes = ranseconds / (long)60 ; 
	
		ranseconds %= (long)60; 
		long seconds = ranseconds;
		
		String returnTmp=timeformat.replaceAll("dd",String.valueOf(day)).replaceAll("hh",String.valueOf(hour)).replaceAll("mm",String.valueOf(minutes)).replaceAll("ss",String.valueOf(seconds));
		
		return returnTmp; 
	} 
}