package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.Message;
import todo.demo.Models.PayBill;

@Repository
public interface PayBillRepository extends JpaRepository<PayBill,Long> {
}
