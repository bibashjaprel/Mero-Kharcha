package com.example.merokharcha.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.merokharcha.R;
import com.example.merokharcha.database.DBHelper;
import com.example.merokharcha.models.Expense;
import com.example.merokharcha.utils.CurrencyUtils;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private LinearLayout layoutCategoryStats;
    private Button btnBack;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new DBHelper(this);
        layoutCategoryStats = findViewById(R.id.layoutCategoryStats);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        generateReport();
    }

    private void generateReport() {
        String[] categories = {"Food", "Transport", "Rent", "Data", "Other"};
        List<Expense> allExpenses = dbHelper.getAllExpenses();
        
        double grandTotal = 0;
        for (Expense e : allExpenses) {
            grandTotal += e.getAmount();
        }

        if (grandTotal == 0) return;

        LayoutInflater inflater = LayoutInflater.from(this);

        for (String category : categories) {
            double catTotal = dbHelper.getCategoryTotal(category);
            if (catTotal > 0) {
                View catView = inflater.inflate(R.layout.item_category_report, layoutCategoryStats, false);
                
                TextView tvName = catView.findViewById(R.id.tvCatName);
                TextView tvAmount = catView.findViewById(R.id.tvCatAmount);
                ProgressBar pbProgress = catView.findViewById(R.id.pbCatProgress);

                tvName.setText(category);
                tvAmount.setText(CurrencyUtils.formatCurrency(catTotal));
                
                int percentage = (int) ((catTotal / grandTotal) * 100);
                pbProgress.setProgress(percentage);

                layoutCategoryStats.addView(catView);
            }
        }
    }
}