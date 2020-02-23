import java.util.Random; 
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  

public class ServerSimulator {

    public static int dataToSend = 666;

    private static DeviceConnection[] dCons;

    private static int workerThreadPool = 5;
    private static int devices = 4;
    private static int sendCount = 5;
    private static int testTime = 10;
    private static Boolean isDataLeft = true;
    private static long timeout;

    private static int networkDelay = 200;
    private static int waitTimeout = 5000;
    private static int requestInterval = 500;
    private static int deviceProcessDelay = 20;
    private static int reqGenDelay = 10;
    private static int threadSpawnDelay = 100;

    private static TimeCounter timeCounter;
    private static ExecutorService executor;
    private static Random rand;

    public static void main(String[] args) {
        System.out.println("Main class started.");

        executor = Executors.newFixedThreadPool(workerThreadPool);
        timeCounter = new TimeCounter();
        rand = new Random();
        timeout = System.currentTimeMillis() + testTime*1000;
    
        //Thread connector class
        final Server mainServer = new Server();

        mainServer.startServer();

    }

    public static class Server{

        public void startServer(){
            try{
                //Array of device connections
                dCons = new DeviceConnection[devices];
                Thread[] connections = new Thread[devices];

                //Making the connections
                for(int i = 0; i < devices; i++){
                    dCons[i] = new DeviceConnection();
                    connections[i] = new Thread(dCons[i]);
                    connections[i].start();
                }                

                //Loop with randomizer - pick a connection and run - have a delay between connections 
                for(int i = 0; i < workerThreadPool; i++){
                    Runnable worker = new WorkerThread();
                    executor.execute(worker);   
                    Thread.sleep(threadSpawnDelay);                 
                }

                executor.shutdown();

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

        public void threadFun(long currTime){
            try{
                //This is where it responds
                System.out.println(currTime + " : Request received");

                //Add process delay
                Thread.sleep(deviceProcessDelay);
                System.out.println(timeCounter.getCurrTime() + " : Request processed");

                //Add network delay
                Thread.sleep(networkDelay);
                System.out.println(timeCounter.getCurrTime() + " : Sent response");
                //Print the data to send
                System.out.println(dataToSend);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class WorkerThread implements Runnable{
        @Override
        public void run(){
            try{
                while(System.currentTimeMillis() < timeout){
                    int randIndex = rand.nextInt(devices);
                    System.out.println("Connection index = " + randIndex);

                    //Have a req gen delay
                    System.out.println(timeCounter.getCurrTime() + " : Generating new request");
                    Thread.sleep(reqGenDelay);

                    dCons[randIndex].threadFun(timeCounter.getCurrTime());
                    Thread.sleep(requestInterval);
                }
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}