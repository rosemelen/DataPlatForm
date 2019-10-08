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
@Table(name = "business")
public class Business {
  @Id
  @Column(name = "id", unique = true, nullable = false, length = 11)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "name", nullable = true, length = 50)
  private String name;
  @Column(name = "memo", nullable = true, length = 255)
  private String memo;
  @Column(name = "createdate", nullable = true)
  private Date createdate;
  @Column(name = "userid", nullable = true, length = 11)
  private Long userid;
  @Column(name = "status", nullable = true, length = 11)
  private Long status;
  @Column(name = "icon", nullable = true, length = 255)
  private String icon;
  @Column(name = "starttime", nullable = true)
  private Date starttime;
  @Column(name = "endtime")
  private Date endtime;
  @Column(name = "productid", nullable = true, length = 11)
  private Long productid;
  @Column(name = "busikey", nullable = true, length = 50)
  private String key;

}
