import java.io.*;
import java.util.regex.*;

public class DebugTest {
    
    public static void main(String[] args) {
        String filePath = "input_repeated.txt";
        
        try {
            // Читаем файл
            String content = readFile(filePath);
            System.out.println("=== СОДЕРЖИМОЕ ФАЙЛА ===");
            System.out.println(content);
            
            // Тестируем regex
            Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = pattern.matcher(content);
            
            System.out.println("\n=== ПОИСК С REGEX ===");
            int count = 0;
            while (matcher.find()) {
                count++;
                System.out.println("Найдено вхождение " + count + ":");
                System.out.println("  Полное совпадение: " + matcher.group(0));
                System.out.println("  Содержимое массива: " + matcher.group(1));
                System.out.println("  Позиция: " + matcher.start() + "-" + matcher.end());
            }
            
            System.out.println("\n=== ИТОГО ===");
            System.out.println("Найдено вхождений: " + count);
            
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