import java.io.*;
import java.util.*;
import java.util.regex.*;

public class AllPartyIdsCounter {
    
    /**
     * Находит ВСЕ вхождения partyIds в файле и подсчитывает их
     */
    public static Map<String, Object> findAllPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        Map<String, Object> result = new HashMap<>();
        
        List<String> allPartyIds = new ArrayList<>();
        List<List<String>> allArrays = new ArrayList<>();
        int totalOccurrences = 0;
        
        // Регулярное выражение для поиска ВСЕХ partyIds
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        System.out.println("=== ПОИСК ВСЕХ PARTYIDS ===");
        
        // Ищем ВСЕ вхождения
        while (matcher.find()) {
            totalOccurrences++;
            String idsString = matcher.group(1);
            System.out.println("Найдено вхождение " + totalOccurrences + ": " + idsString);
            
            List<String> currentArray = new ArrayList<>();
            String[] ids = idsString.split(",");
            
            for (String id : ids) {
                String cleanId = id.trim().replaceAll("^\"|\"$", "");
                if (!cleanId.isEmpty()) {
                    currentArray.add(cleanId);
                    allPartyIds.add(cleanId);
                }
            }
            
            allArrays.add(currentArray);
        }
        
        result.put("totalOccurrences", totalOccurrences);
        result.put("totalIdCount", allPartyIds.size());
        result.put("allIds", allPartyIds);
        result.put("allArrays", allArrays);
        
        return result;
    }
    
    /**
     * Быстрый подсчет всех partyIds
     */
    public static int countAllPartyIds(String filePath) throws IOException {
        String content = readFile(filePath);
        int totalCount = 0;
        
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String idsString = matcher.group(1);
            String[] ids = idsString.split(",");
            totalCount += ids.length;
        }
        
        return totalCount;
    }
    
    /**
     * Подсчитывает количество вхождений partyIds
     */
    public static int countPartyIdOccurrences(String filePath) throws IOException {
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
     * Выводит подробную статистику
     */
    public static void printDetailedStatistics(String filePath) throws IOException {
        Map<String, Object> result = findAllPartyIds(filePath);
        
        int totalOccurrences = (Integer) result.get("totalOccurrences");
        int totalIdCount = (Integer) result.get("totalIdCount");
        @SuppressWarnings("unchecked")
        List<String> allIds = (List<String>) result.get("allIds");
        @SuppressWarnings("unchecked")
        List<List<String>> allArrays = (List<List<String>>) result.get("allArrays");
        
        System.out.println("\n=== РЕЗУЛЬТАТЫ ===");
        System.out.println("Количество вхождений partyIds: " + totalOccurrences);
        System.out.println("Общее количество partyIds: " + totalIdCount);
        
        if (totalOccurrences > 0) {
            System.out.println("\n=== ДЕТАЛИ ПО ВХОЖДЕНИЯМ ===");
            for (int i = 0; i < allArrays.size(); i++) {
                List<String> array = allArrays.get(i);
                System.out.println("Вхождение " + (i + 1) + ": " + array + " (количество: " + array.size() + ")");
            }
        }
        
        if (!allIds.isEmpty()) {
            System.out.println("\n=== ВСЕ НАЙДЕННЫЕ ID ===");
            System.out.println("Все partyIds: " + allIds);
            
            // Уникальные ID
            Set<String> uniqueIds = new HashSet<>(allIds);
            System.out.println("Уникальных partyIds: " + uniqueIds.size());
            System.out.println("Уникальные ID: " + uniqueIds);
            
            // Дубликаты
            Map<String, Integer> duplicates = new HashMap<>();
            for (String id : allIds) {
                duplicates.put(id, duplicates.getOrDefault(id, 0) + 1);
            }
            
            System.out.println("\n=== АНАЛИЗ ДУБЛИКАТОВ ===");
            boolean hasDuplicates = false;
            for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
                if (entry.getValue() > 1) {
                    if (!hasDuplicates) {
                        hasDuplicates = true;
                    }
                    System.out.println("ID '" + entry.getKey() + "' встречается " + entry.getValue() + " раз");
                }
            }
            if (!hasDuplicates) {
                System.out.println("Дубликатов не найдено");
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
        String filePath = "input_repeated.txt";
        
        try {
            // Быстрый подсчет
            int totalCount = countAllPartyIds(filePath);
            int occurrences = countPartyIdOccurrences(filePath);
            
            System.out.println("=== БЫСТРЫЙ РЕЗУЛЬТАТ ===");
            System.out.println("Вхождений partyIds: " + occurrences);
            System.out.println("Всего partyIds: " + totalCount);
            
            // Подробная статистика
            printDetailedStatistics(filePath);
            
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}