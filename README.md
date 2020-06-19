# ProcessMonitor
A Java application that Monitor and log the Process Start and Stop time.\
It writes state in both CSV and txt format in <b>YYYY-MM-DD</b> format.\
the CSV file holds the application start time, process start time and end time. TXT file holds the state of the process at every time it logs.
The application checks the state of process on every 5 seconds.

Requirements: java 8

## Arguments
```
java ProcessMonitor <process_name> <log_dir> <error_log_dir>
```
<process_name>:Name of the process to monitor. (without extension) <b>Mandatory</b> \
<log_dir>:The directory that save process log. Default : Current Working Directory \
<error_log_dir>:The directory that save error log. Default : Current Working Directory


<b> NOTE: It Frequently check the task manager for the state. It runs on background it consider as running even though you haven't seen them in the screen </b>
