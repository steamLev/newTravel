package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.Cred;
import todo.demo.Models.Message;

@Repository
public interface CredRepository  extends JpaRepository<Cred,Long> {
}
