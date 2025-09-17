# Поиск ВСЕХ повторяющихся partyIds

## Проблема
В файле может быть несколько одинаковых массивов `"partyIds":[111,111,222,33333]` и нужно пересчитать ВСЕ partyIds во всех вхождениях.

## Решение

### Ключевой код:
```java
Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
Matcher matcher = pattern.matcher(content);

List<String> allPartyIds = new ArrayList<>();
int occurrenceCount = 0;

// Ищем ВСЕ вхождения
while (matcher.find()) {
    occurrenceCount++;
    String idsString = matcher.group(1);
    
    String[] ids = idsString.split(",");
    for (String id : ids) {
        String cleanId = id.trim().replaceAll("^\"|\"$", "");
        if (!cleanId.isEmpty()) {
            allPartyIds.add(cleanId); // Добавляем ВСЕ ID
        }
    }
}
```

## Результат тестирования

**Входной файл:**
```json
{
  "data1": {"partyIds": [111, 111, 222, 33333]},
  "data2": {"partyIds": [111, 111, 222, 33333]},
  "data3": {"partyIds": [111, 111, 222, 33333]},
  "other": {"partyIds": [444, 555, 666]},
  "more": {"partyIds": [111, 111, 222, 33333]}
}
```

**Результат:**
- **Вхождений partyIds: 5**
- **Общее количество partyIds: 19**
- **Все ID:** `[111, 111, 222, 33333, 111, 111, 222, 33333, 111, 111, 222, 33333, 444, 555, 666, 111, 111, 222, 33333]`

**Анализ дубликатов:**
- ID '111' встречается **8 раз**
- ID '222' встречается **4 раза**  
- ID '33333' встречается **4 раза**
- ID '444' встречается **1 раз**
- ID '555' встречается **1 раз**
- ID '666' встречается **1 раз**

## Готовые классы

### 1. `SimpleRepeatedExample.java` - простой вариант
```bash
javac SimpleRepeatedExample.java
java SimpleRepeatedExample
```

### 2. `AllPartyIdsCounter.java` - полный функционал
```bash
javac AllPartyIdsCounter.java
java AllPartyIdsCounter
```

## Ключевые особенности

✅ **Находит ВСЕ вхождения** `"partyIds":[...]` в файле
✅ **Пересчитывает ВСЕ partyIds** во всех массивах
✅ **Показывает детали** по каждому вхождению
✅ **Анализирует дубликаты** - сколько раз каждый ID встречается
✅ **Подсчитывает уникальные** ID
✅ **Обрабатывает разные массивы** в одном файле

## Regex
```regex
"partyIds"\s*:\s*\[([^\]]+)\]
```

- `([^\]]+)` - захватывает содержимое между `[]`
- `while (matcher.find())` - находит ВСЕ вхождения
- Каждый найденный ID добавляется в общий список