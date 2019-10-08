package dy.log;

import lombok.Getter;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component("applogger")
public class AppLogger {
    @Getter private Logger logger;

    public AppLogger(){
        logger = Logger.getLogger(AppLogger.class);
    }
}
