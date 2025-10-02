# План создания ИИ-агента для прогнозирования футбольных матчей Реал Мадрид

## 📋 Обзор проекта

Создание интеллектуального агента на базе машинного обучения для прогнозирования результатов футбольных матчей команды Реал Мадрид.

---

## 🎯 Этап 1: Сбор и подготовка данных

### 1.1 Источники данных
- **API футбольных данных:**
  - Football-Data.org API
  - API-Football (RapidAPI)
  - Sportradar API
  - Transfermarkt (веб-скрейпинг)
  
- **Исторические данные:**
  - Результаты матчей (минимум 5-10 сезонов)
  - Статистика игроков
  - Составы команд
  - Домашние/выездные игры
  - Турнирная таблица на момент матча

### 1.2 Параметры для сбора

**Данные о команде:**
- Форма команды (последние 5-10 матчей)
- Голы забитые/пропущенные
- Владение мячом (%)
- Удары по воротам/в створ
- Процент передач
- Угловые, фолы, карточки
- Позиция в турнирной таблице

**Данные об игроках:**
- Наличие ключевых игроков
- Травмы и дисквалификации
- Статистика результативности
- Усталость игроков (количество матчей подряд)

**Контекстные данные:**
- Домашний/выездной матч
- Турнир (Ла Лига, Лига Чемпионов, Кубок и т.д.)
- Погодные условия
- Соперник и его статистика
- История встреч (H2H)
- Время суток матча

### 1.3 Структура базы данных

```sql
-- Таблица матчей
CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP,
    home_team VARCHAR(100),
    away_team VARCHAR(100),
    home_score INTEGER,
    away_score INTEGER,
    competition VARCHAR(100),
    venue VARCHAR(200)
);

-- Таблица статистики матчей
CREATE TABLE match_statistics (
    id SERIAL PRIMARY KEY,
    match_id INTEGER REFERENCES matches(id),
    team VARCHAR(100),
    possession DECIMAL(5,2),
    shots INTEGER,
    shots_on_target INTEGER,
    corners INTEGER,
    fouls INTEGER,
    yellow_cards INTEGER,
    red_cards INTEGER
);

-- Таблица игроков
CREATE TABLE players (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200),
    position VARCHAR(50),
    team VARCHAR(100)
);

-- Таблица форм команд
CREATE TABLE team_form (
    id SERIAL PRIMARY KEY,
    team VARCHAR(100),
    date TIMESTAMP,
    points_last_5 INTEGER,
    goals_scored_last_5 INTEGER,
    goals_conceded_last_5 INTEGER
);
```

---

## 🤖 Этап 2: Разработка модели машинного обучения

### 2.1 Выбор подхода

**Вариант А: Классификация (рекомендуется для начала)**
- Классы: Победа / Ничья / Поражение
- Алгоритмы:
  - Random Forest
  - Gradient Boosting (XGBoost, LightGBM)
  - Neural Networks

**Вариант Б: Регрессия**
- Предсказание точного счета
- Предсказание количества голов каждой команды

**Вариант В: Вероятностное предсказание**
- Вероятность каждого исхода (победа/ничья/поражение)
- Модель: Logistic Regression, Neural Networks

### 2.2 Feature Engineering (создание признаков)

```python
# Примеры признаков
features = [
    # Форма команды
    'points_last_5_matches',
    'goals_scored_last_5',
    'goals_conceded_last_5',
    'win_rate_last_10',
    
    # Домашний фактор
    'is_home_match',
    'home_win_rate',
    'away_win_rate',
    
    # Против соперника
    'h2h_wins',
    'h2h_draws',
    'h2h_losses',
    'avg_goals_vs_opponent',
    
    # Турнирная ситуация
    'league_position',
    'points_difference_from_leader',
    'competition_type',
    
    # Состав
    'key_players_available',
    'injured_players_count',
    
    # Соперник
    'opponent_league_position',
    'opponent_form',
    'opponent_goals_conceded_avg'
]
```

### 2.3 Технологический стек

**Python библиотеки:**
```python
# Обработка данных
pandas
numpy
scipy

# Машинное обучение
scikit-learn
xgboost
lightgbm
tensorflow / pytorch  # для deep learning

# Работа с API
requests
beautifulsoup4  # для скрейпинга

# Визуализация
matplotlib
seaborn
plotly
```

---

## 🏗️ Этап 3: Архитектура системы

### 3.1 Компоненты системы

```
┌─────────────────────────────────────────────────────┐
│                  Frontend (Web UI)                   │
│          - Отображение прогнозов                     │
│          - История предсказаний                      │
│          - Статистика точности                       │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              API Layer (REST/GraphQL)                │
│          - Эндпоинты для прогнозов                   │
│          - Аутентификация                            │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│           AI Agent Service (Python)                  │
│          - Загрузка модели ML                        │
│          - Генерация признаков                       │
│          - Предсказания                              │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│         Data Collection Service                      │
│          - Сбор данных из API                        │
│          - Обновление базы данных                    │
│          - Кэширование                               │
└────────────────────┬────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────┐
│              Database (PostgreSQL)                   │
│          - Исторические матчи                        │
│          - Статистика команд                         │
│          - Прогнозы и их результаты                  │
└─────────────────────────────────────────────────────┘
```

### 3.2 Интеграция с текущим проектом

Используя существующую инфраструктуру RabbitMQ:

```java
// Добавить новый сервис для AI predictions
@Service
public class FootballPredictionService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public PredictionResult predictMatch(MatchRequest request) {
        // Отправить запрос в Python AI сервис через RabbitMQ
        rabbitTemplate.convertAndSend(
            "ai.predictions.queue", 
            request
        );
        
        // Получить результат
        return waitForPrediction(request.getMatchId());
    }
}
```

---

## 🔄 Этап 4: Разработка AI Agent

### 4.1 Структура проекта

```
real-madrid-ai-agent/
├── src/
│   ├── data/
│   │   ├── collectors/
│   │   │   ├── football_api_collector.py
│   │   │   └── web_scraper.py
│   │   ├── preprocessors/
│   │   │   ├── feature_engineering.py
│   │   │   └── data_cleaner.py
│   │   └── database/
│   │       └── db_manager.py
│   ├── models/
│   │   ├── classifiers/
│   │   │   ├── random_forest_model.py
│   │   │   ├── xgboost_model.py
│   │   │   └── neural_network_model.py
│   │   ├── ensemble.py
│   │   └── model_trainer.py
│   ├── prediction/
│   │   ├── predictor.py
│   │   └── post_processor.py
│   ├── evaluation/
│   │   ├── metrics.py
│   │   └── backtesting.py
│   └── api/
│       ├── app.py  # Flask/FastAPI
│       └── rabbitmq_consumer.py
├── notebooks/
│   ├── 01_data_exploration.ipynb
│   ├── 02_feature_engineering.ipynb
│   ├── 03_model_training.ipynb
│   └── 04_evaluation.ipynb
├── tests/
├── models/  # сохраненные модели
├── config/
│   └── config.yaml
├── requirements.txt
└── README.md
```

### 4.2 Пример кода предсказания

```python
# src/prediction/predictor.py
import joblib
import pandas as pd
from typing import Dict, List

class RealMadridPredictor:
    def __init__(self, model_path: str):
        self.model = joblib.load(model_path)
        self.feature_names = self.model.feature_names_in_
    
    def predict_match(self, match_data: Dict) -> Dict:
        """
        Предсказать результат матча
        
        Args:
            match_data: Словарь с данными о матче
            
        Returns:
            Словарь с предсказанием и вероятностями
        """
        # Подготовка признаков
        features = self._prepare_features(match_data)
        
        # Предсказание
        prediction = self.model.predict(features)[0]
        probabilities = self.model.predict_proba(features)[0]
        
        return {
            'prediction': prediction,  # 0: loss, 1: draw, 2: win
            'probability': {
                'loss': probabilities[0],
                'draw': probabilities[1],
                'win': probabilities[2]
            },
            'confidence': max(probabilities),
            'recommendation': self._get_recommendation(probabilities)
        }
    
    def _prepare_features(self, match_data: Dict) -> pd.DataFrame:
        """Создание признаков из сырых данных"""
        # Извлечение и обработка признаков
        features = {}
        
        # Форма команды
        features['points_last_5'] = self._calculate_recent_form(
            match_data['recent_matches']
        )
        
        # Домашний фактор
        features['is_home'] = 1 if match_data['venue'] == 'home' else 0
        
        # История встреч
        h2h = match_data.get('head_to_head', {})
        features['h2h_win_rate'] = h2h.get('wins', 0) / max(h2h.get('total', 1), 1)
        
        # ... другие признаки
        
        return pd.DataFrame([features])[self.feature_names]
    
    def _get_recommendation(self, probabilities: List[float]) -> str:
        """Рекомендация для ставки на основе вероятностей"""
        loss, draw, win = probabilities
        
        if win > 0.65:
            return "Strong Win Prediction"
        elif win > 0.50:
            return "Moderate Win Prediction"
        elif draw > 0.35:
            return "Draw Possible"
        else:
            return "Uncertain Outcome"
```

---

## 📊 Этап 5: Обучение и оценка модели

### 5.1 Процесс обучения

```python
# src/models/model_trainer.py
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.ensemble import RandomForestClassifier
from xgboost import XGBClassifier
import numpy as np

class ModelTrainer:
    def train_model(self, X, y):
        # Разделение данных
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42
        )
        
        # Обучение нескольких моделей
        models = {
            'random_forest': RandomForestClassifier(
                n_estimators=200,
                max_depth=10,
                min_samples_split=5,
                random_state=42
            ),
            'xgboost': XGBClassifier(
                n_estimators=200,
                max_depth=8,
                learning_rate=0.1,
                random_state=42
            )
        }
        
        results = {}
        for name, model in models.items():
            # Обучение
            model.fit(X_train, y_train)
            
            # Кросс-валидация
            cv_scores = cross_val_score(
                model, X_train, y_train, cv=5
            )
            
            # Оценка на тестовой выборке
            test_score = model.score(X_test, y_test)
            
            results[name] = {
                'model': model,
                'cv_score': np.mean(cv_scores),
                'test_score': test_score
            }
            
            print(f"{name}:")
            print(f"  CV Score: {np.mean(cv_scores):.3f}")
            print(f"  Test Score: {test_score:.3f}")
        
        # Выбор лучшей модели
        best_model_name = max(
            results, 
            key=lambda x: results[x]['test_score']
        )
        
        return results[best_model_name]['model']
```

### 5.2 Метрики оценки

- **Accuracy**: Общая точность предсказаний
- **Precision/Recall**: Для каждого класса (победа/ничья/поражение)
- **F1-Score**: Гармоническое среднее precision и recall
- **ROC-AUC**: Для вероятностных предсказаний
- **Brier Score**: Качество вероятностных предсказаний
- **Return on Investment (ROI)**: Если использовать для ставок

---

## 🚀 Этап 6: Развертывание

### 6.1 API Service (FastAPI)

```python
# src/api/app.py
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from prediction.predictor import RealMadridPredictor

app = FastAPI(title="Real Madrid Prediction API")

predictor = RealMadridPredictor(model_path="models/best_model.pkl")

class MatchRequest(BaseModel):
    opponent: str
    venue: str  # 'home' or 'away'
    competition: str
    date: str

class PredictionResponse(BaseModel):
    prediction: str
    probabilities: dict
    confidence: float
    recommendation: str

@app.post("/predict", response_model=PredictionResponse)
async def predict_match(match: MatchRequest):
    try:
        # Собрать актуальные данные о матче
        match_data = await collect_match_data(match)
        
        # Получить предсказание
        result = predictor.predict_match(match_data)
        
        return PredictionResponse(**result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/upcoming-matches")
async def get_upcoming_predictions():
    """Получить предсказания для всех предстоящих матчей"""
    upcoming = await fetch_upcoming_matches()
    predictions = []
    
    for match in upcoming:
        pred = predictor.predict_match(match)
        predictions.append({
            'match': match,
            'prediction': pred
        })
    
    return predictions
```

### 6.2 Docker контейнер

```dockerfile
# Dockerfile для AI сервиса
FROM python:3.10-slim

WORKDIR /app

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Копирование кода
COPY src/ ./src/
COPY models/ ./models/
COPY config/ ./config/

# Запуск API
CMD ["uvicorn", "src.api.app:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 6.3 Docker Compose (обновление)

```yaml
# Добавить в существующий docker-compose.yml
services:
  ai-prediction-service:
    build: ./ai-service
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql://user:password@postgres:5432/football_db
      - RABBITMQ_URL=amqp://guest:guest@rabbitmq:5672/
    depends_on:
      - postgres
      - rabbitmq
    volumes:
      - ./models:/app/models
      - ./data:/app/data
```

---

## 📈 Этап 7: Мониторинг и улучшение

### 7.1 Отслеживание производительности

```python
# src/evaluation/performance_tracker.py
class PerformanceTracker:
    def __init__(self, db_connection):
        self.db = db_connection
    
    def log_prediction(self, match_id, prediction, actual_result):
        """Сохранить предсказание и фактический результат"""
        self.db.execute("""
            INSERT INTO predictions 
            (match_id, predicted_result, actual_result, 
             prediction_date, accuracy)
            VALUES (%s, %s, %s, NOW(), %s)
        """, (
            match_id, 
            prediction['result'],
            actual_result,
            prediction['result'] == actual_result
        ))
    
    def calculate_accuracy(self, period='last_30_days'):
        """Рассчитать точность за период"""
        return self.db.query("""
            SELECT 
                COUNT(*) as total_predictions,
                SUM(CASE WHEN accuracy THEN 1 ELSE 0 END) as correct,
                AVG(CASE WHEN accuracy THEN 1.0 ELSE 0.0 END) as accuracy
            FROM predictions
            WHERE prediction_date > NOW() - INTERVAL '30 days'
        """)
```

### 7.2 Автоматическое переобучение

```python
# Настроить cron job или scheduled task
def retrain_model():
    """Переобучение модели на новых данных"""
    # Загрузить новые данные
    data = load_recent_matches(days=180)
    
    # Переобучить модель
    trainer = ModelTrainer()
    new_model = trainer.train_model(data.X, data.y)
    
    # Сравнить с текущей моделью
    if new_model.score > current_model.score:
        # Сохранить новую модель
        joblib.dump(new_model, 'models/best_model.pkl')
        print("Model updated!")
```

---

## 📱 Этап 8: Пользовательский интерфейс

### 8.1 Web Dashboard (React/Vue)

Основные компоненты:
- **Главная страница**: Следующий матч и прогноз
- **История прогнозов**: График точности
- **Календарь матчей**: Прогнозы на все предстоящие игры
- **Статистика**: Детальная статистика команды
- **Сравнение**: Сравнение Реала с соперником

### 8.2 Уведомления

- Email уведомления перед матчами
- Push-уведомления с прогнозами
- Telegram бот для быстрого доступа

---

## ⚡ Этап 9: Оптимизация

### 9.1 Улучшение точности

1. **Больше данных**: 
   - Добавить данные о погоде
   - Психологические факторы (мотивация)
   - Экспертные оценки

2. **Feature Engineering**:
   - Временные признаки (день недели, время сезона)
   - Взаимодействия признаков
   - Полиномиальные признаки

3. **Ансамблирование моделей**:
   - Комбинировать разные модели
   - Стекинг и блендинг

4. **Deep Learning**:
   - LSTM для временных рядов
   - Трансформеры для последовательностей матчей

### 9.2 Производительность

- Кэширование часто используемых данных
- Асинхронные запросы к API
- Batch predictions для множества матчей
- Redis для быстрого доступа

---

## 🔐 Этап 10: Безопасность и соблюдение норм

### 10.1 Безопасность
- API authentication (JWT tokens)
- Rate limiting
- Защита от SQL injection
- HTTPS

### 10.2 Соблюдение условий использования
- Проверить Terms of Service API
- Не использовать для коммерческих ставок без лицензии
- Соблюдать rate limits API

---

## 📋 Чеклист реализации

### Фаза 1: MVP (2-4 недели)
- [ ] Настроить сбор данных из одного API
- [ ] Создать базу данных с историческими данными
- [ ] Реализовать базовую feature engineering
- [ ] Обучить простую модель (Random Forest)
- [ ] Создать простое API для предсказаний
- [ ] Тестирование на исторических данных

### Фаза 2: Улучшение (4-6 недель)
- [ ] Добавить больше источников данных
- [ ] Улучшить feature engineering
- [ ] Экспериментировать с разными моделями
- [ ] Внедрить ансамблирование
- [ ] Создать веб-интерфейс
- [ ] Добавить мониторинг

### Фаза 3: Production (4-6 недель)
- [ ] Автоматизация сбора данных
- [ ] Автоматическое переобучение
- [ ] Масштабирование инфраструктуры
- [ ] Расширенная аналитика
- [ ] Мобильное приложение
- [ ] A/B тестирование моделей

---

## 🎓 Ресурсы для изучения

### Курсы
- Coursera: Machine Learning by Andrew Ng
- Fast.ai: Practical Deep Learning
- DataCamp: Sports Analytics courses

### Книги
- "Machine Learning for Sports Analytics" 
- "Soccermatics" by David Sumpter
- "The Numbers Game" by Chris Anderson

### Полезные ссылки
- Football-Data.org
- FBref.com (статистика)
- Kaggle datasets (European Soccer Database)
- GitHub репозитории с похожими проектами

---

## 💡 Дополнительные идеи

1. **Анализ ожидаемых голов (xG)**
   - Использовать продвинутые метрики

2. **Sentiment Analysis**
   - Анализ настроений в соцсетях перед матчем

3. **Визуализация тактики**
   - Heat maps, pass networks

4. **Live predictions**
   - Обновление прогноза во время матча

5. **Multi-market predictions**
   - Тотал голов, обе забьют, и т.д.

---

## 📊 Ожидаемые результаты

**Реалистичные цели точности:**
- Победа/Ничья/Поражение: 50-60% accuracy
- Вероятность победы: 0.05-0.10 Brier Score
- Better than bookmakers: сложно, но возможно в определенных ситуациях

**Важно**: Футбол - очень непредсказуемый спорт. Даже лучшие модели не могут достичь 100% точности из-за множества случайных факторов.

---

## 🚀 Начало работы

### Шаг 1: Регистрация на Football API
```bash
# Получить API ключ на https://www.football-data.org/
# Бесплатный тариф: 10 запросов в минуту
```

### Шаг 2: Настройка окружения
```bash
# Создать виртуальное окружение
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate  # Windows

# Установить зависимости
pip install pandas numpy scikit-learn xgboost \
    fastapi uvicorn requests sqlalchemy psycopg2
```

### Шаг 3: Первый запрос к API
```python
import requests

API_KEY = 'your_api_key'
headers = {'X-Auth-Token': API_KEY}

# Получить данные о Реал Мадрид
response = requests.get(
    'https://api.football-data.org/v4/teams/86/matches',
    headers=headers
)
matches = response.json()
print(matches)
```

---

**Удачи в создании AI агента! 🎯⚽**
