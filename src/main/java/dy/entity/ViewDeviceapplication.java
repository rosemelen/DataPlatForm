package dy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "view_deviceapplication")
public class ViewDeviceapplication implements Serializable{
    @Id
    @Column(name = "sn", unique = true, nullable = false, length = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String sn;
    @Column(name = "bussinessid", nullable = true, length = 11)
    private Integer bussinessid;
    @Column(name = "pushurl", nullable = true, length = 20)
    private String pushurl;
    @Column(name = "starttime", nullable = true)
    private Date starttime;
    @Column(name = "endtime", nullable = true)
    private Date endtime;
    @Column(name = "parsektv", nullable = true, length = 3)
    private int parsektv;

}
