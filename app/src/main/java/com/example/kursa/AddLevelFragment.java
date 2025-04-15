
package com.example.kursa;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;
/**
 * AddLevelFragment — фрагмент для создания и добавления новых уровней словаря в Firestore.
 * Предоставляет интерфейс для ввода названия уровня, 10 английских слов и их переводов на русский.
 * Выполняет валидацию данных, генерирует уникальный ID уровня, сохраняет уровень в коллекцию "levelsAll"
 * и привязывает его ко всем пользователям в коллекции "levels". Обеспечивает проверку символов,
 * обработку ошибок и обратную связь через Toast-сообщения.
 */
public class AddLevelFragment extends Fragment {

    private TextInputEditText levelNameEditText;
    private TextInputEditText[] englishWords = new TextInputEditText[10];
    private TextInputEditText[] translations = new TextInputEditText[10];
    private Button addLevelButton;
    private FirebaseFirestore db;

    /**
     * Инициализирует UI фрагмента, связывает элементы интерфейса с полями класса и настраивает
     * обработчик нажатия кнопки для добавления уровня.
     *
     * @param inflater           Объект для раздувания layout
     * @param container          Родительский контейнер для фрагмента
     * @param savedInstanceState Сохраненное состояние фрагмента
     * @return                   Надутый View фрагмента
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_level_main_add, container, false);

        db = FirebaseFirestore.getInstance();

        levelNameEditText = view.findViewById(R.id.levelNameEditText);

        englishWords[0] = view.findViewById(R.id.englishWord1);
        englishWords[1] = view.findViewById(R.id.englishWord2);
        englishWords[2] = view.findViewById(R.id.englishWord3);
        englishWords[3] = view.findViewById(R.id.englishWord4);
        englishWords[4] = view.findViewById(R.id.englishWord5);
        englishWords[5] = view.findViewById(R.id.englishWord6);
        englishWords[6] = view.findViewById(R.id.englishWord7);
        englishWords[7] = view.findViewById(R.id.englishWord8);
        englishWords[8] = view.findViewById(R.id.englishWord9);
        englishWords[9] = view.findViewById(R.id.englishWord10);

        translations[0] = view.findViewById(R.id.translation1);
        translations[1] = view.findViewById(R.id.translation2);
        translations[2] = view.findViewById(R.id.translation3);
        translations[3] = view.findViewById(R.id.translation4);
        translations[4] = view.findViewById(R.id.translation5);
        translations[5] = view.findViewById(R.id.translation6);
        translations[6] = view.findViewById(R.id.translation7);
        translations[7] = view.findViewById(R.id.translation8);
        translations[8] = view.findViewById(R.id.translation9);
        translations[9] = view.findViewById(R.id.translation10);

        addLevelButton = view.findViewById(R.id.addLevelButton);

        addLevelButton.setOnClickListener(v -> addLevelToFirestore());

        return view;
    }

    /**
     * Собирает данные из полей ввода, выполняет валидацию (пустые поля, корректность символов),
     * формирует уровень и сохраняет его в Firestore. После успешного сохранения вызывает
     * привязку уровня к пользователям.
     */
    private void addLevelToFirestore() {
        String userLevelName = levelNameEditText.getText().toString().trim();

        if (userLevelName.isEmpty()) {
            levelNameEditText.setError("Введите название уровня");
            return;
        }

        String[] englishWordStrings = new String[10];
        String[] translationStrings = new String[10];

        for (int i = 0; i < 10; i++) {
            englishWordStrings[i] = englishWords[i].getText().toString().trim();
            translationStrings[i] = translations[i].getText().toString().trim();
        }

        for (int i = 0; i < 10; i++) {
            if (englishWordStrings[i].isEmpty() || translationStrings[i].isEmpty()) {
                Toast.makeText(getContext(), "Заполните все поля слов и переводов", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (int i = 0; i < 10; i++) {
            if (!englishWordStrings[i].matches("^[a-zA-Z\\s.,!?'-]+$")) {
                Toast.makeText(getContext(), "Слово " + (i + 1) + " (английское) должно содержать только английские символы", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (int i = 0; i < 10; i++) {
            if (!translationStrings[i].matches("^[а-яА-ЯёЁ\\s.,!?'-]+$")) {
                Toast.makeText(getContext(), "Перевод " + (i + 1) + " (русский) должен содержать только русские символы", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, String> wordsMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            wordsMap.put(englishWordStrings[i], translationStrings[i]);
            Log.d("Firestore", "Добавлено слово: " + englishWordStrings[i] + " -> " + translationStrings[i]);
        }

        Log.d("Firestore", "Всего добавлено слов: " + wordsMap.size());

        generateLevelId(levelId -> {
            int levelNumber = Integer.parseInt(levelId.replace("level", ""));
            String levelName = "Уровень " + levelNumber + ": " + userLevelName;

            Map<String, Object> levelDetails = new HashMap<>();
            levelDetails.put("isUnlocked", levelNumber == 1);
            levelDetails.put("words", wordsMap);
            Map<String, Object> level = new HashMap<>();
            level.put("details", levelDetails);
            level.put("levelName", levelName);

            db.collection("levelsAll")
                    .document(levelId)
                    .set(level)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Уровень успешно добавлен", Toast.LENGTH_SHORT).show();
                        resetFields();
                        bindLevelToUsers(level);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Ошибка при добавлении уровня", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Ошибка: ", e);
                    });
        });
    }

    /**
     * Очищает все поля ввода (название уровня, слова и переводы) после успешного добавления уровня.
     */
    private void resetFields() {
        levelNameEditText.setText("");
        for (int i = 0; i < 10; i++) {
            englishWords[i].setText("");
            translations[i].setText("");
        }
    }

    /**
     * Генерирует уникальный ID уровня на основе количества существующих уровней в коллекции "levelsAll".
     * Передает сгенерированный ID через callback.
     *
     * @param listener Callback для передачи сгенерированного ID
     */
    private void generateLevelId(OnLevelIdGeneratedListener listener) {
        db.collection("levelsAll")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int levelCount = queryDocumentSnapshots.size();
                    String levelId = "level" + (levelCount + 1);
                    listener.onLevelIdGenerated(levelId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка при получении количества уровней", e);
                    listener.onLevelIdGenerated("level1");
                });
    }

    /**
     * Привязывает новый уровень ко всем пользователям в коллекции "levels".
     * Если документ пользователя отсутствует, создает новый. Логирует успех или ошибки.
     *
     * @param level Данные уровня для привязки
     */
    private void bindLevelToUsers(Map<String, Object> level) {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String nickname = document.getString("nickname");

                        if (nickname != null && !nickname.isEmpty()) {
                            db.collection("levels")
                                    .document(nickname)
                                    .update("levels", FieldValue.arrayUnion(level))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Уровень добавлен пользователю: " + nickname);
                                    })
                                    .addOnFailureListener(e -> {
                                        if (e.getMessage() != null && e.getMessage().contains("No document to update")) {
                                            Map<String, Object> userLevels = new HashMap<>();
                                            userLevels.put("levels", FieldValue.arrayUnion(level));

                                            db.collection("levels")
                                                    .document(nickname)
                                                    .set(userLevels)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d("Firestore", "Документ создан и уровень добавлен пользователю: " + nickname);
                                                    })
                                                    .addOnFailureListener(e2 -> {
                                                        Log.e("Firestore", "Ошибка при создании документа: " + nickname, e2);
                                                    });
                                        } else {
                                            Log.e("Firestore", "Ошибка при добавлении уровня пользователю: " + nickname, e);
                                        }
                                    });
                        } else {
                            Log.e("Firestore", "Никнейм пользователя пустой или null");
                        }
                    }
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Ошибка при получении пользователей", e);
                });
    }

    /**
     * Интерфейс для передачи сгенерированного ID уровня через callback.
     */
    interface OnLevelIdGeneratedListener {
        void onLevelIdGenerated(String levelId);
    }
}