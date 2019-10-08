package dy.udp;

import dy.block.BlockProtocolProcessor;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IDeviceConfigService;
import dy.entity.DeviceConfig;
import dy.ktv.CommunicationKtvProcessor;
import dy.log.AppLogger;
import dy.type.DeviceIpMap;
import dy.type.PostConfigData;
import dy.type.ProcessorType;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("ktveventhandler")
@Scope("prototype")
@Data
@RequiredArgsConstructor
public class KtvEventHandle extends IoHandlerAdapter {

    NioDatagramAcceptor acceptor;

    @NonNull
    ProcessorType processorType;

    @Autowired
    CommunicationKtvProcessor communicationKtvProcessor;

    @Autowired
    AppLogger appLogger;

    @Autowired
    Appcache appcache;

    @Autowired
    IDeviceConfigService deviceConfigService;

    static final int MINKTVFRAMELEN = 20;


    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if(message instanceof IoBuffer){
            IoBuffer revBuffer = (IoBuffer)message;
            if(revBuffer.hasArray()){
                if(revBuffer.array().length >= MINKTVFRAMELEN){
                    InetSocketAddress remoteAddr = (InetSocketAddress) session.getRemoteAddress();
                    String hostName = remoteAddr.getAddress().getHostAddress();
                    int port = remoteAddr.getPort();
                    DeviceIpMap deviceIpMap = new DeviceIpMap(processorType,"",hostName,port);
                    deviceIpMap.setAcceptor(acceptor);
                    appLogger.getLogger().info(String.format("dataplatform receive RTU data from device ip: %s port:%s receive data:%s",deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(revBuffer.array())));
                    byte[] responseBytes = communicationKtvProcessor.dealKtvkData(revBuffer.array(),deviceIpMap);
                    appLogger.getLogger().info(String.format("dataplatform receive RTU data from device : %s",deviceIpMap.getSn()));
                    if(responseBytes != null){
                        if(responseBytes.length >0){
                            IoBuffer replyEndResponse= IoBuffer.wrap(responseBytes);
                            WriteFuture wf = session.write(replyEndResponse);
                            appLogger.getLogger().info(String.format("dataplatform send RTU response to device: %s  ip: %s port:%s response:%s by ktv protocol",deviceIpMap.getSn(),deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(responseBytes)));
                        }
                    }else{
                        appLogger.getLogger().info(String.format("dataplatform parse error receive RTU data : %s",CommonUtil.byte2HexStr(revBuffer.array())));
                    }

                    if(appcache.getDeviceConfigMap().containsKey(deviceIpMap.getSn())){
                        ConcurrentHashMap<String,PostConfigData> postConfigDataConcurrentHashMap = appcache.getDeviceConfigMap().get(deviceIpMap.getSn());
                        if(postConfigDataConcurrentHashMap != null){
                            appcache.getDeviceConfigMap().remove(deviceIpMap.getSn());
                            for(Map.Entry entry:postConfigDataConcurrentHashMap.entrySet()){
                                PostConfigData postConfigData = (PostConfigData) entry.getValue();
                                IoBuffer replyConfig= IoBuffer.wrap(postConfigData.getDeviceConfigData().getConfighexdata());
                                WriteFuture wf = session.write(replyConfig);
                                appLogger.getLogger().info(String.format("dataplatform send RTU config to device sn: %s  ip: %s port:%s configdata:%s",deviceIpMap.getSn(),deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(postConfigData.getDeviceConfigData().getConfighexdata())));

                                //更改配置状态
//                                appcache.getDeviceConfigList().addObj(new DeviceConfig(postConfigData.getSn(),postConfigData.getConfigId(),"设备配置已发送"));
                                appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(postConfigData.getSn(),postConfigData.getConfigId(),"设备配置已发送"));

                            }
                        }
                    }

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
