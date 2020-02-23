import java.util.Random;
import java.io.*;

public class P2PSimulator {

    public static long startTime = System.currentTimeMillis();
    private static int _delayBetweenRequests = 50;
    private static int _deviceCount = 8;
    private static int _requestSrc;
    private static int _requestDest;

    public static void main(String[] args) throws InterruptedException,FileNotFoundException {
        System.out.println("Main class started.");

        //Device Threads
        Device[] devices = new Device[_deviceCount];
        Thread[] deviceThreads = new Thread[_deviceCount];
        for(int i=0; i<_deviceCount; i++) {
            devices[i] = new Device(i);
            deviceThreads[i] = new Thread(devices[i]);
            deviceThreads[i].start();
        }

        //Thread connectors
        Connection[][] deviceConnections = new Connection[_deviceCount][_deviceCount];
        for(int i=0; i<_deviceCount; i++) {
            for(int j=i+1; j<_deviceCount; j++) {
                deviceConnections[i][j] = new Connection();
            }
        }

        //Setup Write-To-File
        PrintStream outputFile = new PrintStream(new File("OutputLog.txt"));
        System.setOut(outputFile);

        //Other
        long timeout = System.currentTimeMillis() + 10000;
        Random randomizer = new Random();

        //Run Simulation
        while(System.currentTimeMillis() < timeout) {
            _requestSrc = randomizer.nextInt(_deviceCount);
            _requestDest = randomizer.nextInt(_deviceCount);
            // If generated source is same as destination, redo
            if (_requestSrc == _requestDest)
                continue;
            // Send and Receive requests; Connection arg based on device value for memory optimization
            else if (_requestSrc < _requestDest) {
                devices[_requestSrc].send(_requestSrc, _requestDest, deviceConnections[_requestSrc][_requestDest]);
                devices[_requestDest].receive(_requestSrc, _requestDest, deviceConnections[_requestSrc][_requestDest]);
            }
            else {
                devices[_requestSrc].send(_requestSrc, _requestDest, deviceConnections[_requestDest][_requestSrc]);
                devices[_requestDest].receive(_requestSrc, _requestDest, deviceConnections[_requestDest][_requestSrc]);
            }
            Thread.sleep(_delayBetweenRequests);
        }

        //Finish threads
        for(int i=0; i<_deviceCount; i++) {
            deviceThreads[i].join();
        }
    }

    //Represents a single device in network
    //Make Runnable
    public static class Device implements Runnable{
        private int _deviceId;
        private int _totalSent;
        private int _totalReceived;
        private int _lastReceivedValue;
        private final int _generateRequestDelay;
        private final int _processRequestDelay;

        //Constructor
        // !!! Must use this !!!
        public Device(int deviceId) {
            _deviceId = deviceId;
            _totalSent = _totalReceived = 0;
            _lastReceivedValue = -1;
            _generateRequestDelay = 20;
            _processRequestDelay = 80;
        }

        //reqd run method for Runnable interface
        public void run() {
            System.out.println("Thread with ID " + _deviceId + " started.");
        }

        //Send data
        public void send(int msgSource, int msgDestination, Connection connection) {
            //Finish
            try {
                connection.sendData(_totalSent + 1);
                _totalSent++;
                System.out.println((System.currentTimeMillis() - startTime) + " MsgSource=" + msgSource + ", MsgDest=" + msgDestination + ", sending.");
                Thread.sleep(_generateRequestDelay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Receive data
        public void receive(int msgSource, int msgDestination, Connection connection) {
            //Finish
            try {
                _lastReceivedValue = connection.getData();
                _totalReceived++;
                System.out.println((System.currentTimeMillis() - startTime) + " MsgSource=" + msgSource + ", MsgDest=" + msgDestination + ", " + _lastReceivedValue + " received, total=" + _totalReceived);
                Thread.sleep(_processRequestDelay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //For connection between two devices
    public static class Connection {
        private int _data;
        private Boolean _isNotified;    //Semaphore
        private final int _networkLatency = 50;

        //Constructor
        public Connection() {
            _data = -1;
            _isNotified = false;
        }

        //Send data to thread
        public void sendData(int _incomingData) throws InterruptedException {
            //Set critical section
            synchronized(this) {
                //Save incoming data temporarily
                _data = _incomingData;

                //Notify waiting getData(); semaphore
                _isNotified = true;
                
            }
        }

        //Final kill signal to receiver thread
        public void killReceiver() {
            synchronized(this) {
                //Set data to null
                _data = -1;
                //Notify waiting thread
                notify();
            }
        }

        //Get data from thread
        public int getData() throws InterruptedException {
            //Set Critical Section
            synchronized(this) {
                //Wait for response, with 5s timeout; semaphore
                long _getTimeout = System.currentTimeMillis() + 5000;
                while(!_isNotified && System.currentTimeMillis() <= _getTimeout) {
                    Thread.sleep(5);
                }
                
                //In case of timeout
                if (System.currentTimeMillis() > _getTimeout) {
                    System.out.println("Error: Wait timeout on getData.");
                    return _data;
                }

                //Network delay
                Thread.sleep(_networkLatency);

                int _receivedData = _data;
                //Set default data (to signify fail)
                _data = -1;
                _isNotified = false;
                return _receivedData;
            }
        }
    }
}