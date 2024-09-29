package todo.demo.Models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PageHtml {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String pageName;
    @Column
    private String pageContent;
    @Column
    private String pageImage;

    public PageHtml(String pageName, String pageContent,String pageImage) {
        this.pageName=pageName;
        this.pageContent=pageContent;
        this.pageImage=pageImage;
    }
}
