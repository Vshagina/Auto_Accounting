package com.example.auto_accounting;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ServiceFragment extends Fragment {
    private DBHelper dbHelper;
    private AutoCompleteTextView typeService;
    private EditText timeAndDateService;
    private EditText amountService;
    private long lastVehicleId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ServiceFragment() {
        // Required empty public constructor
    }
    public static ServiceFragment newInstance(String param1, String param2) {
        ServiceFragment fragment = new ServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(requireContext());
        lastVehicleId = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getLong("lastInsertedVehicleId", 0);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);

        timeAndDateService = view.findViewById(R.id.timeAndDateService);
        typeService = view.findViewById(R.id.typeService);
        amountService = view.findViewById(R.id.amountService);

        timeAndDateService.setText(getCurrentDateTime());

        setupAutoCompleteAdapter(typeService, Arrays.asList(getResources().getStringArray(R.array.services)));


        Button addButton = view.findViewById(R.id.buttonService);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = typeService.getText().toString();
                double amount;

                try {
                    amount = Double.parseDouble(amountService.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Проверьте введённые данные!", Toast.LENGTH_SHORT).show();
                    return; // Выйти из метода при возникновении ошибки
                }
                if (lastVehicleId == -1) {
                    if (!dbHelper.hasVehicles()) {
                        openAddTsFragment();
                        return;
                    }

                    Toast.makeText(requireContext(), "Выберите транспортное средство", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.putNull(DBHelper.COLUMN_ID); // Поле _id будет автоматически установлено
                values.put(DBHelper.COLUMN_SERVICE_DATE, getCurrentDateTime());
                values.put(DBHelper.COLUMN_SERVICE_TYPE, type);
                values.put(DBHelper.COLUMN_SERVICE_AMOUNT, amount);
                values.put(DBHelper.COLUMN_SERVICE_VEHICLE_ID, lastVehicleId);

                long newRowId = db.insertWithOnConflict(DBHelper.TABLE_SERVICE, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                db.close();

                if (newRowId != -1) {
                    Toast.makeText(requireContext(), "Сервис успешно добавлен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                }

                timeAndDateService.setText(getCurrentDateTime());
            }
        });

        return view;
    }

    private void setupAutoCompleteAdapter(AutoCompleteTextView autoCompleteTextView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, data);
        autoCompleteTextView.setAdapter(adapter);

        // количество выпадающих данных
        autoCompleteTextView.setThreshold(5);
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
    }
    private String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
    private void openAddTsFragment() {
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new AddTsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

}

