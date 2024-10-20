package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import todo.demo.Models.PageHtml;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface PageHtmlRepository extends JpaRepository< PageHtml,Long> {
    String findByPageName(String pageName);

}
