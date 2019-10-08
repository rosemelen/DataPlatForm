package dy.dy_dataplatform_accessservice;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Administrator on 2017/6/21.
 */
public class UdpClient {

    private String hostName;
    private int port;

    public UdpClient(String host, int port){
        this.hostName = host;
        this.port = port;
    }

    public static void sendUdpData(String host, int localPort, int remotePort, byte[] data) throws Exception{
        //随机端口发送
        DatagramSocket socket = new DatagramSocket(localPort);
        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, remotePort);
        socket.send(packet);
        socket.close();
    }

}
