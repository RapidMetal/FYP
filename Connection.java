import java.util.*;

//For network connection between two devices
//Synchronized -> Queue length acts as semaphore for producer - consumer
public class Connection {
    Queue<Integer> data = new LinkedList<Integer>();
    private int sender;
    private int receiver;

    //Constructor
    public Connection(int _sender, int _receiver) {
        sender = _sender;
        receiver = _receiver;
    }

    //Send data to thread
    public void sendData(int _incomingData) throws InterruptedException {
        //Set critical section
        synchronized(this) {
            //Save incoming data
            data.add(_incomingData);
            
        }
    }

    //Final kill signal to receiver thread
    public void killReceiver() {
        synchronized(this) {
            //Set data to null
            data.add(-1);
        }
    }

    //Get data from thread
    public int getData() throws InterruptedException,NoSuchElementException {
        //Set Critical Section
        synchronized(this) {
            //Wait for response, with 5s timeout; semaphore
            long _getTimeout = System.currentTimeMillis() + 5000;
            while(data.isEmpty() && System.currentTimeMillis() <= _getTimeout) {
                Thread.sleep(5);
            }
            
            //In case of timeout or if killed(element is -1)
            if (System.currentTimeMillis() > _getTimeout || data.peek() == -1 ) {
                System.out.println("Error: Wait timeout on getData. Sender = " + sender + ", Receiver = " + receiver + ".");
                return data.remove();
            }

            //Network delay
            Thread.sleep(SimulatorAttributes.networkLatency);
            return data.poll();
        }
    }
}