package dy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "deviceconfig")
public class
DeviceConfig {
  @Id
  @Column(name = "id", unique = true, nullable = false, length = 11)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "sn", nullable = false, length = 12)
  private String sn;
  @Column(name = "configid", nullable = true, length = 12)
  private String configid;
  @Column(name = "timeservice", nullable = true, length = 10)
  private String timeservice;
  @Column(name = "configtype", nullable = true, length = 10)
  private String configtype;
  @Column(name = "configdata", nullable = true)
  private String configdata;
  @Column(name = "createdate", nullable = true)
  private Date createdate;
  @Column(name = "status", nullable = true, length = 3)
  private String status;

  public DeviceConfig(String sn, String configid, String status) {
    this.sn = sn;
    this.configid = configid;
    this.status = status;
  }

  public DeviceConfig(String sn, String configid, String configtype, String status) {
    this.sn = sn;
    this.configid = configid;
    this.configtype = configtype;
    this.status = status;
  }

}
