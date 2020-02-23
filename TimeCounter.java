public class TimeCounter { 

    private static int currTime = 0;

    public static void addTime(int time){
        try{
            currTime += time;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static int getCurrTime(){
        try{
            return currTime;
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return -1;
    }
}