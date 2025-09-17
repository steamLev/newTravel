import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MultiplePartyIdExtractor {
    
    /**
     * Находит ВСЕ partyIds в файле и возвращает список всех найденных ID
     */
    public static List<String> extractAllPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> allPartyIds = new ArrayList<>();
        
        // Регулярное выражение для поиска ВСЕХ partyIds
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        // Ищем все вхождения
        while (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            
            for (String id : ids) {
                String cleanId = id.trim().replaceAll("^\"|\"$", "");
                if (!cleanId.isEmpty()) {
                    allPartyIds.add(cleanId);
                }
            }
        }
        
        return allPartyIds;
    }
    
    /**
     * Находит все partyIds и группирует их по массивам
     */
    public static List<List<String>> extractAllPartyIdArrays(String filePath) throws IOException {
        String content = readFile(filePath);
        List<List<String>> allArrays = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            List<String> currentArray = new ArrayList<>();
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            
            for (String id : ids) {
                String cleanId = id.trim().replaceAll("^\"|\"$", "");
                if (!cleanId.isEmpty()) {
                    currentArray.add(cleanId);
                }
            }
            
            if (!currentArray.isEmpty()) {
                allArrays.add(currentArray);
            }
        }
        
        return allArrays;
    }
    
    /**
     * Подсчитывает общее количество partyIds во всех массивах
     */
    public static int countAllPartyIds(String filePath) throws IOException {
        List<String> allIds = extractAllPartyIds(filePath);
        return allIds.size();
    }
    
    /**
     * Подсчитывает количество массивов partyIds
     */
    public static int countPartyIdArrays(String filePath) throws IOException {
        String content = readFile(filePath);
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    /**
     * Получает подробную статистику по всем partyIds
     */
    public static void printDetailedStatistics(String filePath) throws IOException {
        List<List<String>> allArrays = extractAllPartyIdArrays(filePath);
        List<String> allIds = extractAllPartyIds(filePath);
        
        System.out.println("=== ПОДРОБНАЯ СТАТИСТИКА ===");
        System.out.println("Количество массивов partyIds: " + allArrays.size());
        System.out.println("Общее количество partyIds: " + allIds.size());
        
        if (!allArrays.isEmpty()) {
            System.out.println("\n=== ДЕТАЛИ ПО МАССИВАМ ===");
            for (int i = 0; i < allArrays.size(); i++) {
                List<String> array = allArrays.get(i);
                System.out.println("Массив " + (i + 1) + ": " + array + " (количество: " + array.size() + ")");
            }
        }
        
        if (!allIds.isEmpty()) {
            System.out.println("\n=== ВСЕ НАЙДЕННЫЕ ID ===");
            System.out.println("Все partyIds: " + allIds);
            
            // Находим уникальные ID
            Set<String> uniqueIds = new HashSet<>(allIds);
            System.out.println("Уникальных partyIds: " + uniqueIds.size());
            System.out.println("Уникальные ID: " + uniqueIds);
            
            // Находим дубликаты
            Map<String, Integer> duplicates = new HashMap<>();
            for (String id : allIds) {
                duplicates.put(id, duplicates.getOrDefault(id, 0) + 1);
            }
            
            System.out.println("\n=== ДУБЛИКАТЫ ===");
            boolean hasDuplicates = false;
            for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
                if (entry.getValue() > 1) {
                    System.out.println("ID '" + entry.getKey() + "' встречается " + entry.getValue() + " раз");
                    hasDuplicates = true;
                }
            }
            if (!hasDuplicates) {
                System.out.println("Дубликатов не найдено");
            }
        }
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
        String filePath = "input_multiple.txt";
        
        try {
            System.out.println("=== ПОИСК ВСЕХ PARTYIDS ===");
            
            // Простой подсчет
            int totalCount = countAllPartyIds(filePath);
            int arrayCount = countPartyIdArrays(filePath);
            
            System.out.println("Количество массивов partyIds: " + arrayCount);
            System.out.println("Общее количество partyIds: " + totalCount);
            
            // Подробная статистика
            printDetailedStatistics(filePath);
            
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }
}