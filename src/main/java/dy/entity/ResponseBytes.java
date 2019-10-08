package dy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseBytes {
  private   boolean isReturnData;
  private   byte[] responseBytes;
}
