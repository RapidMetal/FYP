import java.util.Random;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class ICNSimulator {
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
            Runnable worker = new WorkerThread(logger);
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
        private Logger logger;

        public WorkerThread(Logger _logger) {
            logger = _logger;
        }
        @Override
        public void run(){
            try{
                while(System.currentTimeMillis() < timeout) {
                    //Generate request source and destination
                    int requestSrc = randomizer.nextInt(SimulatorAttributes.deviceCount);
                    int requestDest = randomizer.nextInt(SimulatorAttributes.deviceCount);

                    //ICN 3 cases ( 0 is server)
                    // 1. user sends req to intended dest
                    // 2. central node contacts intended dest
                    // 3. source contacts central node
                    // assume 0 is server
                    if (requestSrc == requestDest) {
                        //Handle for direct response
                        System.out.println("Generating request between " + requestSrc + " and " + requestSrc);
                        // deviceid check 
                        long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        devices[requestSrc].process();
                        long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                    }
                    //Handle routing and final response
                    // central node contacts dest node and node responds
                    else if(requestSrc == 0 || requestDest == 0 ){
                       
                        System.out.println("Generating request between " + requestSrc + " and " + requestDest);
                        long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                        devices[requestDest].receive(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);

                        devices[requestDest].send( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                        devices[requestSrc].receive( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                        long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                    }

                    else{
                        System.out.println("Generating request between " + requestSrc + " and " + requestDest);
                        long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][0]);
                        devices[0].receive(requestSrc, requestDest, deviceConnections[requestSrc][0]);

                        devices[0].send(requestSrc, requestDest, deviceConnections[0][requestDest]);
                        devices[requestDest].receive(requestSrc, requestDest, deviceConnections[0][requestDest]);

                        devices[requestDest].send( requestSrc, requestDest, deviceConnections[requestDest][0]);
                        devices[0].receive(requestSrc, requestDest, deviceConnections[requestDest][0]);

                        devices[0].send(requestSrc, requestDest, deviceConnections[0][requestSrc]);
                        devices[requestSrc].receive(requestSrc, requestDest, deviceConnections[0][requestSrc]);
                        long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                        logger.logMessage(startTime, finishTime, requestDest, requestSrc);
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