//Class containing program wide constants
public class SimulatorAttributes {
    //Simulation Runtime in Seconds
    public static final int simulationRunningTime = 10;
    //Request Generation Delay
    public static final int delayBetweenRequests = 50;
    //Number of request handler threads
    public static final int workerThreadPoolSize = 5;
    //Number of Devices
    public static final int deviceCount = 8;
    //Simulation Start Time (internal)
    public static final long startTime = System.currentTimeMillis();
    //Request Generation Delay (at Src)
    public static final int generateRequestDelay = 40;
    //Request Process Delay (at Dest)
    public static final int processRequestDelay = 100;
    //Request Forwarding Delay (at Intermediary)
    public static final int forwardRequestDelay = 20;
    //Network latency
    public static final int networkLatency = 50;
    
}