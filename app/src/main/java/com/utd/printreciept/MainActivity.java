package com.utd.printreciept;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button PrintBtn;
    EditText PrintInput;
    IposPrinter iposPrinter;
    Context context;
    Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        iposPrinter = new IposPrinter(getApplicationContext(), activity);

        iposPrinter.InitPrinter();

        context = this;
        PrintBtn = findViewById(R.id.printbtn);
        PrintInput = findViewById(R.id.printinput);

        PrintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PrintInput.length() > 0)
                    iposPrinter.IntentPrint(PrintInput.getText().toString() + "\n \n");
                else
                    Toast.makeText(getApplicationContext(), "Please enter text first", Toast.LENGTH_LONG).show();

            }
        });
    }
}


