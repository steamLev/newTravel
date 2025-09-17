import java.io.*;
import java.util.*;
import java.util.regex.*;

public class PartyIdExtractor {
    
    /**
     * Метод 1: Использование регулярных выражений
     * Извлекает partyIds из JSON-подобного текста
     */
    public static List<String> extractPartyIdsWithRegex(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> partyIds = new ArrayList<>();
        
        // Регулярное выражение для поиска "partyIds":[xxxx,xxxx,xxxx]
        Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String idsString = matcher.group(1);
            // Разделяем по запятым и очищаем от пробелов и кавычек
            String[] ids = idsString.split(",");
            for (String id : ids) {
                partyIds.add(id.trim().replaceAll("\"", ""));
            }
        }
        
        return partyIds;
    }
    
    /**
     * Метод 2: Поиск по строкам (более простой подход)
     * Ищет строку содержащую partyIds и извлекает значения
     */
    public static List<String> extractPartyIdsByLine(String filePath) throws IOException {
        List<String> partyIds = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("\"partyIds\"")) {
                    // Находим начало массива
                    int startIndex = line.indexOf("[");
                    int endIndex = line.lastIndexOf("]");
                    
                    if (startIndex != -1 && endIndex != -1) {
                        String arrayContent = line.substring(startIndex + 1, endIndex);
                        String[] ids = arrayContent.split(",");
                        for (String id : ids) {
                            partyIds.add(id.trim().replaceAll("\"", ""));
                        }
                        break; // Найдено, выходим из цикла
                    }
                }
            }
        }
        
        return partyIds;
    }
    
    /**
     * Метод 3: Более продвинутый парсинг с обработкой многострочных массивов
     */
    public static List<String> extractPartyIdsAdvanced(String filePath) throws IOException {
        String content = readFile(filePath);
        List<String> partyIds = new ArrayList<>();
        
        // Ищем начало partyIds
        int partyIdsIndex = content.indexOf("\"partyIds\"");
        if (partyIdsIndex == -1) {
            return partyIds;
        }
        
        // Ищем открывающую скобку
        int openBracketIndex = content.indexOf("[", partyIdsIndex);
        if (openBracketIndex == -1) {
            return partyIds;
        }
        
        // Ищем закрывающую скобку
        int closeBracketIndex = findMatchingBracket(content, openBracketIndex);
        if (closeBracketIndex == -1) {
            return partyIds;
        }
        
        // Извлекаем содержимое между скобками
        String arrayContent = content.substring(openBracketIndex + 1, closeBracketIndex);
        
        // Разбираем значения
        String[] parts = arrayContent.split(",");
        for (String part : parts) {
            String cleanId = part.trim().replaceAll("^\"|\"$", ""); // Убираем кавычки
            if (!cleanId.isEmpty()) {
                partyIds.add(cleanId);
            }
        }
        
        return partyIds;
    }
    
    /**
     * Вспомогательный метод для поиска соответствующей закрывающей скобки
     */
    private static int findMatchingBracket(String content, int openIndex) {
        int bracketCount = 0;
        for (int i = openIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') {
                bracketCount++;
            } else if (c == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * Чтение файла в строку
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
     * Метод для быстрого подсчета количества partyIds
     */
    public static int countPartyIds(String filePath) throws IOException {
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
     * Метод для получения статистики по partyIds
     */
    public static void printPartyIdsStatistics(String filePath) throws IOException {
        List<String> ids = extractPartyIdsWithRegex(filePath);
        
        System.out.println("=== Статистика partyIds ===");
        System.out.println("Количество partyIds: " + ids.size());
        System.out.println("Список partyIds: " + ids);
        
        if (!ids.isEmpty()) {
            System.out.println("Первый partyId: " + ids.get(0));
            System.out.println("Последний partyId: " + ids.get(ids.size() - 1));
        }
    }
    
    /**
     * Главный метод для демонстрации
     */
    public static void main(String[] args) {
        String filePath = "input.txt"; // Путь к вашему файлу
        
        try {
            System.out.println("=== Метод 1: Регулярные выражения ===");
            List<String> ids1 = extractPartyIdsWithRegex(filePath);
            System.out.println("Найденные partyIds: " + ids1);
            System.out.println("Количество partyIds: " + ids1.size());
            
            System.out.println("\n=== Метод 2: Поиск по строкам ===");
            List<String> ids2 = extractPartyIdsByLine(filePath);
            System.out.println("Найденные partyIds: " + ids2);
            System.out.println("Количество partyIds: " + ids2.size());
            
            System.out.println("\n=== Метод 3: Продвинутый парсинг ===");
            List<String> ids3 = extractPartyIdsAdvanced(filePath);
            System.out.println("Найденные partyIds: " + ids3);
            System.out.println("Количество partyIds: " + ids3.size());
            
            System.out.println("\n=== Быстрый подсчет ===");
            int count = countPartyIds(filePath);
            System.out.println("Количество partyIds (быстрый метод): " + count);
            
            System.out.println("\n=== Подробная статистика ===");
            printPartyIdsStatistics(filePath);
            
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }
}