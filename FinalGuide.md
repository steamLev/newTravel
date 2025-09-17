# Поиск ВСЕХ partyIds в файле

## Проблема
Нужно найти и посчитать ВСЕ вхождения `partyIds` в файле, а не только первое.

## Решение

### Ключевое изменение: использование `while` вместо `if`

```java
// ❌ НЕПРАВИЛЬНО - находит только первое вхождение
if (matcher.find()) {
    // обработка
}

// ✅ ПРАВИЛЬНО - находит ВСЕ вхождения
while (matcher.find()) {
    // обработка каждого найденного массива
}
```

### Основной код:

```java
Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]*)\\]");
Matcher matcher = pattern.matcher(content);

List<String> allPartyIds = new ArrayList<>();
int arrayCount = 0;

while (matcher.find()) {
    arrayCount++;
    String idsString = matcher.group(1).trim();
    
    if (!idsString.isEmpty()) {
        String[] ids = idsString.split(",");
        for (String id : ids) {
            String cleanId = id.trim().replaceAll("^\"|\"$", "");
            if (!cleanId.isEmpty()) {
                allPartyIds.add(cleanId);
            }
        }
    }
}
```

## Готовые классы:

### 1. `SimpleMultipleExample.java` - простой вариант
- Находит все partyIds
- Показывает количество массивов и общее количество ID
- Выводит все найденные ID

### 2. `FinalPartyIdExtractor.java` - полный функционал
- Подробная статистика по каждому массиву
- Поиск дубликатов
- Обработка пустых массивов
- Уникальные ID

## Результат тестирования:

**Входной файл содержит:**
```json
{
  "users": [
    {"partyIds": [111, 222, 333]},
    {"partyIds": [444, 555]}
  ],
  "groups": {
    "admin": {"partyIds": [999, 888, 777, 666]},
    "user": {"partyIds": [123, 456]}
  }
}
```

**Результат:**
- Количество массивов partyIds: **4**
- Общее количество partyIds: **11**
- Все найденные ID: `[111, 222, 333, 444, 555, 999, 888, 777, 666, 123, 456]`

## Использование:

```bash
# Простой вариант
javac SimpleMultipleExample.java
java SimpleMultipleExample

# Полный функционал
javac FinalPartyIdExtractor.java
java FinalPartyIdExtractor
```

## Regex для поиска всех partyIds:

```regex
"partyIds"\s*:\s*\[([^\]]*)\]
```

- `([^\]]*)` - захватывает содержимое между `[]` (включая пустые массивы)
- `while (matcher.find())` - находит ВСЕ вхождения