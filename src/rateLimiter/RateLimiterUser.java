package rateLimiter;

import java.util.concurrent.*;

public class RateLimiterUser {

    //suppose we wanna rate limit particular user
    //for this example, we have 100 users
    //all users will have a maxToken and refilling

    private final int maxBucketSize; // max bucket
    private final int refillTokens; //refill per unit time
    private ConcurrentHashMap<String,Token> userMap; //its shared resoource,
    // it will thread safety for its methods

    ScheduledExecutorService executer;

    private static class Token{
        int currentToken;

        Token(int currentToken){
            this.currentToken=currentToken;
        }
    }

    public RateLimiterUser(int maxBucketSize, int refill) {
        this.maxBucketSize = maxBucketSize;
        this.refillTokens = refill;
        userMap=new ConcurrentHashMap<>();
        executer=new ScheduledThreadPoolExecutor(1);
        //executer=new ScheduledThreadPoolExecutor(1, TimeUnit.SECONDS);
    }

     boolean isRequestAllowed(String userId){
        //case 1- ConcurrentHashMap<String, Integer>
        // we cant use map<String,Integer> because
        //t1,t2 got the remainingToken as 3 simonteniously, concurrentHashMap will allow only one thread access
        // and they will update the remaining token as 2 which is wrong, it should be 1.

        //case 2- ConcurrentHashMap<String, AtomicInteger>
        // we cant this use in this scenario becaus , we cant directly update the of atomicInteger
        // atomicIntObj.compareAndSet(expectedValue, new value), we need to compare before setting
        //so we have to do this in while loop, till the expected value is is met ?? how will it same?
        // still not clear completely

        // case 3 - use synchronize method , it will make the process slow, for the all thread of  all user
        // it will be blocked.

        // case 4 - use  custom static class, that way we dont block the all users,
        // only objects of one users would be blocked in worst case
        Token remainingToken=userMap.computeIfAbsent(userId,a->userMap.put(a,new Token(maxBucketSize)));
        if(remainingToken.currentToken>0){
            remainingToken.currentToken--;
            userMap.put(userId,remainingToken);
            System.out.println("Request  Allowed");
            return true;
        }else{
            System.out.println("limit reached. Request Not Allowed");
            return false;
        }
    }

    void setRefillAllUserTokens(){
        for(String userId: userMap.keySet()){
            isRequestAllowed(userId);
        }
    }
     void refillTokens(){
        if(currentTokens<maxBucketSize){
            currentTokens=Math.min(currentTokens+refillTokens,maxBucketSize);
        }
    }

    void stop(){
        executer.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiterSynchronizeBlock rt=new RateLimiterSynchronizeBlock(5,1);
        rt.executer.scheduleAtFixedRate(()-> rt.refillTokens(),0,1, TimeUnit.SECONDS);

        Runnable task=()->{
            //System.out.println(Thread.currentThread().getName()+" "+);
            rt.isRequestAllowed();
        };

        ScheduledExecutorService executorService= Executors.newScheduledThreadPool(2);
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
