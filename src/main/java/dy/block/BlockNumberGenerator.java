package dy.block;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component("blocknumbergenerator")
@Data
public class BlockNumberGenerator {
    private int blocknum;
    private Object lock;

    public BlockNumberGenerator(){
        blocknum = 0;
        lock = new Object();
    }

    public int getBlockNum(){
        synchronized (lock){
            blocknum += 1;
            if(blocknum > 254){
                blocknum = 0;
            }
        }
        return blocknum;
    }
}
