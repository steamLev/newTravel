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
public class PayBill {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private Integer orderNo;
    @Column
    private Date date;
    @Column
    private Integer clientId;
    @Column
    private BigDecimal amount;
    @Column
    private Boolean status;
}
