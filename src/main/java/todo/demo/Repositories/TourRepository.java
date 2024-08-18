package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.Tour;

@Repository
public interface TourRepository extends JpaRepository<Tour,Long> {
}
