import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SimpleNumberExample {
    
    public static void main(String[] args) {
        String filePath = "input_numbers.txt";
        
        try {
            // Читаем файл
            String content = readFile(filePath);
            
            // ПРАВИЛЬНЫЙ REGEX для чисел: "partyIds":[111,111,222,33333]
            Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = pattern.matcher(content);
            
            if (matcher.find()) {
                String idsString = matcher.group(1);
                System.out.println("Найденная строка: " + idsString);
                
                // Разделяем по запятым и очищаем от пробелов
                String[] ids = idsString.split(",");
                List<String> partyIds = new ArrayList<>();
                
                for (String id : ids) {
                    String cleanId = id.trim(); // Убираем только пробелы
                    partyIds.add(cleanId);
                }
                
                System.out.println("Извлеченные partyIds: " + partyIds);
                System.out.println("Количество partyIds: " + partyIds.size());
                
                // Дополнительно: конвертируем в числа
                List<Integer> numbers = new ArrayList<>();
                for (String id : partyIds) {
                    try {
                        numbers.add(Integer.parseInt(id));
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка парсинга числа: " + id);
                    }
                }
                System.out.println("Как числа: " + numbers);
                
            } else {
                System.out.println("partyIds не найдены в файле");
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}