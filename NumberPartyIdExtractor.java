import java.io.*;
import java.util.*;
import java.util.regex.*;

public class NumberPartyIdExtractor {
    
    /**
     * Метод 1: Поиск чисел без кавычек
     * Для формата: "partyIds":[111,111,222,33333]
     */
    public static List<String> extractNumberPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> partyIds = new ArrayList<>();
        
        // Регулярное выражение для поиска "partyIds":[числа]
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            // Разделяем по запятым и очищаем от пробелов
            String[] ids = idsString.split(",");
            for (String id : ids) {
                partyIds.add(id.trim());
            }
        }
        
        return partyIds;
    }
    
    /**
     * Метод 2: Более строгий поиск только чисел
     * Ищет только цифры между скобками
     */
    public static List<String> extractOnlyNumbers(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> partyIds = new ArrayList<>();
        
        // Регулярное выражение для поиска только цифр
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([0-9,\\s]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            for (String id : ids) {
                String cleanId = id.trim();
                if (cleanId.matches("\\d+")) { // Проверяем, что это только цифры
                    partyIds.add(cleanId);
                }
            }
        }
        
        return partyIds;
    }
    
    /**
     * Метод 3: Поиск с валидацией чисел
     * Проверяет, что каждое значение - это число
     */
    public static List<Integer> extractNumbersAsIntegers(String filePath) throws IOException {
        String content = readFile(filePath);
        List<Integer> partyIds = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            for (String id : ids) {
                try {
                    int number = Integer.parseInt(id.trim());
                    partyIds.add(number);
                } catch (NumberFormatException e) {
                    // Пропускаем нечисловые значения
                }
            }
        }
        
        return partyIds;
    }
    
    /**
     * Метод 4: Универсальный поиск (строки и числа)
     * Обрабатывает как "111" так и 111
     */
    public static List<String> extractUniversalPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> partyIds = new ArrayList<>();
        
        // Регулярное выражение для поиска значений в кавычках или без них
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            for (String id : ids) {
                String cleanId = id.trim();
                // Убираем кавычки если есть, иначе оставляем как есть
                cleanId = cleanId.replaceAll("^\"|\"$", "");
                partyIds.add(cleanId);
            }
        }
        
        return partyIds;
    }
    
    /**
     * Подсчет количества partyIds
     */
    public static int countNumberPartyIds(String filePath) throws IOException {
        List<String> ids = extractNumberPartyIds(filePath);
        return ids.size();
    }
    
    /**
     * Чтение файла
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
        String filePath = "input_numbers.txt";
        
        try {
            System.out.println("=== Метод 1: Поиск чисел без кавычек ===");
            List<String> ids1 = extractNumberPartyIds(filePath);
            System.out.println("Найденные partyIds: " + ids1);
            System.out.println("Количество: " + ids1.size());
            
            System.out.println("\n=== Метод 2: Только цифры ===");
            List<String> ids2 = extractOnlyNumbers(filePath);
            System.out.println("Найденные partyIds: " + ids2);
            System.out.println("Количество: " + ids2.size());
            
            System.out.println("\n=== Метод 3: Как целые числа ===");
            List<Integer> ids3 = extractNumbersAsIntegers(filePath);
            System.out.println("Найденные partyIds: " + ids3);
            System.out.println("Количество: " + ids3.size());
            
            System.out.println("\n=== Метод 4: Универсальный ===");
            List<String> ids4 = extractUniversalPartyIds(filePath);
            System.out.println("Найденные partyIds: " + ids4);
            System.out.println("Количество: " + ids4.size());
            
            System.out.println("\n=== Быстрый подсчет ===");
            int count = countNumberPartyIds(filePath);
            System.out.println("Количество partyIds: " + count);
            
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }
}