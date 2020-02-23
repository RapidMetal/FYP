public class TimeCounter { 

    private static long startTime;

    public TimeCounter(){
        startTime = System.currentTimeMillis();
    }

    public static long getCurrTime(){
        try{
            return System.currentTimeMillis() - startTime;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }
}