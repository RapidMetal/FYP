import java.util.Random; 

public class ServerSimulator {

    public static int dataToSend = 666;

    private static int devices = 4;
    private static int sendCount = 5;
    private static int testCases = 10;
    private static Boolean isDataLeft = true;
    private static int networkDelay = 50;
    private static int waitTimeout = 5000;
    private static int requestInterval = 500;

    private static TimeCounter timeCounter;

    public static void main(String[] args) {
        System.out.println("Main class started.");

        timeCounter = new TimeCounter();
    
        //Thread connector class
        final Server mainServer = new Server();

        mainServer.startServer();

    }

    public static class Server{

        public void startServer(){
            try{
                //Array of device connections
                DeviceConnection[] dCons = new DeviceConnection[devices];
                Thread[] connections = new Thread[devices];

                //Making the connections
                for(int i = 0; i < devices; i++){
                    dCons[i] = new DeviceConnection();
                    connections[i] = new Thread(dCons[i]);
                    connections[i].start();
                }

                Random rand = new Random();

                //Loop with randomizer - pick a connection and run - have a delay between connections 
                for(int i = 0; i < testCases; i++){
                    int randIndex = rand.nextInt(devices);
                    System.out.println("Connection index = " + randIndex);
                    dCons[randIndex].threadFun();
                    Thread.sleep(requestInterval);

                    //Control the time here
                    timeCounter.addTime(requestInterval);
                }

                //Close the connections
                for(int i = 0; i < devices; i++){
                    connections[i].join();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }       

    }

    static class DeviceConnection implements Runnable{
        public void run(){
            try{
                System.out.println("Thread created");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void threadFun(){
            try{
                //This is where it responds
                //Add a delay
                Thread.sleep(networkDelay);
                //Print the data to send
                System.out.println(dataToSend);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    } 
}