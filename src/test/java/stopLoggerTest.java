import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.chickling.util.StopLogger;
/**
 * Created by jw6v on 2016/2/15.
 */

public class stopLoggerTest {
    private static Logger log= LogManager.getLogger(stopLoggerTest.class);
    public static void main(String args[]){
        ThreadContext.put("logFileName","testStop");
        log.info("test1");
        log.info("test2");
        StopLogger.stopLogger(log);
        ThreadContext.remove("logFileName");
        while(true);
    }
}
