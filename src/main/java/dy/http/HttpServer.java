package dy.http;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.ws.spi.http.HttpContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

@Data
@NoArgsConstructor
public class HttpServer {

    private com.sun.net.httpserver.HttpServer server;
    @Value(value = "${localHttpPort}")
    private int port;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void runServer() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(port);
        server = com.sun.net.httpserver.HttpServer.create(addr,100);
        server.createContext("/",(HttpEventHandle)applicationContext.getBean("httphandler"));
        server.setExecutor(null);
        server.start();
        System.out.println("http: server start listen port:" + port);
    }

    @PreDestroy
    public void stopServer(){
        server.stop(port);
    }
}
