import java.util.Random;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ConsensusSimulator {
    private static long timeout;
    private static Random randomizer;
    private static ExecutorService executor;
    static Device[] devices;
    static Connection[][] deviceConnections;


    public static void main(String[] args) throws InterruptedException,FileNotFoundException {

        //Start logger
        Logger logger = new Logger();

        //Device Threads
        devices = new Device[SimulatorAttributes.deviceCount];
        Thread[] deviceThreads = new Thread[SimulatorAttributes.deviceCount];
        for(int i=0; i<SimulatorAttributes.deviceCount; i++) {
            devices[i] = new Device(i,logger);
            deviceThreads[i] = new Thread(devices[i]);
            deviceThreads[i].start();
        }

        //Thread connectors -> assuming 2 way connections between devices
        deviceConnections = new Connection[SimulatorAttributes.deviceCount][SimulatorAttributes.deviceCount];
        for(int i=0; i<SimulatorAttributes.deviceCount; i++) {
            for(int j=0; j<SimulatorAttributes.deviceCount; j++) {
                if (i != j) {
                    deviceConnections[i][j] = new Connection(i,j);
                }
            }
        }

        //Set timeout value
        timeout = SimulatorAttributes.startTime + 1000*SimulatorAttributes.simulationRunningTime;
        //Instantiate randomizer
        randomizer = new Random();

        //Make thread pool
        executor = Executors.newFixedThreadPool(SimulatorAttributes.workerThreadPoolSize);

        //Start request generation threads
        for(int i=0; i<SimulatorAttributes.workerThreadPoolSize; i++) {
            //Actual worker thread
            Runnable worker = new WorkerThread();
            //Run thread
            executor.execute(worker);
            //Wait between thread starts
            Thread.sleep(SimulatorAttributes.delayBetweenRequests);
        }

        //Finish threads
        executor.shutdown();
        for(int i=0; i<SimulatorAttributes.deviceCount; i++) {
            deviceThreads[i].join();
        }
    }

    //Worker Thread for Request generation and handling
    //Need to change workerThreadPoolSize in SimulatorAttributes according to time taken per request.
    static class WorkerThread implements Runnable{
        @Override
        public void run(){
            try{
                while(System.currentTimeMillis() < timeout) {
                    //Generate request source and destination
                    //Src is the node that gets the request from user
                    //Dst is the node that is the target of the request
                    int requestSrc = randomizer.nextInt(SimulatorAttributes.deviceCount);
                    int requestDest = randomizer.nextInt(SimulatorAttributes.deviceCount);

                    if (requestSrc == requestDest) {
                        //Handle for direct response
                    }
                    //Handle routing and final response
                    else {
                        /*System.out.println("Generating request between " + requestSrc + " and " + requestDest);
                        devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                        devices[requestDest].receive(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);*/
                        for(int i = 0; i < SimulatorAttributes.deviceCount; i++){
                            if(requestSrc != i){
                                System.out.println("Generating request between " + requestSrc + " and " + i);
                                devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][i]);
                                devices[i].receive(requestSrc, requestDest, deviceConnections[requestSrc][i]);
                            }
                        }
                        
                        for(int i = 0; i < SimulatorAttributes.deviceCount; i++){
                            if(i != requestDest){
                                System.out.println("Generating request between " + i + " and " + requestDest);
                                devices[i].send(requestSrc, requestDest, deviceConnections[i][requestDest]);
                                devices[requestDest].receive(requestSrc, requestDest, deviceConnections[i][requestDest]);
                            }
                        }

                        System.out.println("Generating request between " + requestDest + " and " + requestSrc);
                        devices[requestDest].send(requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                        devices[requestSrc].receive(requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                    }

                    //Randomize sleep times -> between 0 to 2*delay*poolSize
                    int waitTime = randomizer.nextInt(2 * SimulatorAttributes.delayBetweenRequests * SimulatorAttributes.workerThreadPoolSize);
                    Thread.sleep(waitTime);
                }
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}