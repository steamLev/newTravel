import java.io.*;
import java.util.regex.*;

/**
 * Простой класс для подсчета количества partyIds в файле
 */
public class PartyIdCounter {
    
    /**
     * Подсчитывает количество partyIds в файле
     * @param filePath путь к файлу
     * @return количество найденных partyIds
     */
    public static int countPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        
        // Регулярное выражение для поиска partyIds
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            // Подсчитываем количество запятых + 1 = количество элементов
            int count = 1;
            for (char c : idsString.toCharArray()) {
                if (c == ',') {
                    count++;
                }
            }
            return count;
        }
        
        return 0;
    }
    
    /**
     * Альтернативный метод подсчета через разделение строки
     */
    public static int countPartyIdsAlternative(String filePath) throws IOException {
        String content = readFile(filePath);
        
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            return ids.length;
        }
        
        return 0;
    }
    
    /**
     * Читает файл и возвращает его содержимое
     */
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
    
    /**
     * Главный метод для демонстрации
     */
    public static void main(String[] args) {
        String filePath = "input.txt";
        
        try {
            // Метод 1: Подсчет через символы
            int count1 = countPartyIds(filePath);
            System.out.println("Количество partyIds (метод 1): " + count1);
            
            // Метод 2: Подсчет через разделение
            int count2 = countPartyIdsAlternative(filePath);
            System.out.println("Количество partyIds (метод 2): " + count2);
            
            // Проверяем, что оба метода дают одинаковый результат
            if (count1 == count2) {
                System.out.println("✓ Оба метода дают одинаковый результат: " + count1);
            } else {
                System.out.println("✗ Методы дают разные результаты!");
            }
            
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }
}