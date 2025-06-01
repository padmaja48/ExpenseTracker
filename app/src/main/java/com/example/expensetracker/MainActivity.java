package com.example.expensetracker;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextCategory, editTextAmount, editTextBudget;
    Button buttonAddExpense, buttonSetBudget, buttonViewAll;
    ListView listView;
    ExpenseDatabaseHelper dbHelper;
    UserDatabaseHelper userDbHelper;
    int userId;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userId = getIntent().getIntExtra("userId", -1);

        dbHelper = new ExpenseDatabaseHelper(this);
        userDbHelper = new UserDatabaseHelper(this);

        editTextCategory = findViewById(R.id.editTextCategory);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextBudget = findViewById(R.id.editTextBudget);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonSetBudget = findViewById(R.id.buttonSetBudget);
        buttonViewAll = findViewById(R.id.buttonViewAll); // New Button
        listView = findViewById(R.id.listViewExpenses);

        buttonAddExpense.setOnClickListener(v -> {
            String category = editTextCategory.getText().toString().trim();
            String amtText = editTextAmount.getText().toString().trim();

            if (category.isEmpty() || amtText.isEmpty()) {
                Toast.makeText(this, "Enter category and amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amtText);
            dbHelper.insertExpense(category, amount);

            double totalSpent = getTotalExpenses();
            double budget = userDbHelper.getBudget(userId);

            if (budget > 0 && totalSpent > budget) {
                Toast.makeText(this, "⚠️ Budget Exceeded! Limit: ₹" + budget, Toast.LENGTH_LONG).show();
            }

            updateExpenseList();
            editTextCategory.setText("");
            editTextAmount.setText("");
        });

        buttonSetBudget.setOnClickListener(v -> {
            String budgetText = editTextBudget.getText().toString().trim();
            if (!budgetText.isEmpty()) {
                double budget = Double.parseDouble(budgetText);
                userDbHelper.setBudget(userId, budget);
                Toast.makeText(this, "✅ Budget Set: ₹" + budget, Toast.LENGTH_SHORT).show();
                editTextBudget.setText("");
            }
        });

        // 🔹 View All Expenses Button Logic
        buttonViewAll.setOnClickListener(v -> {
            List<Expense> expenses = dbHelper.getAllExpenses();
            if (expenses.isEmpty()) {
                Toast.makeText(this, "No expenses found.", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder all = new StringBuilder();
            for (Expense e : expenses) {
                all.append("Category: ").append(e.getCategory())
                        .append("\nAmount: ₹").append(e.getAmount())
                        .append("\n\n");
            }

            new AlertDialog.Builder(this)
                    .setTitle("All Expenses")
                    .setMessage(all.toString())
                    .setPositiveButton("OK", null)
                    .show();
        });

        updateExpenseList();
    }

    private void updateExpenseList() {
        List<Expense> expenses = dbHelper.getAllExpenses();
        String[] expenseArray = new String[expenses.size()];
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            expenseArray[i] = e.getCategory() + " - ₹" + e.getAmount();
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseArray);
        listView.setAdapter(adapter);
    }

    private double getTotalExpenses() {
        double total = 0;
        for (Expense e : dbHelper.getAllExpenses()) {
            total += e.getAmount();
        }
        return total;
    }
}
