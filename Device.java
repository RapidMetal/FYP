//Represents a single device in network
public class Device implements Runnable{
    private int deviceId;
    private int totalSent;
    private int totalReceived;
    private int lastReceivedValue;
    private Logger logger;

    //Constructor
    // !!! Must use this !!!
    public Device(int _deviceId, Logger _logger) {
        deviceId = _deviceId;
        totalSent = totalReceived = 0;
        lastReceivedValue = -1;
        logger = _logger;
    }

    //reqd run method for Runnable interface
    public void run() {
        //System.out.println("Device " + deviceId + " started.");
    }

    //Process request directly
    public void process() {
        try{
            //Sleep for processing duration
            Thread.sleep(SimulatorAttributes.processRequestDelay);
            //Delay duration in msgSource, msgDest fields
            logger.logEvent((System.currentTimeMillis() - SimulatorAttributes.startTime), deviceId, deviceId, SimulatorAttributes.processRequestDelay, totalSent, SimulatorAttributes.processRequestDelay, totalReceived);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Process request for given time
    public void processForTime(int _delay) {
        try{
            //Sleep for processing duration
            Thread.sleep(_delay);
            //Delay duration in msgSource, msgDest fields
            logger.logEvent((System.currentTimeMillis() - SimulatorAttributes.startTime), deviceId, deviceId, _delay, totalSent, _delay, totalReceived);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Send data
    public void send(int _msgSource, int _msgDestination, Connection _connection) {
        try {
            _connection.sendData(deviceId*1000 + totalSent + 1);
            totalSent++;
            //If request is generated at this node, add generation delay
            if(deviceId == _msgSource) 
                Thread.sleep(SimulatorAttributes.generateRequestDelay);
            //Else add forwarding delay
            else
                Thread.sleep(SimulatorAttributes.forwardRequestDelay);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Receive data
    public void receive(int _msgSource, int _msgDestination, Connection _connection) {
        try {
            lastReceivedValue = _connection.getData();
            totalReceived++;
            logger.logEvent((System.currentTimeMillis() - SimulatorAttributes.startTime), _msgSource, _msgDestination, lastReceivedValue/1000, lastReceivedValue%1000, deviceId, totalReceived);
            //If request is processed at this node, add delay
            if(deviceId == _msgDestination)
                Thread.sleep(SimulatorAttributes.processRequestDelay);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}