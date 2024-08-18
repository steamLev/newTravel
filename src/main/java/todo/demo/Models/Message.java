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
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private Integer orderNo;
    @Column
    private BigDecimal amount;
    @Column
    private Date date;
    @Column
    private Integer clientId;
    @Column
    private Boolean status;
    @Column
    private String notification;
}
