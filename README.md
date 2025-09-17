# Извлечение partyIds из текстового файла

Этот проект содержит Java код для извлечения массива `partyIds` из текстового файла.

## Основные методы

### 1. Простой метод с регулярными выражениями
```java
Pattern pattern = Pattern.compile("\"partyIds\"\\s*:\\s*\\[([^\\]]+)\\]");
Matcher matcher = pattern.matcher(content);
if (matcher.find()) {
    String idsString = matcher.group(1);
    // Обработка найденных ID
}
```

### 2. Поиск по строкам
```java
if (line.contains("\"partyIds\"")) {
    int startIndex = line.indexOf("[");
    int endIndex = line.lastIndexOf("]");
    // Извлечение содержимого между скобками
}
```

### 3. Продвинутый парсинг
Обрабатывает многострочные массивы и корректно находит соответствующие скобки.

## Использование

### Простой пример с подсчетом:
1. Скомпилируйте код:
```bash
javac SimpleExample.java
```

2. Запустите:
```bash
java SimpleExample
```

### Только подсчет количества:
```bash
javac PartyIdCounter.java
java PartyIdCounter
```

### Полный функционал:
```bash
javac PartyIdExtractor.java
java PartyIdExtractor
```

## Подсчет partyIds

Код предоставляет несколько способов подсчета:

1. **Через размер списка**: `partyIds.size()`
2. **Через подсчет запятых**: количество запятых + 1
3. **Через разделение строки**: `idsString.split(",").length`

### Примеры методов подсчета:

```java
// Быстрый подсчет
int count = countPartyIds(filePath);

// Подсчет с извлечением списка
List<String> ids = extractPartyIdsWithRegex(filePath);
int count = ids.size();

// Статистика
printPartyIdsStatistics(filePath);
```

## Формат входного файла

Код ищет структуру вида:
```json
{
  "partyIds": ["id1", "id2", "id3"]
}
```

## Регулярное выражение

`"partyIds"\\s*:\\s*\\[([^\\]]+)\\]`

- `"partyIds"` - ищет точную строку "partyIds"
- `\\s*` - ноль или более пробельных символов
- `:` - двоеточие
- `\\s*` - снова пробелы
- `\\[` - открывающая квадратная скобка
- `([^\\]]+)` - захватывающая группа с содержимым (все символы кроме `]`)
- `\\]` - закрывающая квадратная скобка