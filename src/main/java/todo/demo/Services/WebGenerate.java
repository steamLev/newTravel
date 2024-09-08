package todo.demo.Services;

import todo.demo.Models.PageHtml;

import java.util.List;

public interface WebGenerate {

  String  generateWebs(String promt,String content);
  List<PageHtml> getGeneratedPages();

  PageHtml getPageByName(Long id);

}
