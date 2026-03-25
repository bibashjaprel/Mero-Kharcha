package com.example.merokharcha.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.merokharcha.R;
import com.example.merokharcha.database.DBHelper;
import com.example.merokharcha.models.Expense;
import com.example.merokharcha.utils.CurrencyUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private TextView tvAmountDisplay, tvHeaderTitle, btnSelectCategory;
    private EditText etNote;
    private ImageButton btnClose, btnDelete;
    private Button btnDone, btnBackspace;
    private DBHelper dbHelper;
    private String currentAmount = "";
    private String selectedCategory = "Food";
    private int expenseId = -1;
    private String[] categories = {"Food", "Transport", "Rent", "Data", "Other"};
    private String[] categoryEmojis = {"🍔 Food", "🚲 Transport", "🏠 Rent", "📱 Data", "📦 Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        dbHelper = new DBHelper(this);

        tvAmountDisplay = findViewById(R.id.tvAmountDisplay);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnSelectCategory = findViewById(R.id.btnSelectCategory);
        etNote = findViewById(R.id.etNote);
        btnClose = findViewById(R.id.btnClose);
        btnDelete = findViewById(R.id.btnDelete);
        btnDone = findViewById(R.id.btnDone);
        btnBackspace = findViewById(R.id.btnBackspace);

        setupKeypad();

        if (btnSelectCategory != null) btnSelectCategory.setOnClickListener(v -> showCategoryDialog());
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());
        if (btnDone != null) btnDone.setOnClickListener(v -> saveExpense());
        if (btnBackspace != null) btnBackspace.setOnClickListener(v -> onBackspaceClick());

        // Check if we are editing an existing expense
        if (getIntent().hasExtra("expense_id")) {
            expenseId = getIntent().getIntExtra("expense_id", -1);
            double amount = getIntent().getDoubleExtra("amount", 0);
            currentAmount = String.format(Locale.getDefault(), "%.0f", amount);
            updateAmountDisplay();
            if (etNote != null) etNote.setText(getIntent().getStringExtra("note"));
            selectedCategory = getIntent().getStringExtra("category");
            updateCategoryDisplay();
            if (tvHeaderTitle != null) tvHeaderTitle.setText("Edit Expense");
            if (btnDelete != null) btnDelete.setVisibility(View.VISIBLE);
        }

        if (btnDelete != null) btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void setupKeypad() {
        // Find the GridLayout
        ViewGroup layout = findViewById(android.R.id.content);
        GridLayout keypad = findGridLayout(layout);
        
        if (keypad != null) {
            for (int i = 0; i < keypad.getChildCount(); i++) {
                View v = keypad.getChildAt(i);
                if (v instanceof Button) {
                    Button b = (Button) v;
                    // Skip backspace since it has its own listener
                    if (b.getId() != R.id.btnBackspace) {
                        b.setOnClickListener(this::onKeypadClick);
                    }
                }
            }
        }
    }

    private GridLayout findGridLayout(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof GridLayout) {
                return (GridLayout) child;
            } else if (child instanceof ViewGroup) {
                GridLayout found = findGridLayout((ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void onKeypadClick(View v) {
        if (!(v instanceof Button)) return;
        String key = ((Button) v).getText().toString();
        
        // Prevent multiple decimals
        if (key.equals(".") && currentAmount.contains(".")) return;
        
        // Limit amount length
        if (currentAmount.length() > 9) return;

        currentAmount += key;
        updateAmountDisplay();
    }

    private void onBackspaceClick() {
        if (currentAmount.length() > 0) {
            currentAmount = currentAmount.substring(0, currentAmount.length() - 1);
            updateAmountDisplay();
        }
    }

    private void updateAmountDisplay() {
        if (tvAmountDisplay != null) {
            if (currentAmount.isEmpty()) {
                tvAmountDisplay.setText("Rs. 0");
            } else {
                try {
                    double amt = Double.parseDouble(currentAmount);
                    tvAmountDisplay.setText(CurrencyUtils.formatCurrency(amt));
                } catch (Exception e) {
                    tvAmountDisplay.setText("Rs. " + currentAmount);
                }
            }
        }
    }

    private void showCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setItems(categoryEmojis, (dialog, which) -> {
            selectedCategory = categories[which];
            updateCategoryDisplay();
        });
        builder.show();
    }

    private void updateCategoryDisplay() {
        if (btnSelectCategory == null) return;
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(selectedCategory)) {
                btnSelectCategory.setText(categoryEmojis[i]);
                break;
            }
        }
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteExpense(expenseId);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveExpense() {
        if (currentAmount.isEmpty() || currentAmount.equals(".")) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(currentAmount);
            String note = etNote != null ? etNote.getText().toString() : "";
            
            // Format both Date and Time
            String dateTime = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(new Date());

            Expense expense = new Expense(amount, selectedCategory, dateTime, note);
            
            if (expenseId != -1) {
                expense.setId(expenseId);
                // Keep original date if editing, or update to current? 
                // Let's update to current for simplicity unless specified
                dbHelper.updateExpense(expense);
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addExpense(expense);
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}