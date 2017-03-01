import com.chickling.boot.Init;
import com.chickling.util.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by jw6v on 2016/1/11.
 */
public class NotificationTest {


    private static Logger log = LogManager.getLogger("RSMTPA");
    public static void main(String args[]){
        Init.setSqliteName("PrestoJobPortal.sqlite");
        Init.setSiteURLBase("http://localhost:8889");
//        ThreadContext.put("RecipientName", "Jerome.J.Wu@newegg.com");
//        ThreadContext.put("logFileName", "notification");
//        log.info("info");
//        log.error("error");
//        log.error("http://www.google.com");
        String[] recipeints={"jerome.j.wu@newegg.com","eugene.y.yan@newegg.com"};
        StringBuilder email=new StringBuilder();
        email.append("<html><body>"
                + "<table style='border:2px solid black'>");
            email.append("<tr bgcolor=\"#33CC99\">");
            email.append("<td>");
            email.append("123456");
            email.append("</td>");

            email.append("<td>");
            email.append("test");
            email.append("</td>");

            email.append("<td>");
            email.append("test456");
            email.append("</td>");

            email.append("<td>");
            email.append("testqqq");
            email.append("</td>");

            email.append("<tr>");


        email.append("</table></body></html>");
        Notification.notification(2000,email.toString(),"Test",recipeints);
    }
}
