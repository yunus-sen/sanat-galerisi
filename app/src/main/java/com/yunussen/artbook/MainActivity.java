package com.yunussen.artbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String >nameArray;
    ArrayList<Integer>idArray;
    ArrayAdapter arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);
        nameArray=new ArrayList<>();
        idArray=new ArrayList<>();

        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent =new Intent(MainActivity.this,MainActivity2.class);
                intent.putExtra("id",idArray.get(i));
                intent.putExtra("info","old");
                startActivity(intent);
            }
        });
        getData();
    }

    public void getData(){
        try {
            SQLiteDatabase db=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor=db.rawQuery("SELECT * FROM arts",null);
            int idIndex=cursor.getColumnIndex("id");
            int artNameIndex=cursor.getColumnIndex("artName");
            while (cursor.moveToNext()){
                nameArray.add(cursor.getString(artNameIndex));
                idArray.add(cursor.getInt(idIndex));
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_add,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_art_item){
            Intent intent=new Intent(MainActivity.this,MainActivity2.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}