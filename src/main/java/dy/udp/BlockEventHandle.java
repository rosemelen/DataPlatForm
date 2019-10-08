package dy.udp;

import dy.block.BlockProtocolProcessor;
import dy.block.CommonUtil;
import dy.log.AppLogger;
import dy.type.DeviceIpMap;
import dy.type.ProcessorType;
import lombok.*;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Component("blockeventhandler")
@Scope("prototype")
@Data
@RequiredArgsConstructor
public class BlockEventHandle extends IoHandlerAdapter {

    NioDatagramAcceptor acceptor;

    @NonNull
    private ProcessorType processorType;

    @Autowired
    BlockProtocolProcessor blockProtocolProcessor;

    @Autowired
    AppLogger appLogger;

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }


    @Override

    public void messageReceived(IoSession session, Object message) throws Exception {
        if(message instanceof IoBuffer){
            IoBuffer revBuffer = (IoBuffer)message;
            if(revBuffer.hasArray()){
                InetSocketAddress remoteAddr = (InetSocketAddress) session.getRemoteAddress();
                String hostName = remoteAddr.getAddress().getHostAddress();
                int port = remoteAddr.getPort();
                DeviceIpMap deviceIpMap = new DeviceIpMap(processorType,"",hostName,port);
                deviceIpMap.setAcceptor(acceptor);
                appLogger.getLogger().info(String.format("dataplatform receive block data ip: %s port:%s receive data:%s",deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(revBuffer.array())));
                byte[] responseBytes = blockProtocolProcessor.dealBlockData(revBuffer.array(),deviceIpMap);
                appLogger.getLogger().info(String.format("dataplatform receive block data from device : %s",deviceIpMap.getSn()));
                if(responseBytes != null){
                    if(responseBytes.length >0){
                        IoBuffer replyEndResponse= IoBuffer.wrap(responseBytes);
                        WriteFuture wf = session.write(replyEndResponse);
                        appLogger.getLogger().info(String.format("dataplatform send new response to device by block protocol: %s  ip: %s port:%s response:%s",deviceIpMap.getSn(),deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(responseBytes)));
                    }
                } else{
                    appLogger.getLogger().info(String.format("dataplatform block parse error receive data : %s",CommonUtil.byte2HexStr(revBuffer.array())));
                }

            }
        }
        session.close(true);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }
}
