package todo.demo.Services;

import todo.demo.Models.PageHtml;

import java.util.List;

public interface WebGenerate {

  String  generateWebs(String promt,String content,String image);
  List<PageHtml> getGeneratedPages();

  List<PageHtml> getGenerated2Pages(Long id);
  PageHtml getPageByName(Long id);

}
