package todo.demo.Services.ImplementsWebs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import todo.demo.Services.WebGenerate;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Component
@Qualifier("GenerateWebTravel")
public class GenerateWebTravel implements WebGenerate {

    private final String generatedFolderPath = "src/main/resources/static/generated";
    @Override
    public String generateWebs(String promt,String content) {
        String pageName = promt.substring(7)+".html";
        String filePath = Paths.get(generatedFolderPath, pageName).toString();

        // Создание HTML файла с контентом
        try (FileWriter writer = new FileWriter(new File(filePath), StandardCharsets.UTF_8)) {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html xmlns:th=\"http://www.thymeleaf.org\">\n");
            writer.write("    <style>\n" +
                    "        body {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            font-family: Arial, sans-serif;\n" +
                    "            background-color: #f0f0f0;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            width: 100%;\n" +
                    "            max-width: 1000px;\n" +
                    "            margin: 0 auto;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .image-container {\n" +
                    "            position: relative;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .image {\n" +
                    "            width: 100%;\n" +
                    "            height: auto;\n" +
                    "            display: block;\n" +
                    "        }\n" +
                    "        .image-container-guide {\n" +
                    "            width: 100%;\n" +
                    "            height: auto;\n" +
                    "             position: relative;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            margin-left:100px\n" +
                    "        }\n" +
                    "        .text-block {\n" +
                    "            position: absolute;\n" +
                    "            bottom: 20px;\n" +
                    "            left: 20px;\n" +
                    "            color: white;\n" +
                    "            background-color: rgba(0, 0, 0, 0.5);\n" +
                    "            padding: 10px;\n" +
                    "            border-radius: 5px;\n" +
                    "        }\n" +
                    "        .title {\n" +
                    "            font-size: 24px;\n" +
                    "            font-weight: bold;\n" +
                    "        }\n" +
                    "        .order-form {\n" +
                    "            background-image: url('images/chok.jpg');\n" +
                    "            background-size: cover;\n" +
                    "            padding: 20px;\n" +
                    "            border-radius: 5px;\n" +
                    "            color: #333;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .order-form input, .order-form textarea {\n" +
                    "            width: 100%;\n" +
                    "            padding: 10px;\n" +
                    "            margin: 10px 0;\n" +
                    "            border-radius: 5px;\n" +
                    "            border: none;\n" +
                    "        }\n" +
                    "        .order-form button {\n" +
                    "            padding: 10px 20px;\n" +
                    "            border-radius: 5px;\n" +
                    "            border: none;\n" +
                    "            background-color: #333;\n" +
                    "            color: white;\n" +
                    "            font-size: 16px;\n" +
                    "            cursor: pointer;\n" +
                    "        }\n" +
                    "        .navbar {\n" +
                    "            width: 100%;\n" +
                    "            background-color: #333;\n" +
                    "            overflow: hidden;\n" +
                    "        }\n" +
                    "         .navbar a {\n" +
                    "            float: left;\n" +
                    "            display: block;\n" +
                    "            color: white;\n" +
                    "            text-align: center;\n" +
                    "            padding: 14px 20px;\n" +
                    "            text-decoration: none;\n" +
                    "        }\n" +
                    "        .navbar a:hover {\n" +
                    "            background-color: #ddd;\n" +
                    "            color: black;\n" +
                    "        }\n" +
                    "        .contact {\n" +
                    "            float: right;\n" +
                    "        }\n" +
                    "    </style>");
            writer.write("<head>\n");
            writer.write("    <title>Generated Content</title>\n");
            writer.write(" <meta charset=\"UTF-8\"> </head>\n");
            writer.write("<body>\n");
            writer.write("    <div class=\"container\">\n");
            writer.write("    <div class=\"navbar\"  >\n" +
                            "   <a href=\"/сhat\">На главную </a>\n" +
                            "</div>\n");
            writer.write(" <div class=\"container\">   <p>" + content + "</p> </div>\n");
            writer.write("    </div>\n");
            writer.write("</body>\n");
            writer.write("</html>");

        }catch (Exception e){e.printStackTrace();}

        return pageName.replace(".html", "");

    }

    public List<String> getGeneratedPages(){
        File folder = new File(generatedFolderPath);
        File[] listOfFiles = folder.listFiles();
        List<String> pageNames = new ArrayList<>();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    pageNames.add(file.getName());
                }
            }
        }
        return pageNames;
    }


}
