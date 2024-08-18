package todo.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;



@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class OrderTask {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String fkFrom;
    @Column
    private String fkTo;
    @Column
    private String scheduledDate;
    @Column
    private Integer fkTransport;
    @Column
    private Integer clientId;
    @Column
    private BigDecimal amount;
    @Column
    private Boolean status;
    @Column
    private String process;
    @Column
    private String phone;
    @Column
    private String surname;

    public OrderTask(String fkFrom, String fkTo, String phone, String surname) {
        this.fkFrom = fkFrom;
        this.fkTo = fkTo;
        this.phone = phone;
        this.surname = surname;
    }

    public OrderTask(String fkFrom, String fkTo, String scheduled, Integer fkTransport, Integer clientId, BigDecimal amount) {
        this.fkFrom = fkFrom;
        this.fkTo = fkTo;
        this.scheduledDate =  scheduled;
        this.fkTransport = fkTransport;
        this.clientId = clientId;
        this.amount=amount;
    }

    public OrderTask(Long id, String fkFrom, String fkTo, String scheduledDate, Integer fkTransport,String process) {
        this.id = id;
        this.fkFrom = fkFrom;
        this.fkTo = fkTo;
        this.scheduledDate = scheduledDate;
        this.fkTransport = fkTransport;
        this.process=process;
    }

}