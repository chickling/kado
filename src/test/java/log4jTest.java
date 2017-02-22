/**
 * Created by jw6v on 2015/12/15.
 */

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.IOException;

public class log4jTest {
    static org.apache.logging.log4j.Logger log = LogManager.getLogger(log4jTest.class);

    public static void main(String args[]) throws IOException{
        ThreadContext.put("logFileName","ppp");
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        log.error("1111111111111111");
        RollingFileAppender app= (RollingFileAppender) config.getAppender("getLogDir");
        File dir = new File(app.getFileName().replaceFirst("[^\\/]+$", ""));
        int pause=0;
//        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//        final Configuration config = ctx.getConfiguration();

        Layout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, config, null, null,true,true,"","");
        Appender appender = FileAppender.createAppender("logs/test.log", "false", "false", "File", "true", "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);


        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, "org.apache.logging.log4j", "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("org.apache.logging.log4j", loggerConfig);
        ctx.updateLoggers();



        log.debug("Here is some DEBUG1");
        log.info("Here is some INFO2");
        log.warn("Here is some WARN3");
        log.error("Here is some ERROR4");
        log.fatal("Here is some FATAL5");
    }
}
