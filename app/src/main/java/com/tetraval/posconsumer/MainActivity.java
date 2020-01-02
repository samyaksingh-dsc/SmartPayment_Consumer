package com.tetraval.posconsumer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DatabaseReference posRef;
    EditText txtBal, txtUserID;
    Button btnAddBal, btnLogout;
    FirebaseAuth auth;
    ListView listView;
    String amount = "0";
    TextView txtCurrentBal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        txtBal = findViewById(R.id.txtBal);
        txtUserID = findViewById(R.id.txtUserID);
        btnAddBal = findViewById(R.id.btnAddBal);
        btnLogout = findViewById(R.id.btn_logout);
        listView = findViewById(R.id.listView);
        txtCurrentBal = findViewById(R.id.txtCurrentBal);

        posRef = FirebaseDatabase.getInstance().getReference("request");
        posRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                amount = dataSnapshot.child("amount").getValue().toString();
                txtCurrentBal.setText("₹"+amount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnAddBal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String balance_to_add = txtBal.getText().toString();
                if (TextUtils.isEmpty(balance_to_add)){
                    Toast.makeText(MainActivity.this, "Please enter balance amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                addBalance(balance_to_add);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        fetchTransactions();

    }

    private void addBalance(String balance_to_add){
        int current_balance = Integer.parseInt(amount);
        int add_balance = Integer.parseInt(balance_to_add);
        int updated_balance = current_balance+add_balance;
        posRef = FirebaseDatabase.getInstance().getReference("request");
        posRef.child("amount").setValue(updated_balance);
        posRef = FirebaseDatabase.getInstance().getReference("transactions");
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("amount", balance_to_add);
        hashMap.put("user", "--");
        hashMap.put("type", "1");
        posRef.push().setValue(hashMap);
        Toast.makeText(MainActivity.this, "Balance Added!", Toast.LENGTH_SHORT).show();
        txtBal.setText("");
    }

    private void fetchTransactions(){

        final ArrayList<String> txn = new ArrayList<>();
        final String[] txn_list = new String[1];
        posRef = FirebaseDatabase.getInstance().getReference("transactions");
        posRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txn.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    String user = dataSnapshot1.child("user").getValue().toString();
                    String amount = dataSnapshot1.child("amount").getValue().toString();
                    String type = dataSnapshot1.child("type").getValue().toString();
                    if (type.equals("1")){
                       txn_list[0] = "Rs. "+amount+" add to wallet";
                    }
                    if (type.equals("0")){
                        txn_list[0] = "Rs. "+amount+" paid to merchant.";
                    }
                    txn.add(txn_list[0]);
                }

                Context context;
                ArrayAdapter arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, txn);
                listView.setAdapter(arrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void signOut(){
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
