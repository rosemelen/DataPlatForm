package dy.udp;

import dy.type.ProcessorType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Component("udpserver")
@Data
@Scope("prototype")
@RequiredArgsConstructor
public class UdpServer {

    private NioDatagramAcceptor acceptor;
    @NonNull
    private ProcessorType processorType;

    @Autowired
    ApplicationContext applicationContext;

    @PostConstruct
    public void initUdpServer() throws Exception{
        acceptor = new NioDatagramAcceptor();
        if(processorType.getType() == 1){
            BlockEventHandle blockEventHandle = (BlockEventHandle)applicationContext.getBean("blockeventhandler",processorType);
            blockEventHandle.setAcceptor(acceptor);
            acceptor.setHandler(blockEventHandle);
        }
        else if(processorType.getType() == 2){
            OldBlockEventHandle oldBlockEventHandle = (OldBlockEventHandle)applicationContext.getBean("oldblockeventhandler",processorType);
            oldBlockEventHandle.setAcceptor(acceptor);
            acceptor.setHandler(oldBlockEventHandle);
        }
        else if(processorType.getType() == 3){
            KtvEventHandle ktvEventHandle = (KtvEventHandle)applicationContext.getBean("ktveventhandler",processorType);
            ktvEventHandle.setAcceptor(acceptor);
            acceptor.setHandler(ktvEventHandle);
        }
        else{
            
            
        }
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);
        acceptor.bind(new InetSocketAddress(processorType.getPort()));
        System.out.println(processorType.getDescription() + " server start listen port:" + processorType.getPort());
    }

    @PreDestroy
    public void stopServer(){
        acceptor.unbind();
    }
}
