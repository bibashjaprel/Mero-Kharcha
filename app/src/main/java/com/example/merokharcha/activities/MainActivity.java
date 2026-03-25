package com.example.merokharcha.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.merokharcha.R;
import com.example.merokharcha.adapters.ExpenseAdapter;
import com.example.merokharcha.database.DBHelper;
import com.example.merokharcha.models.Expense;
import com.example.merokharcha.utils.CurrencyUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalExpense, tvBudget, tvRemaining;
    private ProgressBar pbBudget;
    private RecyclerView rvExpenses;
    private DBHelper dbHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        preferences = getSharedPreferences("MeroKharchaPrefs", MODE_PRIVATE);

        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ImageButton btnProfile = findViewById(R.id.btnProfile);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBudget = findViewById(R.id.tvBudget);
        tvRemaining = findViewById(R.id.tvRemaining);
        pbBudget = findViewById(R.id.pbBudget);
        LinearLayout layoutBudget = findViewById(R.id.layoutBudget);
        rvExpenses = findViewById(R.id.rvExpenses);

        if (rvExpenses != null) {
            rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            });
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        if (layoutBudget != null) {
            layoutBudget.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        try {
            List<Expense> list = dbHelper.getAllExpenses();
            double total = 0;
            for (Expense e : list) {
                total += e.getAmount();
            }
            
            if (tvTotalExpense != null)
                tvTotalExpense.setText(CurrencyUtils.formatCurrency(total));
            
            float budget = preferences.getFloat("monthly_budget", 0);
            if (tvBudget != null)
                tvBudget.setText(CurrencyUtils.formatCurrency((double) budget));
            
            double remaining = budget - total;
            if (tvRemaining != null) {
                tvRemaining.setText(CurrencyUtils.formatCurrency(remaining));
                
                int colorRes = (remaining < 0) ? R.color.expense_red : R.color.white;
                tvRemaining.setTextColor(ContextCompat.getColor(this, colorRes));
            }
            
            if (pbBudget != null) {
                if (budget > 0) {
                    int progress = (int) ((total / budget) * 100);
                    pbBudget.setProgress(Math.min(progress, 100));
                } else {
                    pbBudget.setProgress(0);
                }
            }

            if (rvExpenses != null) {
                ExpenseAdapter adapter = new ExpenseAdapter(list, this);
                rvExpenses.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}