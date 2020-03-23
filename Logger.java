import java.io.*;

//Performs logging to file OutputLog.txt
public class Logger {

    //For all messages between devices
    private PrintStream eventLogFile;
    //For end to end transmissions
    private PrintStream messageLogFile;
    
    public Logger() {
        try {
            eventLogFile = new PrintStream(new File("EventLog.csv"));
            messageLogFile = new PrintStream(new File("MessageLog.csv"));
            eventLogFile.println("Time, InitSource, FinalDest, MsgSource, SendCount, MsgDest, ReceiveCount");
            messageLogFile.println("StartTime, FinishTime, Source, Destination");
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //Write to Event Log
    public void logEvent(long time, int initSource, int finalDest, int msgSource, int sendCount, int msgDest, int receiveCount) {
        eventLogFile.println(time + ", " + initSource + ", " + finalDest + ", " + msgSource + ", " + sendCount + ", " + msgDest + ", " + receiveCount);
    }
    //Write to Message Log
    public void logMessage(long startTime, long finishTime, int source, int destination) {
        messageLogFile.println(startTime + ", " + finishTime + ", " + source + "," + destination);
    }
}