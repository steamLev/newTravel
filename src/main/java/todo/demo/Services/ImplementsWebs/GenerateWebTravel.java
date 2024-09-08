package todo.demo.Services.ImplementsWebs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public String generateWebs(String promt,String content) {
        String pageName = promt.substring(7)+".html";

        // Создание HTML файла с контентом
       StringBuilder pageContent=new StringBuilder();

        pageContent.append(  content  );
  pageHtmlRepository.save(new PageHtml(pageName,pageContent.toString()));
        return pageName;

    }

    @Override
    public List<PageHtml> getGeneratedPages(){

        return pageHtmlRepository.findAll().stream()
                .map(page->new PageHtml(page.getId(),page.getPageName(),"")).collect(Collectors.toList());
    }

    @Override
    public PageHtml getPageByName(Long pageName){
       return pageHtmlRepository.findById(pageName).get();
    }



}
