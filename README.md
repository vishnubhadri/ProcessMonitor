# ProcessMonitor
A Java application that Monitor and log the Process Start and Stop time.\
It writes state in both CSV and txt format in <b>YYYY-MM-DD</b> format.\
the CSV file holds the application start time, process start time and end time. TXT file holds the state of the process at every time it logs.
The application checks the state of process on every 5 seconds.

Requirements: java 8

## Arguments
```
java ProcessMonitor <process_name> <log_dir> <error_log_dir> <time_format>
```
--process-name=<process_name>:Name of the process to monitor. (without extension) <b>Mandatory</b> \
--log=<log_dir>:The directory that save process log. Default : Current Working Directory \
--error-log=<error_log_dir>:The directory that save error log. Default : Current Working Directory \
--time-format=<time_format>: Return the ran time of the application from previous state. : default: dd hh:mm:ss \
--lockcsvfile: Lock file. prevent file from writing. Default:false \

<b> NOTE: It Frequently check the task manager for the state. It runs on background it consider as running even though you haven't seen them in the screen </b>
<b> NOTE: Time format currently support dd,hh,mm and ss. </b>

## Example:
```
java ProcessMonitor --process-name=mstsc
```

```
java ProcessMonitor --process-name=mstsc --log=D:\Vish\MSTSC\
```

```
java ProcessMonitor --process-name=mstsc --log=D:\Vish\MSTSC\ --error-log=D:\Vish\MSTSC\error
```

```
java ProcessMonitor --process-name=mstsc --log=D:\Vish\MSTSC\ --error-log=D:\Vish\MSTSC\error --time-format=d-hh:mm:ss
```

```
java ProcessMonitor --process-name=mstsc --log=D:\Vish\MSTSC\ --error-log=D:\Vish\MSTSC\error --time-format=d-hh:mm:ss --lockcsvfile
```
