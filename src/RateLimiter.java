import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RateLimiter {
    //token bucket Rate Limiter using java

    private final int maxBucketSize; // max bucket
    private final int refillTokens; //refill per unit time
    private int currentTokens;

    ScheduledExecutorService executer;

    public RateLimiter(int maxBucketSize, int refill) {
        this.maxBucketSize = maxBucketSize;
        this.refillTokens = refill;
        currentTokens=maxBucketSize;
        executer=new ScheduledThreadPoolExecutor(1);
        //executer=new ScheduledThreadPoolExecutor(1, TimeUnit.SECONDS);
    }

    synchronized boolean isRequestAllowed(){
            if(currentTokens>0){
                currentTokens--;
                System.out.println("Request  Allowed");
                return true;
            }else{
                System.out.println("limit reached. Request Not Allowed");
                return false;
            }
    }

    synchronized void refillTokens(){
        if(currentTokens<maxBucketSize){
            currentTokens=Math.min(currentTokens+refillTokens,maxBucketSize);
        }
    }

    void stop(){
        executer.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter rt=new RateLimiter(5,1);
        rt.executer.scheduleAtFixedRate(()-> rt.refillTokens(),0,1,TimeUnit.SECONDS);

        Runnable task=()->{
            //System.out.println(Thread.currentThread().getName()+" "+);
          rt.isRequestAllowed();
        };

        ScheduledExecutorService executorService=Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(task,0,200,TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(task,0,300,TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        executorService.shutdown();
        rt.stop();
    }

}
