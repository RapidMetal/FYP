import java.io.*;

//Performs logging to file OutputLog.txt
public class Logger {
    public PrintStream out;
    public Logger() {
        try {
            out = new PrintStream(new File("OutputLog.txt"));
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}