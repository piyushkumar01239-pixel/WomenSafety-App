package com.safety.womensafety;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private ListView lvContacts;
    private Button btnAddContact;
    private TextView tvNoContacts;
    private DatabaseHelper db;
    private int currentUserId;
    private List<Contact> contactList;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        db = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        lvContacts = findViewById(R.id.contactsListView);
        btnAddContact = findViewById(R.id.addContactButton);
        tvNoContacts = findViewById(R.id.emptyStateText);
        loadContacts();
        btnAddContact.setOnClickListener(v -> showAddContactDialog());
    }

    private void loadContacts() {
        contactList = db.getEmergencyContacts(currentUserId);
        if (contactList.isEmpty()) {
            tvNoContacts.setVisibility(View.VISIBLE);
            lvContacts.setVisibility(View.GONE);
        } else {
            tvNoContacts.setVisibility(View.GONE);
            lvContacts.setVisibility(View.VISIBLE);
            adapter = new ContactAdapter(this, contactList);
            adapter.setOnContactDeleteListener(contact -> showDeleteDialog(contact));
            lvContacts.setAdapter(adapter);
        }
    }

    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText etName = dialogView.findViewById(R.id.dialogContactName);
        EditText etPhone = dialogView.findViewById(R.id.dialogContactPhone);
        EditText etRelation = dialogView.findViewById(R.id.dialogContactRelation);
        EditText etEmail = dialogView.findViewById(R.id.dialogContactEmail);

        new AlertDialog.Builder(this)
                .setTitle("Add Emergency Contact")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String relation = etRelation.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) { Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show(); return; }
                    if (TextUtils.isEmpty(phone)) { Toast.makeText(this, "Phone is required", Toast.LENGTH_SHORT).show(); return; }
                    if (!TextUtils.isEmpty(email) && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show(); return;
                    }
                    long result = db.addEmergencyContact(currentUserId, name, phone, relation, email);
                    if (result != -1) {
                        String msg = "Contact added!" + (!TextUtils.isEmpty(email) ? " Email alerts enabled ✅" : "");
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        loadContacts();
                    } else {
                        Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Remove " + contact.getName() + " from emergency contacts?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (db.deleteContact(contact.getContactId())) {
                        Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
                        loadContacts();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
}