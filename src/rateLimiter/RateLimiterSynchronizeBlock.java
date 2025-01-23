package rateLimiter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RateLimiterSynchronizeBlock {
    //token bucket Rate Limiter using java
    //using synchronize block

    private final int maxBucketSize; // max bucket
    private final int refillTokens; //refill per unit time
    private int currentTokens;

    ScheduledExecutorService executer;

    public RateLimiterSynchronizeBlock(int maxBucketSize, int refill) {
        this.maxBucketSize = maxBucketSize;
        this.refillTokens = refill;
        currentTokens=maxBucketSize;
        executer=new ScheduledThreadPoolExecutor(1);
        executer.scheduleAtFixedRate(()->this.refillTokens(),0,1,TimeUnit.SECONDS);
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
        RateLimiterSynchronizeBlock rt=new RateLimiterSynchronizeBlock(5,1);
        //rt.executer.scheduleAtFixedRate(()-> rt.refillTokens(),0,1,TimeUnit.SECONDS);

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

        //since every thread comes with a different stack of its own. So if we dont stop our scheduled frameworks,
        // frameworks(last 2  above lines), they will run forever.
        // in the current case, our two executor frameworks, total 3 thread will stop.
        //and then our main thread will also stop
    }

}
