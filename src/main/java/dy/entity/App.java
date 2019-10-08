package dy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "app")
public class App {
  @Id
  @Column(name = "id", unique = true, nullable = false, length = 6)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "appname", nullable = false, length = 100)
  private String appname;
  @Column(name = "pushaddress", nullable = true, length = 255)
  private String pushaddress;
  @Column(name = "apikey", nullable = true, length = 32)
  private String apikey;
  @Column(name = "createdate", nullable = true)
  private Date createdate;
  @Column(name = "status", nullable = true, length = 11)
  private Long status;
  @Column(name = "userid", nullable = true, length = 11)
  private Long userid;
  @Column(name = "token", nullable = true, length = 255)
  private String token;
  @Column(name = "businessid", nullable = true, length = 11)
  private Long businessid;


}
