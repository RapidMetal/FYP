import java.util.Random;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Arrays;


public class BlockchainSimulator {
    private static long timeout;
    private static Random randomizer;
    private static ExecutorService executor;
    static Device[] devices;
    static Connection[][] deviceConnections;
    private static long pollTime;
    private static int[] hasPolled;

    

    

    public static void main(String[] args) throws InterruptedException,FileNotFoundException {

        //Start logger
        Logger logger = new Logger();
        // initialize hasPolled array values as 0
        hasPolled = new int[SimulatorAttributes.deviceCount];
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
        
        // starting polling thread
        Runnable poller = new pollThread();
        Thread polling = new Thread(poller);
        polling.start();

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
                if(System.currentTimeMillis() < timeout) {
                    //Generate request source and destination
                    int requestSrc = randomizer.nextInt(SimulatorAttributes.deviceCount);
                    int requestDest = randomizer.nextInt(SimulatorAttributes.deviceCount);
                    
                    // boolean isFull = new Random().nextInt(SimulatorAttributes.deviceCount)<3; // if true then it is full node
                    boolean doesNodeHaveData = new Random().nextFloat() > SimulatorAttributes.hasData; // if true then dest has data, 50% probability

                 
                    // checking if full node
                    if (requestSrc < SimulatorAttributes.fullNodeMax) {
                          
                        // communicate with dest node
                        if(requestSrc!=requestDest)
                        {
                            System.out.println("Generating request between " + requestSrc + " and " + requestDest);

                            long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                            devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                            devices[requestDest].receive(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                    
                            devices[requestDest].send( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                            devices[requestSrc].receive( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]); 
                            long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                            logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                        }    
                        else
                        {
                            // do process
                            long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                            System.out.println("Generating request between " + requestSrc + " and " + requestSrc);
                            devices[requestSrc].process(); 
                            long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                            logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                        }
                    }
                    
                    else{
                        // node is light and does not have data
                        /* if it doesn't have data
                                1. contact full node
                                2. get its response
                                3. send recv with initial randomly chosen dest node
                        */
                        if(doesNodeHaveData == false){

                            //choosing random full node
                            int randomFullNode = randomizer.nextInt(SimulatorAttributes.fullNodeMax);
                            // commuicating with it
                            System.out.println("Generating request between " + requestSrc + " and " + randomFullNode);

                            long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                            devices[requestSrc].send(requestSrc, randomFullNode, deviceConnections[requestSrc][randomFullNode]);
                            devices[randomFullNode].receive(requestSrc, randomFullNode, deviceConnections[requestSrc][randomFullNode]);
                            
                            
                            devices[randomFullNode].send( requestSrc, randomFullNode, deviceConnections[randomFullNode][requestSrc]);
                            devices[requestSrc].receive( requestSrc, randomFullNode, deviceConnections[randomFullNode][requestSrc]);
                           
                            // communicating with destination node
                            
                            if(requestSrc!=requestDest)
                            {
                                System.out.println("Generating request between " + requestSrc + " and " + requestDest);

                                
                                devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                                devices[requestDest].receive(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                               
                                devices[requestDest].send( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                                devices[requestSrc].receive( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                                long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                            }
                            else
                            {
                               
                                System.out.println("Generating request between " + requestSrc + " and " + requestSrc);
                                devices[requestSrc].process(); 
                                long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                            }
                        }
                        // node is light and has data
                        else{

                            if(requestSrc!=requestDest)
                            {
                                System.out.println("Generating request between " + requestSrc + " and " + requestDest);

                                long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                devices[requestSrc].send(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                                devices[requestDest].receive(requestSrc, requestDest, deviceConnections[requestSrc][requestDest]);
                            
                                devices[requestDest].send( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                                devices[requestSrc].receive( requestSrc, requestDest, deviceConnections[requestDest][requestSrc]);
                                long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                            }
                            else
                            {
                                long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                System.out.println("Generating request between " + requestSrc + " and " + requestSrc);
                                devices[requestSrc].process(); 
                                long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                logger.logMessage(startTime, finishTime, requestDest, requestSrc);
                            }
                        }
                      
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

    static class pollThread implements Runnable{
        @Override
        public void run(){
            
            pollTime = SimulatorAttributes.startTime + SimulatorAttributes.pollInterval;
            while(System.currentTimeMillis() < timeout)
            {
                    try{
                        if(System.currentTimeMillis() > (pollTime + SimulatorAttributes.pollInterval) )
                        {
                           
                            pollTime=System.currentTimeMillis();
                            Arrays.fill(hasPolled,0);

                            int currLightNode = SimulatorAttributes.fullNodeMax;
                            // select random full node
                            int randomFullNode = randomizer.nextInt(SimulatorAttributes.fullNodeMax);

                            while(currLightNode < SimulatorAttributes.deviceCount)
                            {
                                if(hasPolled[currLightNode]==0)
                                {
                                    System.out.println("polling between " + currLightNode + " and " + randomFullNode);
                                    hasPolled[currLightNode]=1;

                                    long startTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                    devices[currLightNode].send(currLightNode, randomFullNode, deviceConnections[currLightNode][randomFullNode]);
                                    devices[randomFullNode].receive(currLightNode, randomFullNode, deviceConnections[currLightNode][randomFullNode]);
                                   
                                    devices[randomFullNode].send( currLightNode, randomFullNode, deviceConnections[randomFullNode][currLightNode]);
                                    devices[currLightNode].receive( currLightNode, randomFullNode, deviceConnections[randomFullNode][currLightNode]);
                                    long finishTime = (System.currentTimeMillis() - SimulatorAttributes.startTime);
                                    logger.logMessage(startTime, finishTime, randomFullNode, currLightNode);
                                    currLightNode++;
                                }
                                else
                                {
                                    currLightNode++;
                                }
                            }                 
                        }
                                
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

            }
            

        }
    }

}