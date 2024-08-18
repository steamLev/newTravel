package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.OrderTask;

@Repository
public interface OrderRepository extends JpaRepository<OrderTask,Long> {
}
