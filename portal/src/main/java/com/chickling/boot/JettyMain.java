package com.chickling.boot;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Created by ey67 on 2015/11/26.
 */
public class JettyMain {

   public static  void main(String [] args) throws Exception {
       final int port = 8889;
       Server server = new Server(port);
       WebAppContext app = new WebAppContext();
       app.setContextPath("/web/");
       ProtectionDomain domain = JettyMain.class.getProtectionDomain();
       URL warLocation = domain.getCodeSource().getLocation();
       app.setDescriptor(warLocation.getPath() + "web.xml");
       System.out.println(warLocation.getPath() + "web.xml");
       app.setServer(server);
       app.setWar(warLocation.toExternalForm());
       server.setHandler(app);
       server.start();
       server.join();
   }
}
