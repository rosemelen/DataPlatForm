package dy.block;

import dy.block.type.ENUM_BLOCKCONTROL;
import dy.block.type.ENUM_BLOCKSIGN;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockData {
    private String sn;
    private ENUM_BLOCKSIGN blocksign;
    private ENUM_BLOCKCONTROL blockcontrol;
    private int blockNum;
    private int frameNum;
    private int frameLen;
    private byte[] dataBytes;
}
