package com.example.taranood.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.taranood.MainActivity;
import com.example.taranood.R;
import com.example.taranood.utils.StorageHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AppSettingsFragment extends Fragment {

    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        saveDataToFile(result.getData().getData());
                    }
                }
        );

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        loadDataFromFile(result.getData().getData());
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_settings, container, false);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisibility(View.GONE);
        }

        ImageButton btnBack = view.findViewById(R.id.btn_back_settings);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        SwitchMaterial darkModeSwitch = view.findViewById(R.id.switch_dark_mode_page);
        darkModeSwitch.setChecked(StorageHelper.isDarkMode(getContext()));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StorageHelper.setDarkMode(getContext(), isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Watch Preferences
        TextView textCurrentDefaultType = view.findViewById(R.id.text_current_default_type);
        textCurrentDefaultType.setText(StorageHelper.getDefaultAddType(getContext()));

        view.findViewById(R.id.btn_default_add_type).setOnClickListener(v -> {
            String[] types = {"Movie", "Series", "Anime", "Other"};
            int currentSelection = 0;
            String currentType = StorageHelper.getDefaultAddType(getContext());
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(currentType)) {
                    currentSelection = i;
                    break;
                }
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Default Add Type")
                    .setSingleChoiceItems(types, currentSelection, (dialog, which) -> {
                        StorageHelper.setDefaultAddType(getContext(), types[which]);
                        textCurrentDefaultType.setText(types[which]);
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        TextView textCurrentLogCount = view.findViewById(R.id.text_current_log_count);
        textCurrentLogCount.setText(String.valueOf(StorageHelper.getLogPreviewCount(getContext())));

        view.findViewById(R.id.btn_log_preview_count).setOnClickListener(v -> {
            String[] counts = {"3", "5", "10", "15", "20"};
            int currentSelection = 0;
            int currentCount = StorageHelper.getLogPreviewCount(getContext());
            for (int i = 0; i < counts.length; i++) {
                if (Integer.parseInt(counts[i]) == currentCount) {
                    currentSelection = i;
                    break;
                }
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Log Preview Count")
                    .setSingleChoiceItems(counts, currentSelection, (dialog, which) -> {
                        int selectedCount = Integer.parseInt(counts[which]);
                        StorageHelper.setLogPreviewCount(getContext(), selectedCount);
                        textCurrentLogCount.setText(String.valueOf(selectedCount));
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        Button btnExport = view.findViewById(R.id.btn_export_data);
        btnExport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            intent.putExtra(Intent.EXTRA_TITLE, "taranood_backup.json");
            exportLauncher.launch(intent);
        });

        Button btnImport = view.findViewById(R.id.btn_import_data);
        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importLauncher.launch(intent);
        });

        Button btnClearData = view.findViewById(R.id.btn_clear_data_page);
        btnClearData.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure you want to delete everything? This cannot be undone.")
                    .setPositiveButton("Yes, Clear Everything", (dialog, which) -> {
                        StorageHelper.clearAllData(getContext());
                        Toast.makeText(getContext(), "Data cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void saveDataToFile(Uri uri) {
        try {
            String data = StorageHelper.exportAllData(requireContext());
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
                Toast.makeText(getContext(), "Backup saved successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to save backup", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDataFromFile(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                
                boolean success = StorageHelper.importAllData(requireContext(), stringBuilder.toString());
                if (success) {
                    Toast.makeText(getContext(), "Data restored successfully. Please restart app.", Toast.LENGTH_LONG).show();
                    if (getActivity() != null) getActivity().recreate();
                } else {
                    Toast.makeText(getContext(), "Failed to restore data", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error reading file", Toast.LENGTH_SHORT).show();
        }
    }
}
