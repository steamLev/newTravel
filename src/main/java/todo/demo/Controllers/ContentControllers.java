package todo.demo.Controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import todo.demo.Models.ChatGpt.ChatGptResponse;
import todo.demo.Models.PageHtml;
import todo.demo.Services.OpenAiService;
import todo.demo.Services.WebGenerate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Controller
@Slf4j
public class ContentControllers {



@Autowired
OpenAiService openAiService;

@Autowired
@Qualifier("GenerateWebTravel")
    WebGenerate webGenerate;
    @GetMapping("/chat")
    public String indexOrder(Model model){
        List<PageHtml> generatedPages = webGenerate.getGeneratedPages();
        model.addAttribute("generatedPages", generatedPages);
      return "chatGpt/createPage";
    }

    @GetMapping("/chat/getPage")
    public String indexOrder(
            @RequestParam(value = "id", required = false) Long id,
                              Model model){

         PageHtml page = webGenerate.getPageByName(id);
        model.addAttribute("page", page.getPageContent());
        model.addAttribute("pageTitle", page.getPageName());
        model.addAttribute("pageImage", page.getPageImage());
        return "pageTravel";
    }


    @PostMapping("/chat/save")
    public String saveContent(@RequestParam("title") String prompt,
                              @RequestParam("content") String content,
                              @RequestParam("image") String image,Model model) throws IOException {

        webGenerate.generateWebs(prompt,content,image);
        return  "redirect:/chat";
    }

    @PostMapping("/chat/generate")
    public String generateContentFront( @RequestParam(value = "prompt", required = false) String prompt,
                                        Model model) throws IOException {
       String  content=  openAiService.getChatGptResponse(prompt ).block();
        model.addAttribute("content", content);
        List<PageHtml> generatedPages = webGenerate.getGeneratedPages();
        model.addAttribute("generatedPages", generatedPages);
        return "chatGpt/createPage";
    }


}

