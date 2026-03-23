package com.example.merokharcha.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import com.example.merokharcha.R;
import com.example.merokharcha.database.DBHelper;
import com.example.merokharcha.models.Expense;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView ivProfilePic;
    private TextView tvUserName;
    private DBHelper dbHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("MeroKharchaPrefs", MODE_PRIVATE);

        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvUserName = findViewById(R.id.tvUserName);
        CardView cardUserInfo = findViewById(R.id.cardUserInfo);
        CardView btnReports = findViewById(R.id.btnViewReports);
        CardView btnExport = findViewById(R.id.btnExportCSV);
        CardView btnBudget = findViewById(R.id.btnSetBudget);

        loadUserData();

        cardUserInfo.setOnClickListener(v -> showEditUserDialog());
        btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        btnExport.setOnClickListener(v -> exportData());
        btnBudget.setOnClickListener(v -> showBudgetDialog());
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String name = preferences.getString("user_name", "Student User");
        tvUserName.setText(name);

        String imageBase64 = preferences.getString("profile_pic", "");
        if (!imageBase64.isEmpty()) {
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivProfilePic.setImageBitmap(decodedByte);
        }
    }

    private void showEditUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile");

        String[] options = {"Change Name", "Change Profile Picture"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showNameInputDialog();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name");
        final EditText input = new EditText(this);
        input.setText(tvUserName.getText());
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) {
                preferences.edit().putString("user_name", newName).apply();
                tvUserName.setText(newName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ivProfilePic.setImageBitmap(selectedImage);
                saveImageToPrefs(selectedImage);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToPrefs(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        preferences.edit().putString("profile_pic", encodedImage).apply();
    }

    private void exportData() {
        List<Expense> list = dbHelper.getAllExpenses();
        if (list.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvData = new StringBuilder();
        csvData.append("ID,Amount,Category,Date,Note\n");
        for (Expense e : list) {
            csvData.append(e.getId()).append(",")
                    .append(e.getAmount()).append(",")
                    .append(e.getCategory()).append(",")
                    .append(e.getDate()).append(",")
                    .append(e.getNote()).append("\n");
        }

        try {
            File file = new File(getCacheDir(), "expenses.csv");
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvData.toString().getBytes());
            out.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Export via:"));
        } catch (Exception e) {
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Monthly Budget");
        final EditText input = new EditText(this);
        input.setHint("Enter Amount (Rs.)");
        input.setPadding(50, 50, 50, 50);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String budgetStr = input.getText().toString();
            if (!budgetStr.isEmpty()) {
                preferences.edit().putFloat("monthly_budget", Float.parseFloat(budgetStr)).apply();
                Toast.makeText(this, "Budget Updated", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}