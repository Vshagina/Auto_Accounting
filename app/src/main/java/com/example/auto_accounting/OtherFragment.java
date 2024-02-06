package com.example.auto_accounting;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OtherFragment extends Fragment {

    private AutoCompleteTextView typeOther;
    private EditText amountOther;
    private EditText timeAndDateOther;
    private DBHelper dbHelper;
    private long lastVehicleId;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public OtherFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static OtherFragment newInstance(long selectedVehicleId) {
        OtherFragment fragment = new OtherFragment();
        Bundle args = new Bundle();
        args.putLong("selectedVehicleId", selectedVehicleId);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other, container, false);

        typeOther = view.findViewById(R.id.typeOther);
        amountOther = view.findViewById(R.id.amountOther);
        timeAndDateOther = view.findViewById(R.id.timeAndDateOther);
        timeAndDateOther.setText(getCurrentDateTime());

        setupAutoCompleteAdapter(typeOther, Arrays.asList(getResources().getStringArray(R.array.other)));
        long selectedVehicleId = getSelectedVehicleId();

        Button addButton = view.findViewById(R.id.buttonOther);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TypeOther = typeOther.getText().toString();
                String amountOtherStr = amountOther.getText().toString();
                double amount;

                try {
                    amount = Double.parseDouble(amountOtherStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Проверьте введённые данные!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long selectedVehicleId = getSelectedVehicleId();
                if (selectedVehicleId == -1) {
                    Toast.makeText(requireContext(), "Выберите транспортное средство", Toast.LENGTH_SHORT).show();
                    return;
                }

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.putNull(DBHelper.COLUMN_OTHER_ID);
                values.put(DBHelper.COLUMN_OTHER_DATE, getCurrentDateTime());
                values.put(DBHelper.COLUMN_OTHER_TYPE, TypeOther);
                values.put(DBHelper.COLUMN_OTHER_AMOUNT, amount);
                values.put(DBHelper.COLUMN_OTHER_VEHICLE_ID, selectedVehicleId);

                long newRowId = db.insertWithOnConflict(DBHelper.TABLE_OTHER, null, values, SQLiteDatabase.CONFLICT_REPLACE);

                db.close();

                if (newRowId != -1) {
                    Toast.makeText(requireContext(), "Запись успешно добавлена", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка при добавлении записи", Toast.LENGTH_SHORT).show();
                }

                timeAndDateOther.setText(getCurrentDateTime());
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
    private long getSelectedVehicleId() {
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("selectedVehicleId")) {
            return arguments.getLong("selectedVehicleId");
        } else {
            // Если идентификатор не найден в аргументах, верните последний добавленный идентификатор
            return lastVehicleId;
        }
    }

}
