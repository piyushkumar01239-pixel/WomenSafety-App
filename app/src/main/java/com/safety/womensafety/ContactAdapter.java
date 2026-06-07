package com.safety.womensafety;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private Context context;
    private List<Contact> contacts;
    private OnContactDeleteListener deleteListener;

    public interface OnContactDeleteListener { void onDelete(Contact contact); }

    public ContactAdapter(Context context, List<Contact> contacts) {
        super(context, R.layout.contact_list_item, contacts);
        this.context = context; this.contacts = contacts;
    }

    public void setOnContactDeleteListener(OnContactDeleteListener l) { this.deleteListener = l; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
        Contact c = contacts.get(position);
        TextView tvName = convertView.findViewById(R.id.contactNameText);
        TextView tvPhone = convertView.findViewById(R.id.contactPhoneText);
        TextView tvRelation = convertView.findViewById(R.id.contactRelationText);
        TextView tvEmail = convertView.findViewById(R.id.contactEmailText);
        Button btnDelete = convertView.findViewById(R.id.deleteContactButton);
        tvName.setText(c.getName());
        tvPhone.setText("📞 " + c.getPhone());
        tvRelation.setText(c.getRelationship() != null ? c.getRelationship() : "");
        if (!TextUtils.isEmpty(c.getEmail())) {
            tvEmail.setVisibility(View.VISIBLE);
            tvEmail.setText("📧 " + c.getEmail());
        } else {
            tvEmail.setVisibility(View.GONE);
        }
        btnDelete.setOnClickListener(v -> { if (deleteListener != null) deleteListener.onDelete(c); });
        return convertView;
    }
}