package todo.demo.Services.ImplementsWebs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import todo.demo.Models.PageHtml;
import todo.demo.Repositories.PageHtmlRepository;
import todo.demo.Services.WebGenerate;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Qualifier("GenerateWebTravel")

public class GenerateWebTravel implements WebGenerate {
    @Autowired
    PageHtmlRepository pageHtmlRepository;
    @Override
    public String generateWebs(String promt,String content,String image) {
        String pageName = promt;

        // Создание HTML файла с контентом
       StringBuilder pageContent=new StringBuilder();

        pageContent.append(  content  );
  pageHtmlRepository.save(new PageHtml(pageName,pageContent.toString(),image));
        return pageName;

    }

    @Override
    public List<PageHtml> getGeneratedPages(){

        return pageHtmlRepository.findAll().stream()
                .map(page->new PageHtml(page.getId(),page.getPageName(),"", page.getPageImage())).collect(Collectors.toList());
    }

    @Override
    public List<PageHtml> getGenerated2Pages(Long id){
        Pageable pageable = PageRequest.of(0, 3);
        return pageHtmlRepository.findAll(pageable).stream()
                .filter(page-> page.getId()!= id)
                .map(page->new PageHtml(page.getId(),page.getPageName(),"", page.getPageImage())).collect(Collectors.toList());
    }

    @Override
    public PageHtml getPageByName(Long pageName){
       return pageHtmlRepository.findById(pageName).get();
    }



}
