package dy.entity;

import com.alibaba.fastjson.annotation.JSONField;
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
@Table(name = "view_accessservice_querytask")
public class ViewAccessServiceQuery {
    @Id
    @Column(name = "sn", unique = true, nullable = false, length = 20)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String sn;

//    @JSONField(serialize = false)
//    @Column(name = "devicesn")
//    private String devicesn;

    @Column(name = "businessid", nullable = true, length = 11)
    private Integer businessid;

    @Column(name = "starttime", nullable = true)
    private Date starttime;

    @Column(name = "endtime", nullable = true)
    private Date endtime;

    @Column(name = "ktvgroupid", nullable = true, length = 11)
    private Integer ktvgroupid;

    @Column(name = "pushaddress", nullable = true, length = 255)
    private String pushaddress;

    @Column(name = "busikey", nullable = true, length = 32)
    private String busikey;

    @Column(name = "app", nullable = true, length = 11)
    private Integer app;

    @Column(name = "coapdeviceid", nullable = true, length = 255)
    private String coapdeviceid;
}
