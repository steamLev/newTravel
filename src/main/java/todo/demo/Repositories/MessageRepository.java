package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.Message;

@Repository
public interface MessageRepository  extends JpaRepository<Message,Long> {
}
