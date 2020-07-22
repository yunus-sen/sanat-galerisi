package com.yunussen.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText,painterNameText,yearText;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView=findViewById(R.id.imageView);
        artNameText=findViewById(R.id.artNameText3);
        painterNameText=findViewById(R.id.painterNameText2);
        yearText=findViewById(R.id.yearText);
        button=findViewById(R.id.button);

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        if(info.matches("new")){
                artNameText.setText("");
                painterNameText.setText("");
                yearText.setText("");
        }else{
            int id=intent.getIntExtra("id",1);
            button.setVisibility(View.INVISIBLE);
            try {
                SQLiteDatabase db=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
                Cursor cursor=db.rawQuery("SELECT * FROM arts WHERE id= ?",new String[]{String.valueOf(id)});
                int idIndex=cursor.getColumnIndex("id");
                int artNameIndex=cursor.getColumnIndex("artName");
                int yearIndex=cursor.getColumnIndex("year");
                int imageIndex=cursor.getColumnIndex("image");
                int painterNameIndex=cursor.getColumnIndex("painterName");
                while (cursor.moveToNext()){
                  artNameText.setText(cursor.getString(artNameIndex));
                  painterNameText.setText(cursor.getString(painterNameIndex));
                  yearText.setText(cursor.getString(yearIndex));
                  byte[] bytes=cursor.getBlob(imageIndex);

                  Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                  imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    public void selectImage(View v){

        //izin verilmediyse.
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intentToGalery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGalery,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Intent intentToGalery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGalery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2 && resultCode==RESULT_OK&&data!=null){
            Uri imageData=data.getData();
            try {
                if(Build.VERSION.SDK_INT>=28){
                    ImageDecoder.Source source=ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage=ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }else{
                    selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View v){
        String artName=artNameText.getText().toString();
        String painterName=painterNameText.getText().toString();
        String year=yearText.getText().toString();

        Bitmap smallImage=makeSmallImage(selectedImage,300);
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray();

        try {
            SQLiteDatabase db=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,artName VARCHAR,painterName VARCHAR,year VARCHAR ,image BLOB)");

            String sqlString="INSERT INTO arts (artName,painterName,year,image)VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement=db.compileStatement(sqlString);
            sqLiteStatement.bindString(1,artName);
            sqLiteStatement.bindString(2,painterName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();


        }catch (Exception e){
            e.printStackTrace();
        }
        //finish();
        Intent intent=new Intent(MainActivity2.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);



    }

    public Bitmap makeSmallImage(Bitmap image, int i) {
        int height=image.getHeight();
        int width=image.getWidth();

        float bitmapRatio=(float) width/(float)height;

        if(bitmapRatio>1){
            width=i;
            height=(int)(width/bitmapRatio);
        }else{
            height=i;
            width=(int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}