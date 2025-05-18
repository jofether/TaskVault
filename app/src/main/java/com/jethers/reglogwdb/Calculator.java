package com.jethers.reglogwdb;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import android.widget.ScrollView;

public class Calculator extends AppCompatActivity {

    private TextView display;
    private TextView historyText;
    private final StringBuilder currentInput = new StringBuilder();
    private final List<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        display = findViewById(R.id.display);
        historyText = findViewById(R.id.historyText);

        setButtonListeners();
    }

    private void setButtonListeners() {
        int[] numberButtons = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9};

        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(v -> onDigitPressed(((Button) v).getText().toString()));
        }

        findViewById(R.id.buttonPlus).setOnClickListener(v -> onOperatorPressed("+"));
        findViewById(R.id.buttonMinus).setOnClickListener(v -> onOperatorPressed("-"));
        findViewById(R.id.buttonMultiply).setOnClickListener(v -> onOperatorPressed("*"));
        findViewById(R.id.buttonDivide).setOnClickListener(v -> onOperatorPressed("/"));
        findViewById(R.id.buttonDot).setOnClickListener(v -> onDotPressed());
        findViewById(R.id.buttonEquals).setOnClickListener(v -> onEqualsPressed());
        findViewById(R.id.buttonClear).setOnClickListener(v -> onClearPressed());
        findViewById(R.id.buttonBackspace).setOnClickListener(v -> onBackspacePressed());
    }

    private void onDigitPressed(String digit) {
        currentInput.append(digit);
        updateDisplay();
    }

    private void onOperatorPressed(String operator) {
        currentInput.append(" ").append(operator).append(" ");
        updateDisplay();
    }

    private void onDotPressed() {
        String input = currentInput.toString();
        int lastOperatorIndex = Math.max(input.lastIndexOf("+"),
                Math.max(input.lastIndexOf("-"),
                        Math.max(input.lastIndexOf("*"), input.lastIndexOf("/"))));

        String lastNumber = input.substring(lastOperatorIndex + 1).trim();
        if (!lastNumber.contains(".")) {
            currentInput.append(".");
            updateDisplay();
        }
    }

    private void onClearPressed() {
        currentInput.setLength(0);
        display.setText("0");
        display.setTextSize(36);
    }

    private void onBackspacePressed() {
        int length = currentInput.length();
        if (length > 0) {
            currentInput.delete(length - 1, length);
            updateDisplay();
        }
    }

    @SuppressLint({"SetTextI18n", "SetTextI18n"})
    private void onEqualsPressed() {
        try {
            String result = SimpleParser.evaluateExpression(currentInput.toString());
            double numericResult = Double.parseDouble(result);

            // Check if the result is 143 (or equal when cast as a double)
            if (numericResult == 143) {
                display.setText("I miss you na");
            } else {
                display.setText(result);
            }

            // Add the result to the history (displaying "I miss you na" if result is 143)
            history.add(0, currentInput + " = " + (numericResult == 143 ? "I miss you na" : result));
            updateHistory();

            // Scroll the history to the top
            ScrollView historyScrollView = findViewById(R.id.historyScrollView);
            historyScrollView.post(() -> historyScrollView.scrollTo(0, 0));

            // Clear the current input after showing the result
            currentInput.setLength(0);
            display.setText("0");
            display.setTextSize(36);
        } catch (Exception e) {
            display.setText("Error");
            currentInput.setLength(0);
        }
    }

    private void updateDisplay() {
        display.setText(currentInput.toString());
        adjustDisplayTextSize(currentInput.length());
    }

    private void adjustDisplayTextSize(int length) {
        int textSize;
        if (length > 20) {
            textSize = 18;
        } else if (length > 15) {
            textSize = 24;
        } else if (length > 10) {
            textSize = 30;
        } else {
            textSize = 36;
        }
        display.setTextSize(textSize);
    }

    private void updateHistory() {
        StringBuilder historyDisplay = new StringBuilder();
        for (String entry : history) {
            historyDisplay.append(entry).append("\n");
        }
        historyText.setText(historyDisplay.toString());
    }
}
