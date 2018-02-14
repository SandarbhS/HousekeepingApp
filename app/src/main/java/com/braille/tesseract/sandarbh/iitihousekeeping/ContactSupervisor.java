package com.braille.tesseract.sandarbh.iitihousekeeping;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import static com.braille.tesseract.sandarbh.iitihousekeeping.Login.toolbar;

public class ContactSupervisor extends AppCompatActivity {

    private RecyclerView contactList;
    private contactsAdapter contactsAdapter;
    private int increment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_supervisor);

        contactList = findViewById(R.id.contactList);
        contactsAdapter = new contactsAdapter();
        contactList.setAdapter(contactsAdapter);
        contactList.setLayoutManager(new LinearLayoutManager(this));

        initActionBar();
        //initDrawer();
    }

    public void initActionBar(){
        toolbar = findViewById(R.id.actionBar);
        TextView title,subtitle;

        title = findViewById(R.id.toolbar_title);
        title.setTextColor(getResources().getColor(R.color.titleColor));

        subtitle = findViewById(R.id.toolbar_subtitle);
        subtitle.setTextColor(getResources().getColor(R.color.titleColor));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.up_icon);
    }

    public class contactsAdapter extends RecyclerView.Adapter<contactsAdapter.Holder>{

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view =  getLayoutInflater().inflate(R.layout.contacts,parent,false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(final Holder holder, int position) {

            position *= 3;
            holder.name.setText(getResources().getStringArray(R.array.contacts_list)[position]);
            holder.position.setText(getResources().getStringArray(R.array.contacts_list)[position+1]);
            holder.contact.setText(getResources().getStringArray(R.array.contacts_list)[position+2]);

            holder.contactIcon.setBackgroundColor(getResources().getColor(R.color.PendingRequest));
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent caller = new Intent(Intent.ACTION_DIAL);
                    caller.setData(Uri.parse("tel:"+holder.contact.getText().toString()));
                    startActivity(caller);
                }
            });
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        public class Holder extends RecyclerView.ViewHolder{

            private TextView name,position,contact,contactIcon;
            private LinearLayout item;

            public Holder(View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                position = itemView.findViewById(R.id.position);
                contact = itemView.findViewById(R.id.contact);
                item = itemView.findViewById(R.id.contact_item);
                contactIcon = itemView.findViewById(R.id.contact_icon);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
