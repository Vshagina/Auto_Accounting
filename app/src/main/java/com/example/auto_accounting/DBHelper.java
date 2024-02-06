package com.example.auto_accounting;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 4 ;

    // Таблица для транспортных средств
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String COLUMN_VEHICLE_ID = "_id";
    public static final String COLUMN_TRANSPORT_TYPE = "transport_type";
    public static final String COLUMN_BRAND = "brand";
    public static final String COLUMN_MODEL = "model";
    public static final String COLUMN_VEHICLES_FUEL_TYPE = "vehicles_fuel_type";
    public static final String COLUMN_YEAR = "year";
    // Таблица для заправок
    public static final String TABLE_REFUEL = "refuel";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_FUEL_TYPE = "fuel_type";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_VEHICLE_ID_REFUEL = "vehicle_id";

    // Таблица для сервисов
    public static final String TABLE_SERVICE = "service";
    public static final String COLUMN_SERVICE_ID = "_id";
    public static final String COLUMN_SERVICE_DATE = "date";
    public static final String COLUMN_SERVICE_TYPE = "type";
    public static final String COLUMN_SERVICE_AMOUNT = "amount";
    public static final String COLUMN_SERVICE_VEHICLE_ID = "vehicle_id";

    // Таблица для заметок
    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_NOTE_ID = "_id";
    public static final String COLUMN_NOTE_TEXT = "note_text";
    public static final String COLUMN_NOTE_CHECKED = "note_checked";

    // Таблица для прочего
    public static final String TABLE_OTHER = "other";
    public static final String COLUMN_OTHER_ID = "_id";
    public static final String COLUMN_OTHER_DATE = "date";
    public static final String COLUMN_OTHER_TYPE = "type";
    public static final String COLUMN_OTHER_AMOUNT = "amount";
    public static final String COLUMN_OTHER_VEHICLE_ID = "vehicle_id";

    private final Context context;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // таблица транспортные средства
        String createVehiclesTable = "CREATE TABLE " + TABLE_VEHICLES + " (" +
                COLUMN_VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TRANSPORT_TYPE + " TEXT," +
                COLUMN_BRAND + " TEXT," +
                COLUMN_MODEL + " TEXT," +
                COLUMN_VEHICLES_FUEL_TYPE + " TEXT," +
                COLUMN_YEAR + " TEXT)";
        db.execSQL(createVehiclesTable);
        // таблица заправки
        String createRefuelTable = "CREATE TABLE " + TABLE_REFUEL + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_FUEL_TYPE + " TEXT, "
                + COLUMN_QUANTITY + " REAL, "
                + COLUMN_AMOUNT + " REAL, "
                + COLUMN_VEHICLE_ID_REFUEL + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_VEHICLE_ID_REFUEL + ") REFERENCES " + TABLE_VEHICLES + "(" + COLUMN_VEHICLE_ID + "));";
        db.execSQL(createRefuelTable);
        // таблица сервисов
        String createServiceTable = "CREATE TABLE " + TABLE_SERVICE + " ("
                + COLUMN_SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SERVICE_DATE + " TEXT, "
                + COLUMN_SERVICE_TYPE + " TEXT, "
                + COLUMN_SERVICE_AMOUNT + " TEXT, "
                + COLUMN_SERVICE_VEHICLE_ID + " INTEGER, "
                + "FOREIGN KEY (" + COLUMN_SERVICE_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + " (" + COLUMN_VEHICLE_ID + "))";

        db.execSQL(createServiceTable);

        // таблица заметки
        String createNotesTable = "CREATE TABLE " + TABLE_NOTES + " ("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOTE_TEXT + " TEXT, "
                + COLUMN_NOTE_CHECKED + " INTEGER DEFAULT 0)"; // Новое поле для статуса отмеченности
        db.execSQL(createNotesTable);

        // таблица прочего
        String createOtherTable = "CREATE TABLE " + TABLE_OTHER + " ("
                + COLUMN_OTHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_OTHER_DATE + " TEXT, "
                + COLUMN_OTHER_TYPE + " TEXT, "
                + COLUMN_OTHER_AMOUNT + " TEXT, "
                + COLUMN_OTHER_VEHICLE_ID + " INTEGER, "
                + "FOREIGN KEY (" + COLUMN_OTHER_VEHICLE_ID + ") REFERENCES " + TABLE_VEHICLES + " (" + COLUMN_VEHICLE_ID + "))";

        db.execSQL(createOtherTable);


        // Добавляем новую колонку в таблицу Транспортные средства для хранения последнего вставленного идентификатора
        String addLastInsertedVehicleIdColumn = "ALTER TABLE " + TABLE_VEHICLES +
                " ADD COLUMN last_inserted_vehicle_id INTEGER DEFAULT -1;";

        db.execSQL(addLastInsertedVehicleIdColumn);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // удаление старых таблиц при обновлении базы данных
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REFUEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OTHER);

        // Создание новых таблиц
        onCreate(db);
    }

    @SuppressLint("Range")
    // получение значений из ресурса
    private List<String> getAllResourceValues(int resourceId) {
        List<String> values = new ArrayList<>();
        String[] items = context.getResources().getStringArray(resourceId);
        Collections.addAll(values, items);
        return values;
    }

    @SuppressLint("Range")
    public List<String> getAllTransportItems() {
        // Получение списка элементов из таблицы "Транспортное средство" в базе данных
        SQLiteDatabase db = getReadableDatabase();
        List<String> transportItems = new ArrayList<>();

        // Запрос к таблице "Транспортное средство" и заполнение списка
        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_VEHICLE_ID, COLUMN_BRAND, COLUMN_MODEL}, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String brand = cursor.getString(cursor.getColumnIndex(COLUMN_BRAND));
                String model = cursor.getString(cursor.getColumnIndex(COLUMN_MODEL));
                transportItems.add(brand + " " + model);
            }

            cursor.close();
        }

        db.close();

        return transportItems;
    }
    // для марок автомобилей
    public List<String> getAllBrands() {
        return getAllResourceValues(R.array.brands);
    }

    public List<String> getAllModels() {
        return getAllResourceValues(R.array.models);
    }
    public interface VehicleDataCallback {
        void onSuccess(String transportType, String brand, String model, String vehicles_fuel_type, String year);
        void onFailure(String error);
    }

    // Метод для получения данных на основе выбранного транспортного средства
    public long getVehicleIdForTransportItem(String brand, String model, String transportType, String vehiclesFuelType, String year) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = DBHelper.COLUMN_BRAND + " = ? AND " +
                DBHelper.COLUMN_MODEL + " = ? AND " +
                DBHelper.COLUMN_TRANSPORT_TYPE + " = ? AND " +
                DBHelper.COLUMN_VEHICLES_FUEL_TYPE + " = ? AND " +
                DBHelper.COLUMN_YEAR + " = ?";

        String[] selectionArgs = {brand, model, transportType, vehiclesFuelType, year};

        Cursor cursor = db.query(
                DBHelper.TABLE_VEHICLES,
                new String[]{DBHelper.COLUMN_VEHICLE_ID},
                selection,
                selectionArgs,
                null, null, null
        );

        long vehicleId = -1;

        if (cursor != null && cursor.moveToFirst()) {
            vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_VEHICLE_ID));
            cursor.close();
        }

        db.close();

        return vehicleId;
    }




    public String getBrandForTransportItem(String transportItem) {
        String[] parts = transportItem.split(" ");
        return parts[0]; // Первая часть строки - марка
    }

    public String getModelForTransportItem(String transportItem) {
        String[] parts = transportItem.split(" ");
        return parts[1]; // Вторая часть строки - модель
    }
    @SuppressLint("Range")
    public String getTransportTypeForTransportItem(String transportItem) {
        SQLiteDatabase db = getReadableDatabase();
        String transportType = "";

        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_TRANSPORT_TYPE},
                COLUMN_BRAND + " || ' ' || " + COLUMN_MODEL + " = ?",
                new String[]{transportItem},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            transportType = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSPORT_TYPE));
            cursor.close();
        }

        db.close();

        return transportType;
    }
    // Метод для получения типа топлива для выбранного транспортного средства
    @SuppressLint("Range")
    public String getVehiclesFuelTypeForTransportItem(String transportItem) {
        SQLiteDatabase db = getReadableDatabase();
        String vehicles_fuel_type = "";

        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_VEHICLES_FUEL_TYPE},
                COLUMN_BRAND + " || ' ' || " + COLUMN_MODEL + " = ?",
                new String[]{transportItem},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            vehicles_fuel_type = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLES_FUEL_TYPE));
            cursor.close();
        }

        db.close();

        return vehicles_fuel_type;
    }
    // Метод для обновления данных о транспортном средстве
    public void updateVehicleData(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSPORT_TYPE, transportType);
        values.put(COLUMN_BRAND, brand);
        values.put(COLUMN_MODEL, model);
        values.put(COLUMN_VEHICLES_FUEL_TYPE, vehicles_fuel_type);
        values.put(COLUMN_YEAR, year);

        // Обновление записи в базе данных по vehicleId
        int updatedRows = db.update(TABLE_VEHICLES, values, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
        db.close();

        if (updatedRows > 0) {
            Log.d("DBHelper", "Vehicle data updated successfully");
        } else {
            Log.d("DBHelper", "No vehicle data updated");
        }
    }
    // Метод для удаления транспортного средства по идентификатору
    public void deleteVehicle(long vehicleId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            int result = db.delete(TABLE_VEHICLES, COLUMN_VEHICLE_ID + " = ?", new String[]{String.valueOf(vehicleId)});
            if (result > 0) {
                Log.d("DBHelper", "Vehicle deleted successfully");

                // Проверяем, остались ли еще транспортные средства
                if (getVehicleCount() == 0) {
                    // Если транспортных средств больше нет, обновляем последний вставленный идентификатор в SharedPreferences
                    SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong("lastInsertedVehicleId", 0);
                    editor.apply();
                }
            } else {
                Log.d("DBHelper", "No vehicle deleted");
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error deleting vehicle: " + e.getMessage());
        }
    }
    // Метод для проверки наличия транспортных средств в базе данных
    public boolean hasVehicles() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VEHICLES, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }

        return count > 0;
    }
    @SuppressLint("Range")
    public void getVehicleData(long vehicleId, VehicleDataCallback callback) {
        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_VEHICLE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(vehicleId)};

        Cursor cursor = db.query(TABLE_VEHICLES, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String transportType = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSPORT_TYPE));
            String brand = cursor.getString(cursor.getColumnIndex(COLUMN_BRAND));
            String model = cursor.getString(cursor.getColumnIndex(COLUMN_MODEL));
            String vehicles_fuel_type = cursor.getString(cursor.getColumnIndex(COLUMN_VEHICLES_FUEL_TYPE));
            String year = cursor.getString(cursor.getColumnIndex(COLUMN_YEAR));

            cursor.close();
            db.close();

            callback.onSuccess(transportType, brand, model, vehicles_fuel_type, year);
        } else {
            db.close();
            callback.onFailure("Error retrieving vehicle data");
        }
    }


    // Метод для получения количества транспортных средств в базе данных
    public int getVehicleCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VEHICLES, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        }

        db.close();

        return count;
    }

    @SuppressLint("Range")
    public String getYearForTransportItem(String transportItem) {
        SQLiteDatabase db = getReadableDatabase();
        String year = "";

        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_YEAR},
                COLUMN_BRAND + " || ' ' || " + COLUMN_MODEL + " = ?",
                new String[]{transportItem},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            year = cursor.getString(cursor.getColumnIndex(COLUMN_YEAR));
            cursor.close();
        }

        db.close();

        return year;
    }

    @SuppressLint("Range")
    public long getLastInsertedVehicleId() {
        SQLiteDatabase db = getReadableDatabase();
        long lastVehicleId = -1;

        Cursor cursor = db.query(TABLE_VEHICLES, new String[]{COLUMN_VEHICLE_ID},
                null, null, null, null, COLUMN_VEHICLE_ID + " DESC", "1");

        if (cursor != null && cursor.moveToFirst()) {
            lastVehicleId = cursor.getLong(cursor.getColumnIndex(COLUMN_VEHICLE_ID));
            cursor.close();
        }

        db.close();

        return lastVehicleId;
    }


    public long insertNote(String noteText) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NOTE_TEXT, noteText);
        values.put(DBHelper.COLUMN_NOTE_CHECKED, 0);

        long newRowId = db.insertWithOnConflict(DBHelper.TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();

        return newRowId;
    }
    public int deleteNoteById(long noteId) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = DBHelper.COLUMN_NOTE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(noteId)};

        // Удалите запись из базы данных и получите количество удаленных строк
        int deletedRows = db.delete(DBHelper.TABLE_NOTES, selection, selectionArgs);

        db.close();

        return deletedRows;
    }
    public long insertRefillData(RefillData refillData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FUEL_TYPE, refillData.getFuelType());
        values.put(COLUMN_QUANTITY, refillData.getQuantity());
        values.put(COLUMN_DATE, refillData.getDate());
        values.put(COLUMN_VEHICLE_ID_REFUEL, refillData.getVehicleId());

        // Вставляем строку в таблицу
        long newRowId = db.insert(TABLE_REFUEL, null, values);

        db.close();

        return newRowId;
    }
    public void deleteRefillData(long refillId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "_id = ?";
        String[] selectionArgs = {String.valueOf(refillId)};
        db.delete(TABLE_REFUEL, selection, selectionArgs);
        db.close();
    }

    public long insertServiceData(ServiceData serviceData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SERVICE_DATE, serviceData.getDate());
        values.put(COLUMN_SERVICE_TYPE, serviceData.getServiceType());
        values.put(COLUMN_SERVICE_AMOUNT, serviceData.getAmount());
        values.put(COLUMN_SERVICE_VEHICLE_ID, serviceData.getVehicleId());

        // Вставляем строку в таблицу
        long newRowId = db.insert(TABLE_SERVICE, null, values);

        db.close();

        return newRowId;
    }
    public void deleteServiceData(long serviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "_id = ?";
        String[] selectionArgs = {String.valueOf(serviceId)};
        db.delete(TABLE_SERVICE, selection, selectionArgs);
        db.close();
    }
    public long insertOtherData(OtherData otherData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_OTHER_DATE, otherData.getDateOther());
        values.put(COLUMN_OTHER_TYPE, otherData.getOtherType());
        values.put(COLUMN_OTHER_AMOUNT, otherData.getAmountOther());
        values.put(COLUMN_OTHER_VEHICLE_ID, otherData.getVehicleId());

        // Вставляем строку в таблицу
        long newRowId = db.insert(TABLE_OTHER, null, values);

        db.close();

        return newRowId;
    }
    public void deleteOtherData(long otherId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "_id = ?";
        String[] selectionArgs = {String.valueOf(otherId)};
        db.delete(TABLE_OTHER, selection, selectionArgs);
        db.close();
    }
    @SuppressLint("Range")
    public Map<String, Double> getServiceDataForVehicleFromDatabase(long vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Map<String, Double> serviceData = new HashMap<>();

        // SQL-запрос для выборки суммы всех сервисов для каждого типа для указанного ТС
        String query = "SELECT " + COLUMN_SERVICE_TYPE + ", SUM(" + COLUMN_SERVICE_AMOUNT + ") AS total_amount " +
                "FROM " + TABLE_SERVICE +
                " WHERE " + COLUMN_SERVICE_VEHICLE_ID + " = ?" +
                " GROUP BY " + COLUMN_SERVICE_TYPE;

        // Выполнение запроса с использованием параметра vehicleId
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(vehicleId)});

        // Обход результатов запроса и добавление данных в Map
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String serviceType = cursor.getString(cursor.getColumnIndex(COLUMN_SERVICE_TYPE));
                double totalAmount = cursor.getDouble(cursor.getColumnIndex("total_amount"));
                serviceData.put(serviceType, totalAmount);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Закрытие базы данных
        db.close();

        return serviceData;
    }
    @SuppressLint("Range")
    public Map<String, Double> getRefuelDataForVehicleFromDatabase(long vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Double> refuelData = new HashMap<>();

        // Выполните запрос к таблице "Заправка" и получите данные о заправках для указанного ТС
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_FUEL_TYPE + ", SUM(" + COLUMN_QUANTITY + ") AS total_quantity " +
                "FROM " + TABLE_REFUEL +
                " WHERE " + COLUMN_VEHICLE_ID_REFUEL + " = ?" +
                " GROUP BY " + COLUMN_FUEL_TYPE, new String[]{String.valueOf(vehicleId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String fuelType = cursor.getString(cursor.getColumnIndex(COLUMN_FUEL_TYPE));
                double totalQuantity = cursor.getDouble(cursor.getColumnIndex("total_quantity"));
                refuelData.put(fuelType, totalQuantity);
            }
            cursor.close();
        }

        db.close();

        return refuelData;
    }
    @SuppressLint("Range")
    public Map<String, Double> getOtherDataForVehicleFromDatabase(long vehicleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Double> otherData = new HashMap<>();

        // Выполните запрос к таблице "Прочее" и получите данные о прочих расходах для указанного ТС
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_OTHER_TYPE + ", SUM(" + COLUMN_OTHER_AMOUNT + ") AS total_amount " +
                "FROM " + TABLE_OTHER +
                " WHERE " + COLUMN_OTHER_VEHICLE_ID + " = ?" +
                " GROUP BY " + COLUMN_OTHER_TYPE, new String[]{String.valueOf(vehicleId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String otherType = cursor.getString(cursor.getColumnIndex(COLUMN_OTHER_TYPE));
                double totalAmount = cursor.getDouble(cursor.getColumnIndex("total_amount"));
                otherData.put(otherType, totalAmount);
            }
            cursor.close();
        }

        db.close();

        return otherData;
    }
}
