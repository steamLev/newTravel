package todo.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import todo.demo.Models.PageHtml;

@Repository
public interface PageHtmlRepository extends JpaRepository< PageHtml,Long> {
    String findByPageName(String pageName);
}
