
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

class ProcessMonitor extends TimerTask {

    static final String HELP_ARGUMENTS[] = {"\\?", "\\h", "help"};
    static final String ARGUMENT_PROCESS_NAME = "--process-name";
    static final String ARGUMENT_LOG = "--log";
    static final String ARGUMENT_LOG_ERROR = "--error-log";
    static final String ARGUMENT_TIME_FORMAT = "--time-format";
    static final String ARGUMENT_LOCK_FILE = "--lockcsvfile";

    static String logPath = System.getProperty("user.dir");
    static String ErrorlogPath = System.getProperty("user.dir");
    static String applicationName;
    static boolean prevStatus;
    static boolean error = false;
    static String timeformat = "dd hh:mm:ss";
    static long prevStatusTime = 0;
    static boolean fileLocked = false;
    static FileOutputStream fs = null;

    static File f_log_path;
    static File f_error_log_path;
    
    static String csv_file_path=logPath + "\\" + curDate() + ".csv";
    
    @Override
    public void run() {
        boolean curStatus = checkApplicationStatus(applicationName);
        if (prevStatus != curStatus) {
            long curTime = curtimeToSec();
            long ranseconds = curTime - prevStatusTime;
            String convertTOHours = convertTOHours(ranseconds);
            System.out.println(curTime + "-" + prevStatusTime);
            fileWriter("STATUS CHANGED AT " + curTime() + ": Monitoring " + applicationName + " Status:" + boolToStatus(curStatus) + " Ran For:" + convertTOHours, logPath);
            fileWriterCSV(boolToStatus(curStatus));
            prevStatus = curStatus;
            prevStatusTime = curtimeToSec();
        } else {
            fileWriter("Status Remain Same " + curTime() + ": Monitoring " + applicationName + " Status:" + boolToStatus(curStatus), logPath);
        }
    }

    private static void fileWriter(String content, String logPath) {
        try (FileOutputStream fsw = new FileOutputStream(logPath + "\\" + curDate() + ".txt", true);) {
            fsw.write((content + "\n").getBytes());
        } catch (Exception e) {
            if (error) {
                e.printStackTrace();
                System.exit(0);
            }
            error = true;
            fileWriter("Error on " + applicationName + " : At " + curTime() + " : Reason " + e.toString(), ErrorlogPath);
            error = false;
        }
    }

    private static void fileWriterCSV(String status) {
        String heading = "";
        File tempFile = new File(csv_file_path);
        long curTime = curtimeToSec();
        long ranseconds = curTime - prevStatusTime;
        String convertTOHours = convertTOHours(ranseconds);
        if (!tempFile.exists()) {
            heading = "processname,time,status,rantime\n";
        }
        if (!fs.getChannel().isOpen()) {
            System.out.println("File locked by another process");
        }
        String content = "" + applicationName + "," + curTime() + "," + status + "," + convertTOHours + "\n";
        try {
            fs.write(((heading != "" ? heading + content : content)).getBytes());
        } catch (Exception e) {
            fileWriter("Error on " + applicationName + " : At " + curTime() + " : Reason " + e.toString(), ErrorlogPath);
        }
    }

    public static void main(String args[]) throws Exception {
        try {

            /*if(args.length<0||args[0]==null||args[0].equals(""))
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
			}	*/
            //String[] test = {"--process-name=cmd", "--lockcsvfile"};
            if (argumentParser(args)) {
                System.exit(0);
            }
            System.out.println(getStatus());
            if(f_log_path!=null&&!f_log_path.exists())
            {
                System.out.println("Creation path:"+f_log_path.toString());
                f_log_path.mkdirs();
            }
            if(f_error_log_path!=null&&!f_error_log_path.mkdirs())
            {
                System.out.println("Creation path:"+f_error_log_path.toString());
                f_error_log_path.mkdirs();
            }
            
            fs = new FileOutputStream(logPath + "\\" + curDate() + ".csv", true);
            
            ProcessMonitor p = new ProcessMonitor();
            Timer t = new Timer();
            prevStatus = checkApplicationStatus(applicationName);
            prevStatusTime = curtimeToSec();
            if (fileLocked) {
                fs.getChannel().lock();
            }
            try {
                fileWriter("Application Started at " + curTime() + ": Monitoring " + applicationName + " Status:" + boolToStatus(prevStatus), logPath);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            try {
                fileWriterCSV(boolToStatus(prevStatus));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            t.scheduleAtFixedRate(p, 0, 5 * 1000);
        } catch (Exception e) {
            fileWriter("Error on " + applicationName + " : At " + curTime() + " : Reason " + e.toString(), ErrorlogPath);
        }
    }

    private static Long curtimeToSec() {
        java.time.LocalDate.now();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        long seconds = date.getTime() / 1000L;
        return new Long(seconds);
    }

    private static String curDate() {
        return String.valueOf(java.time.LocalDate.now());
    }

    private static String boolToStatus(boolean status) {
        return (status ? "Running" : "Not Running");
    }

    private static String curTime() {
        return String.valueOf(java.time.LocalTime.now());
    }

    private static boolean checkApplicationStatus(String processname) {
        try {
            String line;
            String pidInfo = "";

            Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe /FI \"IMAGENAME eq " + processname + ".exe\"");

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((line = input.readLine()) != null) {
                pidInfo += line;
            }

            input.close();
            System.out.println("Checking");
            return pidInfo.contains(processname + ".exe");
        } catch (Exception e) {
            fileWriter("Error on " + applicationName + " : At " + curTime() + " : Reason " + e.toString(), ErrorlogPath);
            return false;
        }
    }

    private static void displayHelp() {
        String content = "";
        content = "\tProcess Montior help menu\n\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=<process_name> [options]\n\n";
        content += "\t\tOptions:\n";
        content += "\t\t\t" + ARGUMENT_LOG + "=<path to save log>: Path to save log file. Default:" + logPath + " \n";
        content += "\t\t\t" + ARGUMENT_LOG_ERROR + "=<path to save error log>:  Path to save error log file. Default:" + ErrorlogPath + " \n";
        content += "\t\t\t" + ARGUMENT_TIME_FORMAT + "=<timeformat>: Date format to display in csv file. Default:" + timeformat + " \n";
        content += "\t\t\t" + ARGUMENT_LOCK_FILE + ": Lock file. prevent file from writing. Default:" + fileLocked + " \n";
        content += "\t\tExample:\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=mstsc\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=mstsc " + ARGUMENT_LOG + "=D:\\Vish\\MSTSC\\\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=mstsc " + ARGUMENT_LOG + "=D:\\Vish\\MSTSC\\ " + ARGUMENT_LOG_ERROR + "=D:\\Vish\\MSTSC\\error\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=mstsc " + ARGUMENT_LOG + "=D:\\Vish\\MSTSC\\ " + ARGUMENT_LOG_ERROR + "=D:\\Vish\\MSTSC\\error " + ARGUMENT_TIME_FORMAT + "=d-hh:mm:ss\n";
        content += "\t\tjava ProcessMonitor " + ARGUMENT_PROCESS_NAME + "=mstsc " + ARGUMENT_LOG + "=D:\\Vish\\MSTSC\\ " + ARGUMENT_LOG_ERROR + "=D:\\Vish\\MSTSC\\error " + ARGUMENT_TIME_FORMAT + "=d-hh:mm:ss " + ARGUMENT_LOCK_FILE + "\n";
        System.out.println(content);
    }

    private static String convertTOHours(long ranseconds) {
        long day = ranseconds / (long) (24 * 3600);

        ranseconds = ranseconds % (long) (24 * 3600);
        long hour = ranseconds / (long) 3600;

        ranseconds %= (long) 3600;
        long minutes = ranseconds / (long) 60;

        ranseconds %= (long) 60;
        long seconds = ranseconds;

        String returnTmp = timeformat.replaceAll("dd", String.valueOf(day)).replaceAll("hh", String.valueOf(hour)).replaceAll("mm", String.valueOf(minutes)).replaceAll("ss", String.valueOf(seconds));

        return returnTmp;
    }

    private static boolean argumentParser(String[] args) {

        if (args.length < 1 || Arrays.toString(HELP_ARGUMENTS).contains(args[0])) {
            displayHelp();
            return true;
        }

        if (!Arrays.toString(args).contains(ARGUMENT_PROCESS_NAME)) {
            System.err.println("Need process name to monitor");
            return true;
        }

        for (String arg : args) {
            String key = arg.split("=")[0];
            String val = "";
            if (arg.contains(("="))) {
                val = arg.split("=")[1];
            }
            switch (key) {
                case ARGUMENT_PROCESS_NAME: {
                    applicationName = val;
                    break;
                }
                case (ARGUMENT_LOG): {
                    logPath = val;
                    f_log_path=new File(logPath);
                    break;
                }
                case (ARGUMENT_LOG_ERROR): {
                    ErrorlogPath = val;
                    f_error_log_path=new File(ErrorlogPath);
                    break;
                }
                case (ARGUMENT_TIME_FORMAT): {
                    timeformat = val;
                    break;
                }
                case (ARGUMENT_LOCK_FILE): {
                    fileLocked = true;
                    break;
                }
            }
        }
        return false;
    }
    private static String getStatus() {
        StringBuilder sb=new StringBuilder();
        sb.append("******************************STATUS******************************");
        sb.append("\nProcess name : "+applicationName);
        sb.append("\nLog path : "+logPath);
        sb.append("\nfile name: "+curDate() + ".txt");
        sb.append("\nError log path: "+ErrorlogPath);
        sb.append("\ntimeformat: "+timeformat);
        sb.append("\nfile locked: "+fileLocked);
        sb.append("\n************************************************************");
        return sb.toString();
    }
}
