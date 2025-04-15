package com.example.kursa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * DateHelper — утилитный класс для работы с датами.
 * Предоставляет метод для получения текущей даты в формате "yyyy-MM-dd".
 */
public class DateHelper {
    /**
     * Возвращает текущую дату в формате "yyyy-MM-dd".
     *
     * @return Строка с текущей датой
     */
    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }
}