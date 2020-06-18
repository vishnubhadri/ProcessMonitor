import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Stack;
import java.util.Arrays;
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
	static Stack<Long> stack=new Stack<>();
	@Override
	public void run() {
		boolean curStatus=checkApplicationStatus(applicationName);
		if(prevStatus!=curStatus)
		{
			fileWriter("STATUS CHANGED AT "+curTime()+": Monitoring "+applicationName+" Status:"+boolToStatus(curStatus),logPath);
			fileWriterCSV(boolToStatus(curStatus));
			prevStatus=curStatus;
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
		if(!tempFile.exists()){
			heading="processname,time,status\n";
		}
		String content=""+applicationName+","+curTime()+","+status+"\n";
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
				logPath=args[1];
			}	
			ProcessMonitor p=new ProcessMonitor();
			Timer t=new Timer();
			prevStatus=checkApplicationStatus(applicationName);
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
			
			if(prevStatus)
			{
				stack.push(curtimeToSec());
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
			
			Process p =Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");
			
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
		content+="\t\targs <ProcessName> <logpath> <errorlogpath>\n\n";
		content+="\t\tExample:\n";
		content+="\t\tjava ProcessMonitor mstsc D:\\Vish\\MSTSC\\ D:\\Vish\\MSTSC\\error";
		System.out.println(content);
	}
}