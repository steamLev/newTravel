import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Финальная версия для поиска ВСЕХ partyIds в файле
 */
public class FinalPartyIdExtractor {
    
    /**
     * Находит ВСЕ partyIds в файле
     * @param filePath путь к файлу
     * @return Map с результатами: количество массивов, общее количество ID, все ID
     */
    public static Map<String, Object> extractAllPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        Map<String, Object> result = new HashMap<>();
        
        List<String> allPartyIds = new ArrayList<>();
        List<List<String>> allArrays = new ArrayList<>();
        
        // Регулярное выражение для поиска ВСЕХ partyIds (включая пустые массивы)
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(content);
        
        int arrayCount = 0;
        while (matcher.find()) {
            arrayCount++;
            String idsString = matcher.group(1).trim();
            List<String> currentArray = new ArrayList<>();
            
            if (!idsString.isEmpty()) {
                String[] ids = idsString.split(",");
                for (String id : ids) {
                    String cleanId = id.trim().replaceAll("^\"|\"$", "");
                    if (!cleanId.isEmpty()) {
                        currentArray.add(cleanId);
                        allPartyIds.add(cleanId);
                    }
                }
            }
            
            allArrays.add(currentArray);
        }
        
        result.put("arrayCount", arrayCount);
        result.put("totalIdCount", allPartyIds.size());
        result.put("allIds", allPartyIds);
        result.put("allArrays", allArrays);
        
        return result;
    }
    
    /**
     * Быстрый подсчет всех partyIds
     */
    public static int countAllPartyIds(String filePath) throws IOException {
        Map<String, Object> result = extractAllPartyIds(filePath);
        return (Integer) result.get("totalIdCount");
    }
    
    /**
     * Подсчет количества массивов partyIds
     */
    public static int countPartyIdArrays(String filePath) throws IOException {
        Map<String, Object> result = extractAllPartyIds(filePath);
        return (Integer) result.get("arrayCount");
    }
    
    /**
     * Выводит полную статистику
     */
    public static void printFullStatistics(String filePath) throws IOException {
        Map<String, Object> result = extractAllPartyIds(filePath);
        
        int arrayCount = (Integer) result.get("arrayCount");
        int totalIdCount = (Integer) result.get("totalIdCount");
        @SuppressWarnings("unchecked")
        List<String> allIds = (List<String>) result.get("allIds");
        @SuppressWarnings("unchecked")
        List<List<String>> allArrays = (List<List<String>>) result.get("allArrays");
        
        System.out.println("=== ПОЛНАЯ СТАТИСТИКА PARTYIDS ===");
        System.out.println("Количество массивов partyIds: " + arrayCount);
        System.out.println("Общее количество partyIds: " + totalIdCount);
        
        if (arrayCount > 0) {
            System.out.println("\n=== ДЕТАЛИ ПО МАССИВАМ ===");
            for (int i = 0; i < allArrays.size(); i++) {
                List<String> array = allArrays.get(i);
                if (array.isEmpty()) {
                    System.out.println("Массив " + (i + 1) + ": [] (пустой)");
                } else {
                    System.out.println("Массив " + (i + 1) + ": " + array + " (количество: " + array.size() + ")");
                }
            }
        }
        
        if (!allIds.isEmpty()) {
            System.out.println("\n=== ВСЕ НАЙДЕННЫЕ ID ===");
            System.out.println("Все partyIds: " + allIds);
            
            // Уникальные ID
            Set<String> uniqueIds = new HashSet<>(allIds);
            System.out.println("Уникальных partyIds: " + uniqueIds.size());
            
            // Дубликаты
            Map<String, Integer> duplicates = new HashMap<>();
            for (String id : allIds) {
                duplicates.put(id, duplicates.getOrDefault(id, 0) + 1);
            }
            
            boolean hasDuplicates = false;
            for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
                if (entry.getValue() > 1) {
                    if (!hasDuplicates) {
                        System.out.println("\n=== ДУБЛИКАТЫ ===");
                        hasDuplicates = true;
                    }
                    System.out.println("ID '" + entry.getKey() + "' встречается " + entry.getValue() + " раз");
                }
            }
            if (!hasDuplicates) {
                System.out.println("\nДубликатов не найдено");
            }
        } else {
            System.out.println("\nPartyIds не найдены в файле");
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
     * Главный метод
     */
    public static void main(String[] args) {
        String filePath = "input_multiple.txt";
        
        try {
            // Быстрый результат
            int totalCount = countAllPartyIds(filePath);
            int arrayCount = countPartyIdArrays(filePath);
            
            System.out.println("=== БЫСТРЫЙ РЕЗУЛЬТАТ ===");
            System.out.println("Массивов partyIds: " + arrayCount);
            System.out.println("Всего partyIds: " + totalCount);
            
            // Подробная статистика
            printFullStatistics(filePath);
            
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}