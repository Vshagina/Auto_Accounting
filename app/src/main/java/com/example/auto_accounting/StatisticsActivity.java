package com.example.auto_accounting;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Pair;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StatisticsActivity extends AppCompatActivity {
    private BarChart barChart;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Статистика");

        // Получение идентификатора последнего выбранного ТС из SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        long lastInsertedVehicleId = preferences.getLong("lastInsertedVehicleId", -1);
        barChart = findViewById(R.id.bar_chart);

        dbHelper = new DBHelper(this); // Создание экземпляра DBHelper

        // Получиние данных о сервисах для последнего выбранного ТС из базы данных, используя DBHelper
        Map<String, Double> serviceDataForLastVehicle = dbHelper.getServiceDataForVehicleFromDatabase(lastInsertedVehicleId);
        // Получиние данных о заправках для последнего выбранного ТС из базы данных, используя DBHelper
        Map<String, Double> refuelDataForLastVehicle = dbHelper.getRefuelDataForVehicleFromDatabase(lastInsertedVehicleId);
        Map<String, Double> otherDataForLastVehicle = dbHelper.getOtherDataForVehicleFromDatabase(lastInsertedVehicleId);
        // Подготовка данных для отображения на столбчатой диаграмме
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, getTotalServiceAmount(serviceDataForLastVehicle)));
        entries.add(new BarEntry(1, getTotalRefuelAmount(refuelDataForLastVehicle)));
        entries.add(new BarEntry(2, getTotalOtherAmount(otherDataForLastVehicle)));
        // Создание списка цветов для каждого столбца
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.GREEN); // Цвет для сервисов
        colors.add(Color.BLUE); // Цвет для заправок
        colors.add(Color.CYAN); // Цвет для прочего

        // Создание набора данных и установите его на диаграмму
        BarDataSet dataSet = new BarDataSet(entries, "Сервисы Заправки Прочее");
        dataSet.setValueTextColor(Color.BLACK); // Цвет значений внутри столбцов
        dataSet.setValueTextSize(12f); // Размер текста значений внутри столбцов

        dataSet.setColors(colors); // Установка цветов столбцов
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Обновление отображения диаграммы
        barChart.invalidate();
    }

    // Метод для вычисления общей суммы сервисов
    private float getTotalServiceAmount(Map<String, Double> serviceData) {
        float total = 0;
        for (Double amount : serviceData.values()) {
            total += amount.floatValue();
        }
        return total;
    }

    // Метод для вычисления общей суммы заправок
    private float getTotalRefuelAmount(Map<String, Double> refuelData) {
        float total = 0;
        for (Double amount : refuelData.values()) {
            total += amount.floatValue();
        }
        return total;
    }
    // Метод для вычисления общей суммы прочего
    private float getTotalOtherAmount(Map<String, Double> otherData) {
        float total = 0;
        for (Double amount : otherData.values()) {
            total += amount.floatValue();
        }
        return total;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Обработка нажатия на элемент тулбара
        if (item.getItemId() == android.R.id.home) {
            // Закрыть активность
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
