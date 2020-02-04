

public class Simulator {

    private static int sendCount = 5;
    private static Boolean isDataLeft = true;
    public static int dataToSend = 666;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Main class started.");
    
        //Thread connector class
        final Connection threadConnection = new Connection();

        //Threads
        Thread sender = new Thread(new Runnable(){
        
            @Override
            public void run() {
                //Try sending data 
                while(sendCount > 0) {
                    try {
                        threadConnection.sendData(dataToSend);

                        //Changing data being sent
                        dataToSend = dataToSend * 2;
                        sendCount--;

                        //Simulate delay between requests
                        Thread.sleep(500);
                    }
                    catch(InterruptedException e) {
                        isDataLeft = false;
                        e.printStackTrace();
                    }
                }
                //Tell receiver to stop
                isDataLeft = false;
            }
        });

        Thread receiver = new Thread(new Runnable(){
        
            @Override
            public void run() {
                //Set 10s timeout for receiver thread
                long timeout = System.currentTimeMillis() + 10000;

                //Receive data
                while(isDataLeft) {
                    try {
                        int receivedData = threadConnection.getData();
                        System.out.println("Data received :" + receivedData);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Check for timeout
                    if (System.currentTimeMillis() > timeout) {
                        System.out.println("Receiver timeout.");
                        isDataLeft = false;
                    }
                }
            }
        });

        //Start threads
        receiver.start();
        sender.start();

        //Finish threads
        receiver.join();
        sender.join();

    }
    public static class Connection {
        private int _data;

        //Send data to thread
        public void sendData(int _incomingData) throws InterruptedException {
            synchronized(this) {
                //Save incoming data temporarily
                _data = _incomingData;

                //Network delay
                Thread.sleep(50);

                //Notify waiting getData()
                notify();
            }
        }

        //Get data from thread
        public int getData() throws InterruptedException {
            synchronized(this) {
                //Wait for response, with 5s timeout
                wait(5000);
                int _receivedData = _data;
                //Set default data (to signify fail)
                _data = -1;
                return _receivedData;
            }
        }
    }
}