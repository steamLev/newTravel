import java.io.*;
import java.util.*;
import java.util.regex.*;

public class UniversalPartyIdFinder {
    
    public static void main(String[] args) {
        String filePath = "input_repeated.txt";
        
        try {
            // Читаем файл
            String content = readFile(filePath);
            System.out.println("=== УНИВЕРСАЛЬНЫЙ ПОИСК PARTYIDS ===");
            System.out.println("Размер файла: " + content.length() + " символов");
            
            // Метод 1: Простой поиск
            System.out.println("\n--- МЕТОД 1: Простой поиск ---");
            findWithSimpleMethod(content);
            
            // Метод 2: Строгий поиск
            System.out.println("\n--- МЕТОД 2: Строгий поиск ---");
            findWithStrictMethod(content);
            
            // Метод 3: Построчный поиск
            System.out.println("\n--- МЕТОД 3: Построчный поиск ---");
            findWithLineByLineMethod(filePath);
            
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
    
    /**
     * Метод 1: Простой поиск с while
     */
    private static void findWithSimpleMethod(String content) {
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        int count = 0;
        List<String> allIds = new ArrayList<>();
        
        while (matcher.find()) {
            count++;
            String idsString = matcher.group(1);
            System.out.println("Найдено " + count + ": " + idsString);
            
            String[] ids = idsString.split(",");
            for (String id : ids) {
                allIds.add(id.trim());
            }
        }
        
        System.out.println("Итого найдено вхождений: " + count);
        System.out.println("Всего partyIds: " + allIds.size());
    }
    
    /**
     * Метод 2: Строгий поиск с детальной отладкой
     */
    private static void findWithStrictMethod(String content) {
        // Более строгий regex
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        
        int count = 0;
        int lastEnd = 0;
        
        while (matcher.find()) {
            count++;
            int start = matcher.start();
            int end = matcher.end();
            
            System.out.println("Вхождение " + count + ":");
            System.out.println("  Позиция: " + start + "-" + end);
            System.out.println("  Полный текст: '" + matcher.group(0) + "'");
            System.out.println("  Содержимое: '" + matcher.group(1) + "'");
            System.out.println("  Расстояние от предыдущего: " + (start - lastEnd));
            
            lastEnd = end;
        }
        
        System.out.println("Итого найдено: " + count);
    }
    
    /**
     * Метод 3: Построчный поиск
     */
    private static void findWithLineByLineMethod(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            int totalCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.contains("\"partyIds\"")) {
                    System.out.println("Строка " + lineNumber + " содержит partyIds: " + line.trim());
                    
                    // Ищем в этой строке
                    Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
                    Matcher matcher = pattern.matcher(line);
                    
                    while (matcher.find()) {
                        totalCount++;
                        System.out.println("  Найдено в строке " + lineNumber + ": " + matcher.group(1));
                    }
                }
            }
            
            System.out.println("Итого найдено вхождений: " + totalCount);
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