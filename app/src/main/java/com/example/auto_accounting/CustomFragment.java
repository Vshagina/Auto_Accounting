package com.example.auto_accounting;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CustomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class CustomFragment extends Fragment {

    private long vehicleId;
    private String brand;
    private String model;
    private DBHelper dbHelper;
    private long lastSelectedVehicleId;
    private String transportType;
    private String vehicles_fuel_type;
    private String year;

    private long lastVehicleId;

    private RecyclerView recyclerViewRefill, recyclerViewService, recyclerViewOther;
    private RefillAdapter refillAdapter;
    private ServiceAdapter serviceAdapter;
    private OtherAdapter otherAdapter;

    public static CustomFragment newInstance(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        CustomFragment fragment = new CustomFragment();
        Bundle args = new Bundle();
        args.putLong("vehicleId", vehicleId);
        args.putString("transportType", transportType);
        args.putString("brand", brand);
        args.putString("model", model);
        args.putString("vehicles_fuel_type", vehicles_fuel_type);
        args.putString("year", year);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleId = getArguments().getLong("vehicleId");
            transportType = getArguments().getString("transportType");
            brand = getArguments().getString("brand");
            model = getArguments().getString("model");
            vehicles_fuel_type = getArguments().getString("vehicles_fuel_type");
            year = getArguments().getString("year");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom, container, false);
        dbHelper = new DBHelper(requireContext());

        TextView makeAndModelTextView = view.findViewById(R.id.makeAndModel);
        dbHelper = new DBHelper(requireContext());
        lastVehicleId = dbHelper.getLastInsertedVehicleId();

//заправка
        recyclerViewRefill = view.findViewById(R.id.recyclerViewRefill);
        recyclerViewRefill.setLayoutManager(new LinearLayoutManager(requireContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerViewRefill);

        refillAdapter = new RefillAdapter(requireContext(), getRefillDataForVehicle(vehicleId));
        recyclerViewRefill.setAdapter(refillAdapter);
//сервисы
        recyclerViewService = view.findViewById(R.id.recyclerViewService);
        recyclerViewService.setLayoutManager(new LinearLayoutManager(requireContext()));

        ItemTouchHelper itemTouchHelperService = new ItemTouchHelper(new SwipeToDeleteServiceCallback());
        itemTouchHelperService.attachToRecyclerView(recyclerViewService);

        serviceAdapter = new ServiceAdapter(requireContext(), getServiceDataForVehicle(vehicleId));
        recyclerViewService.setAdapter(serviceAdapter);
//прочее
        recyclerViewOther = view.findViewById(R.id.recyclerViewOther);
        recyclerViewOther.setLayoutManager(new LinearLayoutManager(requireContext()));

        ItemTouchHelper itemTouchHelperOther = new ItemTouchHelper(new SwipeToDeleteOtherCallback());
        itemTouchHelperOther.attachToRecyclerView(recyclerViewOther);

        otherAdapter = new OtherAdapter(requireContext(), getOtherDataForVehicle(vehicleId));
        recyclerViewOther.setAdapter(otherAdapter);


        if (getArguments() != null) {
            vehicleId = getArguments().getLong("vehicleId");
            transportType = getArguments().getString("transportType");
            brand = getArguments().getString("brand");
            model = getArguments().getString("model");
            vehicles_fuel_type = getArguments().getString("vehicles_fuel_type");
            year = getArguments().getString("year");

            String displayText = brand + " " + model;
            makeAndModelTextView.setText(displayText);

            makeAndModelTextView.setOnClickListener(v -> showEditDialog(vehicleId, transportType, brand, model, vehicles_fuel_type, year));
        }

        return view;


    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            // Обработка свайпа влево (удаление)
            if (direction == ItemTouchHelper.LEFT) {
                refillAdapter.removeItem(position);
            }
            // Обработка свайпа вправо (дублирование)
            else if (direction == ItemTouchHelper.RIGHT) {
                // Дублируем запись
                RefillData selectedRefill = refillAdapter.getItem(position);
                duplicateRefillRecord(selectedRefill);

                // Уведомляем адаптер об изменении данных
                refillAdapter.notifyDataSetChanged();

                // Добавляем новую запись в базу данных
                long newRefillId = dbHelper.insertRefillData(selectedRefill);

                if (newRefillId != -1) {
                    Toast.makeText(requireContext(), "Запись успешно дублирована", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // метод для установки различных цветов фона для свайпов
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            if (dX > 0) { // Свайп вправо (дублирование)
                Drawable editBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorEditSwipeBackground));
                editBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                editBackground.draw(c);

                // Отобразите надпись "Дублирование"
                drawText(c, "Повторить", itemView.getLeft() + 100, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            } else if (dX < 0) { // Свайп влево (удаление)
                Drawable deleteBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDeleteSwipeBackground));
                deleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                deleteBackground.draw(c);

                // Отобразите надпись "Удаление"
                drawText(c, "Удаление", itemView.getRight() - 250, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            }
        }

        // метод для отображения текста при свайпе внутри элемента
        private void drawText(Canvas canvas, String text, float x, float y, int color) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(40);
            paint.setAntiAlias(true);
            canvas.drawText(text, x, y, paint);
        }
    }

    private void duplicateRefillRecord(RefillData selectedRefill) {
        // Создание новый объект RefillData с теми же данными, кроме ID
        RefillData duplicatedRefill = new RefillData(
                0,
                lastVehicleId,
                selectedRefill.getFuelType(),
                selectedRefill.getQuantity(),
                selectedRefill.getDate()
        );

        // Установите новую дату для дублированной записи
        duplicatedRefill.setDate(getCurrentDateTime()); 

        // Добавьте дубликат записи в список и уведомите адаптер
        refillAdapter.addItem(duplicatedRefill);
    }
    private String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
    private List<RefillData> getRefillDataForVehicle(long vehicleId) {
        List<RefillData> refillDataList = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DBHelper(requireContext());
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (db == null) {
            return refillDataList;
        }

        String[] projection = {
                "_id",
                DBHelper.COLUMN_FUEL_TYPE,
                DBHelper.COLUMN_QUANTITY,
                DBHelper.COLUMN_DATE
        };

        String selection = DBHelper.COLUMN_VEHICLE_ID_REFUEL + " = ?";
        String[] selectionArgs = {String.valueOf(vehicleId)};

        Cursor cursor = db.query(
                DBHelper.TABLE_REFUEL,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long refillId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String fuelType = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_FUEL_TYPE));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_QUANTITY));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_DATE));

            RefillData refillData = new RefillData(refillId,vehicleId, fuelType, quantity, date);
            refillDataList.add(refillData);
        }

        cursor.close();
        db.close();

        return refillDataList;
    }


    private static class RefillAdapter extends RecyclerView.Adapter<RefillAdapter.RefillViewHolder> {

        private List<RefillData> refillDataList;
        private Context context;


        public RefillAdapter(Context context, List<RefillData> refillDataList) {
            this.context = context;
            this.refillDataList = refillDataList;
        }

        @NonNull
        @Override
        public RefillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_refill, parent, false);
            return new RefillViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RefillViewHolder holder, int position) {
            RefillData refillData = refillDataList.get(position);
            holder.textViewFuelType.setText(refillData.getFuelType());
            holder.textViewQuantity.setText(" Сумма: " + refillData.getQuantity());
            holder.textViewDate.setText(" Дата: " + refillData.getDate());
        }

        @Override
        public int getItemCount() {
            return refillDataList.size();
        }

        public void removeItem(int position) {
            RefillData removedRefill = refillDataList.get(position);

            // Удаление элемента из списка
            refillDataList.remove(position);
            // Уведомление адаптера об удалении элемента
            notifyItemRemoved(position);

            // Создание экземпляра DBHelper и вызов метода deleteRefillData
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.deleteRefillData(removedRefill.getRefillId());
        }
        public void addItem(RefillData item) {
            refillDataList.add(item);
            notifyItemInserted(refillDataList.size() - 1);
        }

        public RefillData getItem(int position) {
            return refillDataList.get(position);
        }

        public static class RefillViewHolder extends RecyclerView.ViewHolder {
            TextView textViewFuelType;
            TextView textViewQuantity;
            TextView textViewDate;

            public RefillViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewFuelType = itemView.findViewById(R.id.textViewFuelType);
                textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
                textViewDate = itemView.findViewById(R.id.textViewDate);
            }
        }
    }



    private void showEditDialog(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Редактировать");
        builder.setMessage("Выберите действие");

        builder.setPositiveButton("Редактировать", (dialog, which) -> {
            lastSelectedVehicleId = vehicleId;
            openEditFragment(vehicleId, transportType, brand, model, vehicles_fuel_type, year);
            dialog.dismiss();
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void openEditFragment(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        EditTsFragment editFragment = EditTsFragment.newInstance(vehicleId, transportType, brand, model, vehicles_fuel_type, year);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private class SwipeToDeleteServiceCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeToDeleteServiceCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            // Обработка свайпа влево (удаление)
            if (direction == ItemTouchHelper.LEFT) {
                serviceAdapter.removeItem(position);
            }
            // Обработка свайпа вправо (дублирование)
            else if (direction == ItemTouchHelper.RIGHT) {
                // Дублируем запись
                ServiceData selectedService = serviceAdapter.getItem(position);
                duplicateServiceRecord(selectedService);

                // Уведомляем адаптер об изменении данных
                serviceAdapter.notifyDataSetChanged();

                // Добавляем новую запись в базу данных
                long newServiceId = dbHelper.insertServiceData(selectedService);

                if (newServiceId != -1) {
                    Toast.makeText(requireContext(), "Запись успешно дублирована", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        }
        // метод для установки различных цветов фона для свайпов
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            // Установка разные цвета фона для свайпов вправо (дублирование) и влево (удаление)
            if (dX > 0) { // Свайп вправо (дублирование)
                Drawable editBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorEditSwipeBackground));
                editBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                editBackground.draw(c);

                // надпись "Дублирование"
                drawText(c, "Повторить", itemView.getLeft() + 100, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            } else if (dX < 0) { // Свайп влево (удаление)
                Drawable deleteBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDeleteSwipeBackground));
                deleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                deleteBackground.draw(c);

                // надпись "Удаление"
                drawText(c, "Удаление", itemView.getRight() - 250, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            }
        }

        // метод для отображения текста при свайпе внутри элемента
        private void drawText(Canvas canvas, String text, float x, float y, int color) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(40);
            paint.setAntiAlias(true);
            canvas.drawText(text, x, y, paint);
        }
    }

    private void duplicateServiceRecord(ServiceData selectedService) {
        // Создание нового объекта ServiceData с теми же данными, кроме ID
        ServiceData duplicatedService = new ServiceData(
                0,
                lastVehicleId,
                selectedService.getServiceType(),
                selectedService.getAmount(),
                selectedService.getDate()
        );

        // Добавьте дубликат записи в список и уведомите адаптер
        serviceAdapter.addItem(duplicatedService);
    }
    private class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

        private List<ServiceData> serviceDataList;
        private Context context; 


        public ServiceAdapter(Context context, List<ServiceData> serviceDataList) {
            this.context = context;
            this.serviceDataList = serviceDataList;
        }

        @NonNull
        @Override
        public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_service, parent, false);
            return new ServiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
            ServiceData serviceData = serviceDataList.get(position);
            holder.textViewServiceType.setText(serviceData.getServiceType());
            holder.textViewServiceAmount.setText(" Сумма: " + serviceData.getAmount());
            holder.textViewServiceDate.setText(" Дата: " + serviceData.getDate());
        }

        @Override
        public int getItemCount() {
            return serviceDataList.size();
        }
        // Метод для добавления элемента в список

        public void removeItem(int position) {
            ServiceData removedService = serviceDataList.get(position);

            // Удаление элемента из списка
            serviceDataList.remove(position);
            // Уведомление адаптера об удалении элемента
            notifyItemRemoved(position);

            // Создание экземпляра DBHelper и вызов метода deleteRefillData
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.deleteServiceData(removedService.getServiceId());
        }
        public ServiceData getItem(int position) {
            return serviceDataList.get(position);
        }
        public void addItem(ServiceData item) {
            serviceDataList.add(item);
            notifyItemInserted(serviceDataList.size() - 1);
        }

        private class ServiceViewHolder extends RecyclerView.ViewHolder {
            TextView textViewServiceType;
            TextView textViewServiceAmount;
            TextView textViewServiceDate;

            public ServiceViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewServiceType = itemView.findViewById(R.id.textViewServiceType);
                textViewServiceAmount = itemView.findViewById(R.id.textViewServiceAmount);
                textViewServiceDate = itemView.findViewById(R.id.textViewServiceDate);
            }
        }
    }
    private List<ServiceData> getServiceDataForVehicle(long vehicleId) {
        List<ServiceData> serviceDataList = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DBHelper(requireContext());
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (db == null) {
            return serviceDataList;
        }

        String[] projection = {
                "_id",
                DBHelper.COLUMN_SERVICE_TYPE,
                DBHelper.COLUMN_SERVICE_AMOUNT,
                DBHelper.COLUMN_SERVICE_DATE
        };

        String selection = DBHelper.COLUMN_SERVICE_VEHICLE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(vehicleId)};

        Cursor cursor = db.query(
                DBHelper.TABLE_SERVICE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long serviceId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String serviceType = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVICE_TYPE));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVICE_AMOUNT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVICE_DATE));

            ServiceData serviceData = new ServiceData(serviceId, vehicleId, serviceType, amount, date);
            serviceDataList.add(serviceData);
        }

        cursor.close();
        db.close();

        return serviceDataList;
    }
    private class SwipeToDeleteOtherCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeToDeleteOtherCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            // Обработка свайпа влево (удаление)
            if (direction == ItemTouchHelper.LEFT) {
                otherAdapter.removeItem(position);
            }
            // Обработка свайпа вправо (дублирование)
            else if (direction == ItemTouchHelper.RIGHT) {
                // Дублируем запись
                OtherData selectedOther = otherAdapter.getItem(position);
                duplicateOtherRecord(selectedOther);

                // Уведомляем адаптер об изменении данных
                otherAdapter.notifyDataSetChanged();

                // Добавляем новую запись в базу данных
                long newOtherId = dbHelper.insertOtherData(selectedOther);

                if (newOtherId != -1) {
                    Toast.makeText(requireContext(), "Запись успешно дублирована", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // метод для установки различных цветов фона для свайпов
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20;

            // Установка разные цвета фона для свайпов вправо (дублирование) и влево (удаление)
            if (dX > 0) { // Свайп вправо (дублирование)
                Drawable editBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorEditSwipeBackground));
                editBackground.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                editBackground.draw(c);

                // надпись "Дублирование"
                drawText(c, "Повторить", itemView.getLeft() + 100, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            } else if (dX < 0) { // Свайп влево (удаление)
                Drawable deleteBackground = new ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorDeleteSwipeBackground));
                deleteBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                deleteBackground.draw(c);

                // надпись "Удаление"
                drawText(c, "Удаление", itemView.getRight() - 250, itemView.getTop() + itemView.getHeight() / 2, Color.WHITE);
            }
        }

        // метод для отображения текста при свайпе внутри элемента
        private void drawText(Canvas canvas, String text, float x, float y, int color) {
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(40);
            paint.setAntiAlias(true);
            canvas.drawText(text, x, y, paint);
        }
    }
    private void duplicateOtherRecord(OtherData selectedOther) {
        // Создание нового объекта OtherData с теми же данными, кроме ID
        OtherData duplicatedOther = new OtherData(
                0,
                lastVehicleId,
                selectedOther.getOtherType(),
                selectedOther.getAmountOther(),
                selectedOther.getDateOther()
        );

        // Добавьте дубликат записи в список и уведомите адаптер
        otherAdapter.addItem(duplicatedOther);
    }
    private class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.OtherViewHolder> {

        private List<OtherData> otherDataList;
        private Context context;

        public OtherAdapter(Context context, List<OtherData> otherDataList) {
            this.context = context;
            this.otherDataList = otherDataList;
        }

        @NonNull
        @Override
        public OtherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_other, parent, false);
            return new OtherViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OtherViewHolder holder, int position) {
            OtherData otherData = otherDataList.get(position);
            holder.textViewOtherType.setText(otherData.getOtherType());
            holder.textViewAmount.setText(" Сумма: " + otherData.getAmountOther());
            holder.textViewDate.setText(" Дата: " + otherData.getDateOther());
        }

        @Override
        public int getItemCount() {
            return otherDataList.size();
        }

        public void removeItem(int position) {
            OtherData removedOther = otherDataList.get(position);

            // Удаление элемента из списка
            otherDataList.remove(position);
            // Уведомление адаптера об удалении элемента
            notifyItemRemoved(position);

            // Создание экземпляра DBHelper и вызов метода deleteOtherData
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.deleteOtherData(removedOther.getOtherId());
        }

        public void addItem(OtherData item) {
            otherDataList.add(item);
            notifyItemInserted(otherDataList.size() - 1);
        }

        public OtherData getItem(int position) {
            return otherDataList.get(position);
        }

        private class OtherViewHolder extends RecyclerView.ViewHolder {
            TextView textViewOtherType;
            TextView textViewAmount;
            TextView textViewDate;

            public OtherViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewOtherType = itemView.findViewById(R.id.textViewOtherType);
                textViewAmount = itemView.findViewById(R.id.textViewOtherAmount);
                textViewDate = itemView.findViewById(R.id.textViewOtherDate);
            }
        }
    }
    private List<OtherData> getOtherDataForVehicle(long vehicleId) {
        List<OtherData> otherDataList = new ArrayList<>();

        if (dbHelper == null) {
            dbHelper = new DBHelper(requireContext());
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if (db == null) {
            return otherDataList;
        }

        String[] projection = {
                "_id",
                DBHelper.COLUMN_OTHER_TYPE,
                DBHelper.COLUMN_OTHER_AMOUNT,
                DBHelper.COLUMN_OTHER_DATE
        };

        String selection = DBHelper.COLUMN_OTHER_VEHICLE_ID + " = ?";
        String[] selectionArgs = {String.valueOf(vehicleId)};

        Cursor cursor = db.query(
                DBHelper.TABLE_OTHER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long otherId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String otherType = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_OTHER_TYPE));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_OTHER_AMOUNT));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_OTHER_DATE));

            OtherData otherData = new OtherData(otherId, vehicleId, otherType, amount, date);
            otherDataList.add(otherData);
        }

        cursor.close();
        db.close();

        return otherDataList;
    }

}
