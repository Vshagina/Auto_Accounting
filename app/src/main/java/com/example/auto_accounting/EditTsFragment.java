package com.example.auto_accounting;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

public class EditTsFragment extends Fragment {

    private static final String ARG_VEHICLE_ID = "vehicleId";
    private static final String ARG_TRANSPORT_TYPE = "transportType";

    private static final String ARG_BRAND = "brand";
    private static final String ARG_MODEL = "model";
    private static final String ARG_VEHICLES_FUEL_TYPE = "vehicles_fuel_type";
    private static final String ARG_YEAR = "year";

    private long vehicleId;
    private String brand;
    private String model;
    private String year;
    private String transportType;
    private String vehicles_fuel_type;

    private DBHelper dbHelper;

    public EditTsFragment() {
        // Required empty public constructor
    }

    public static EditTsFragment newInstance(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type, String year) {
        EditTsFragment fragment = new EditTsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_VEHICLE_ID, vehicleId);
        args.putString(ARG_TRANSPORT_TYPE, transportType);
        args.putString(ARG_BRAND, brand);
        args.putString(ARG_MODEL, model);
        args.putString(ARG_VEHICLES_FUEL_TYPE, vehicles_fuel_type);
        args.putString(ARG_YEAR, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vehicleId = getArguments().getLong(ARG_VEHICLE_ID);
            transportType = getArguments().getString(ARG_TRANSPORT_TYPE);
            brand = getArguments().getString(ARG_BRAND);
            model = getArguments().getString(ARG_MODEL);
            vehicles_fuel_type = getArguments().getString(ARG_VEHICLES_FUEL_TYPE);
            year = getArguments().getString(ARG_YEAR);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_ts, container, false);
        dbHelper = new DBHelper(requireContext());

        AutoCompleteTextView editTypeTS = view.findViewById(R.id.editTypeTS);
        AutoCompleteTextView editBrand = view.findViewById(R.id.editBrand);
        AutoCompleteTextView editModel = view.findViewById(R.id.editModel);
        AutoCompleteTextView editFuel = view.findViewById(R.id.editFuel);
        AutoCompleteTextView editYear = view.findViewById(R.id.editYear);

        Button editAddCar = view.findViewById(R.id.editAddCar);
        Button deleteAddCar = view.findViewById(R.id.deleteAddCar);

        if (getArguments() != null) {
            vehicleId = getArguments().getLong(ARG_VEHICLE_ID);
            transportType = getArguments().getString(ARG_TRANSPORT_TYPE);
            brand = getArguments().getString(ARG_BRAND);
            model = getArguments().getString(ARG_MODEL);
            vehicles_fuel_type = getArguments().getString(ARG_VEHICLES_FUEL_TYPE);
            year = getArguments().getString(ARG_YEAR);

            // Установка значения в AutoCompleteTextView
            editTypeTS.setText(transportType);
            editBrand.setText(brand);
            editModel.setText(model);
            editFuel.setText(vehicles_fuel_type);
            editYear.setText(year);
        }

        editAddCar.setOnClickListener(v -> {
            String newTransportType = editTypeTS.getText().toString();
            String newBrand = editBrand.getText().toString();
            String newModel = editModel.getText().toString();
            String newVehiclesFuelType = editFuel.getText().toString();
            String newYear = editYear.getText().toString();

            // Вызов метода updateVehicleData для обновления данных
            dbHelper.updateVehicleData(vehicleId, newTransportType, newBrand, newModel, newVehiclesFuelType, newYear);

            // Открытие CustomFragment с обновлёнными данными
            openCustomFragment(vehicleId, newTransportType,  newBrand, newModel,newVehiclesFuelType, newYear);

            // Обновление бокового меню
            updateSideMenu();
        });

        deleteAddCar.setOnClickListener(v -> {
            // Удалить выбранное транспортное средство
            dbHelper.deleteVehicle(vehicleId);

            // Получить данные о последнем добавленном транспортном средстве
            long lastInsertedVehicleId = dbHelper.getLastInsertedVehicleId();
            if (lastInsertedVehicleId > 0) {
                // Если есть последнее добавленное ТС, открыть CustomFragment с его данными
                dbHelper.getVehicleData(lastInsertedVehicleId, new DBHelper.VehicleDataCallback() {
                    @Override
                    public void onSuccess(String transportType, String brand, String model, String vehicles_fuel_type, String year) {
                        openCustomFragment(lastInsertedVehicleId, transportType, brand, model, vehicles_fuel_type, year);
                    }

                    @Override
                    public void onFailure(String error) {
                        // Обработка ошибки получения данных
                        Toast.makeText(requireContext(), "Ошибка", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Если нет последнего добавленного ТС, перейти в AddTsFragment
                openAddTsFragment();

                Toast.makeText(requireContext(), "Добавьте ТС!", Toast.LENGTH_SHORT).show();
            }

            // Обновление бокового меню
            updateSideMenu();
        });

        return view;
    }

    private void openAddTsFragment() {
        AddTsFragment addTsFragment = new AddTsFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, addTsFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openCustomFragment(long vehicleId, String transportType, String brand, String model, String vehicles_fuel_type,String year) {
        // Передача данных в CustomFragment
        CustomFragment customFragment = CustomFragment.newInstance(vehicleId, transportType, brand, model, vehicles_fuel_type, year);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, customFragment)
                .addToBackStack(null)
                .commit();
    }
    private void updateSideMenu() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateTransportMenuInNavigationView();
        } else {

        }
    }
}
