import java.io.*;
import java.util.*;
import java.util.regex.*;

public class SimpleMultipleExample {
    
    public static void main(String[] args) {
        String filePath = "input_multiple.txt";
        
        try {
            // Читаем файл
            String content = readFile(filePath);
            
            // Ищем ВСЕ partyIds
            Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = pattern.matcher(content);
            
            List<String> allPartyIds = new ArrayList<>();
            int arrayCount = 0;
            
            System.out.println("=== ПОИСК ВСЕХ PARTYIDS ===");
            
            // Ищем все вхождения
            while (matcher.find()) {
                arrayCount++;
                String idsString = matcher.group(1);
                System.out.println("Найден массив " + arrayCount + ": " + idsString);
                
                // Разделяем по запятым
                String[] ids = idsString.split(",");
                for (String id : ids) {
                    String cleanId = id.trim().replaceAll("^\"|\"$", "");
                    if (!cleanId.isEmpty()) {
                        allPartyIds.add(cleanId);
                    }
                }
            }
            
            System.out.println("\n=== РЕЗУЛЬТАТЫ ===");
            System.out.println("Количество массивов partyIds: " + arrayCount);
            System.out.println("Общее количество partyIds: " + allPartyIds.size());
            System.out.println("Все найденные partyIds: " + allPartyIds);
            
            // Подсчет уникальных
            Set<String> uniqueIds = new HashSet<>(allPartyIds);
            System.out.println("Уникальных partyIds: " + uniqueIds.size());
            
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