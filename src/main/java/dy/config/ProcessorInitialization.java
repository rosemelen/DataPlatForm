package dy.config;

import com.alibaba.fastjson.JSON;
import dy.type.ProcessorType;
import dy.udp.UdpServer;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.processing.Processor;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

@Service
public class ProcessorInitialization {
    @Value("classpath:processor.json")
    private Resource resource;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initProcessor() throws Exception{

        InputStream inputStream = resource.getInputStream();
        try {
            File jsonConfigFile = File.createTempFile("tmp",".json");
            FileUtils.copyInputStreamToFile(inputStream,jsonConfigFile);
            List<ProcessorType> listProcessor = JSON.parseArray(FileUtils.readFileToString(jsonConfigFile,"utf-8"),
                    ProcessorType.class);

            for(ProcessorType processorType:listProcessor){
                UdpServer udpserver = (UdpServer)applicationContext.getBean("udpserver",processorType);
            }
        }finally {
            IOUtils.closeQuietly(inputStream);
        }

    }
}
