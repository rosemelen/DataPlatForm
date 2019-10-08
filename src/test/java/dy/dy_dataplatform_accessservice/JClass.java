package dy.dy_dataplatform_accessservice;

import dy.type.ProcessorType;
import lombok.Data;

import java.util.List;

@Data
public class JClass {
    private List<ProcessorType> udp;
    private List<Object> http;
}
