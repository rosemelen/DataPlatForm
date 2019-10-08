package dy.udp;

import dy.block.BlockProtocolProcessor;
import dy.block.CommonUtil;
import dy.cache.Appcache;
import dy.dbserivce.IDeviceConfigService;
import dy.entity.DeviceConfig;
import dy.log.AppLogger;
import dy.type.DeviceIpMap;
import dy.type.PostConfigData;
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

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component("oldblockeventhandler")
@Scope("prototype")
@Data
@RequiredArgsConstructor
public class OldBlockEventHandle extends IoHandlerAdapter {

    NioDatagramAcceptor acceptor;

    @NonNull
    private ProcessorType processorType;

    @Autowired
    BlockProtocolProcessor blockProtocolProcessor;

    @Autowired
    AppLogger appLogger;

    @Autowired
    Appcache appcache;

    @Autowired
    IDeviceConfigService deviceConfigService;

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
                //正常块协议数据传输处理
                appLogger.getLogger().info(String.format("old bolck received data : %s ip:%s port:%s", CommonUtil.byte2HexStr(revBuffer.array()),hostName,port));
                byte[] responseBytes = blockProtocolProcessor.dealBlockData(revBuffer.array(),deviceIpMap);
                appLogger.getLogger().info(String.format("dataplatform receive old block data from device : %s",deviceIpMap.getSn()));
                if(responseBytes != null){
                    if(responseBytes.length >0){
                        IoBuffer replyEndResponse= IoBuffer.wrap(responseBytes);
                        WriteFuture wf = session.write(replyEndResponse);
                        appLogger.getLogger().info(String.format("dataplatform send response to device by old block protocol: %s  ip: %s port:%s response:%s",deviceIpMap.getSn(),deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(responseBytes)));
                    }
                }else{
                    appLogger.getLogger().info(String.format("dataplatform old block parse error receive data : %s",CommonUtil.byte2HexStr(revBuffer.array())));
                }

                //旧协议设备配置查询
                List<PostConfigData> listSendConfigData = blockProtocolProcessor.getOldConfigList(deviceIpMap.getSn());

                //如果有该设备的配置，则首先发送配置给设备
                if(listSendConfigData != null){
                    if (listSendConfigData.size() > 0){
                        Collections.sort(listSendConfigData,Comparator.comparing(PostConfigData::getDate));
                        for(PostConfigData deviceConfigData : listSendConfigData){
                            byte[] deviceConfigBytes = blockProtocolProcessor.getOldConfigData(deviceConfigData);
                            if(deviceConfigBytes != null){
                                if(deviceConfigBytes.length >0){
                                    IoBuffer sendOldDeviceConfig= IoBuffer.wrap(deviceConfigBytes);
                                    WriteFuture wf = session.write(sendOldDeviceConfig);
                                    appLogger.getLogger().info(String.format("dataplatform send config to device by old block protocol sn: %s  ip: %s port:%s configdata:%s",deviceIpMap.getSn(),deviceIpMap.getIp(),deviceIpMap.getPort(),CommonUtil.byte2HexStr(deviceConfigBytes)));

                                    //更改配置状态
                                    //appcache.getDeviceConfigList().addObj(new DeviceConfig(deviceConfigData.getSn(),deviceConfigData.getConfigId(),"设备配置已发送"));
                                    appcache.getDeviceConfigDbOperationList().addObj(new DeviceConfig(deviceConfigData.getSn(),deviceConfigData.getConfigId(),"设备配置已发送"));
                                }
                            }
                        }
                    }
                    listSendConfigData.clear();
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
