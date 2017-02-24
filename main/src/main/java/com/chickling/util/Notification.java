package com.chickling.util;

import com.chickling.DB.ConnectionManager;
import com.chickling.boot.Init;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.ServerConnector;


/**
 * Created by jw6v on 2016/1/11.
 */
public class Notification {
//ToDo: multiple recipients
    private static Logger log= LogManager.getLogger(Notification.class);
    private static String recipients="";
    public synchronized static void notification(int JobHistoryID, String contents,String subject, String[] Recipients){

        String SQLQuery="SELECT Email from `main`.`Job_History`,`main`.`User` Where `JHID`=? and `Job_History`.`JobOwner`=`User`.`UID`";
        PreparedStatement stat = null;
        ResultSet rs = null;
        String to="";
        try{
            stat = ConnectionManager.getInstance().getConnection().prepareStatement(SQLQuery);
            stat.setInt(1,JobHistoryID);
            rs=stat.executeQuery();
            to=rs.getString("Email");
            recipients=to;
            stat.close();
        }
        catch(SQLException ex){
            log.error("Default Job Insert Failed cause: "+ex.toString());
        }


        log.info(Init.getSiteURLBase());
        //Build the hyperlink for the location of logs
        if(JobHistoryID>0) {
            contents = contents + "------<p><a href='" +Init.getSiteURLBase() + "/status#" + JobHistoryID+"'>More Running Status...</a></p>";
        }


        // Recipient's email ID needs to be mentioned.
       // String to = ;
        InternetAddress[] internetAddresses=null;
        //InternetAddress singlrAddress;
        if(Recipients!=null) {

            internetAddresses = new InternetAddress[Recipients.length];
            int count=0;
            for (String r:Recipients) {
                try{
                InternetAddress in=new InternetAddress(r);
                internetAddresses[count]=in;
                    count++;
                }catch(AddressException ae){
                    log.warn("Add the recipient "+r+"failed since "+ae);
                }
            }
        }
        else{
            try{
                internetAddresses=new InternetAddress[1];
            internetAddresses[0]=new InternetAddress(to);
            }
            catch(AddressException e){
                log.info(to +" send failed since that "+e);
                return;
            }
        }
        // Sender's email ID needs to be mentioned
        String from = "test";

        // Assuming you are sending email from localhost
        String host = "st01smtp01.buyabs.corp";
        //String host = "172.16.11.14";
        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        //Session session = Session.getDefaultInstance(properties);
        Session session = Session.getInstance(properties);
        session.setProtocolForAddress("rfc822", "smtp");
        try{
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            //from=System.getProperty("mail.user");
            from=System.getProperty("user.name")+"@"+localMachine.getHostName();
            // Create a default MimeMessage object.
            //from="PrestoJobPortal@st01smtp01.buyabs.corp";
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from,System.getProperty("user.name")));

            // Set To: header field of the header.
            message.addRecipients(Message.RecipientType.TO, internetAddresses);

           // message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setContent(contents, "text/html; charset=UTF-8");

            // Send message
            Transport.send(message);
            log.info("Sent message successfully....");


        }catch (MessagingException | UnknownHostException | UnsupportedEncodingException ex) {
            log.info("Reciever is: "+recipients+" "+ex);
            //ex.printStackTrace();
        }
    }

}
