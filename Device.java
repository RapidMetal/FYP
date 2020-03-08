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
        System.out.println("Device " + deviceId + " started.");
    }

    //Send data
    public void send(int _msgSource, int _msgDestination, Connection _connection) {
        //Finish
        try {
            _connection.sendData(deviceId*100 + totalSent + 1);
            totalSent++;
            //If request is generated at this node, add generation delay
            if(deviceId == _msgSource) 
                Thread.sleep(SimulatorAttributes.generateRequestDelay);
            //Else add forwarding delay
            else
                Thread.sleep(SimulatorAttributes.forwardRequestDelay);
            //Log msg send
            logger.out.println((System.currentTimeMillis() - SimulatorAttributes.startTime) + " MsgSource=" + _msgSource + ", MsgDest=" + _msgDestination + ", sending.");
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Receive data
    public void receive(int _msgSource, int _msgDestination, Connection _connection) {
        //Finish
        try {
            lastReceivedValue = _connection.getData();
            totalReceived++;
            logger.out.println((System.currentTimeMillis() - SimulatorAttributes.startTime) + " MsgSource=" + _msgSource + ", MsgDest=" + _msgDestination + ", " + lastReceivedValue + " received, total=" + totalReceived);
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