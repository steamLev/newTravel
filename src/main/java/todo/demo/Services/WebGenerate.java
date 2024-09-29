package todo.demo.Services;

import todo.demo.Models.PageHtml;

import java.util.List;

public interface WebGenerate {

  String  generateWebs(String promt,String content,String image);
  List<PageHtml> getGeneratedPages();

  PageHtml getPageByName(Long id);

}
