package com.example.sartarosh.profil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sartarosh.MainActivity;
import com.example.sartarosh.R;
import com.example.sartarosh.customer.CustomerActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText editPhone, editPassword;
    Button btnRegister, btnLogin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        editPhone = findViewById(R.id.edit_phone);
        editPassword = findViewById(R.id.edit_password);
        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);

        btnRegister.setOnClickListener(v -> {
            String phone = editPhone.getText().toString();
            String password = editPassword.getText().toString();
            registerWithPhone(phone, password);
        });

        btnLogin.setOnClickListener(v -> {
            String phone = editPhone.getText().toString();
            String password = editPassword.getText().toString();
            loginWithPhone(phone, password);
        });



    }


    public void loginWithPhone(String phone, String password) {
        String email = phone + "@barberapp.uz";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("demo7", "CustomerActivity 4");
                        startActivity(new Intent(this, CustomerActivity.class));
                        Toast.makeText(this, "Tizimga kirdingiz!", Toast.LENGTH_SHORT).show();
                        // Профил ёки бош меню
                    } else {
                        Toast.makeText(this, "Login xatolik: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void registerWithPhone(String phone, String password) {
        String email = phone + "@barberapp.uz"; // Телефонни "email" сифатида тайёрлаш

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("demo7", "BarberActivity 5");
                        startActivity(new Intent(this, BarberActivity.class));
                        Toast.makeText(this, "Ro'yxatdan o'tdingiz!", Toast.LENGTH_SHORT).show();
                        // Профил яратиш ёки Dashboard га ўтиш
                    } else {
                        Toast.makeText(this, "Xatolik:1 " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}