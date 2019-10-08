package dy.type;

import dy.type.ProcessorType;
import lombok.*;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class DeviceIpMap {
    @NonNull
    private ProcessorType processorType;
    @NonNull
    private String sn;
    @NonNull
    private String ip;
    @NonNull
    private int port;
    private NioDatagramAcceptor acceptor;
}
