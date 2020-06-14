package com.example.digital_signature;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
public class MainActivity extends AppCompatActivity {
    
    SignaturePad signaturePad;
    Button saveDraw, clearDraw;
    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN );
        
        setContentView( R.layout.activity_main );
        
        signaturePad = findViewById( R.id.signature_pad );
        saveDraw = findViewById( R.id.save );
        clearDraw = findViewById( R.id.delete );
        
        signaturePad.setOnSignedListener( new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
//                Toast.makeText( MainActivity.this, "onStartSigning", Toast.LENGTH_SHORT ).show();
            }

            @Override
            public void onSigned() {
                saveDraw.setEnabled( true );
                clearDraw.setEnabled( true );
            }

            @Override
            public void onClear() {
                saveDraw.setEnabled( false );
                clearDraw.setEnabled( false);
            }
        } );
        
        clearDraw.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signaturePad.clear();
            }
        } );
        
        saveDraw.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
                if (addJPGSignatureToGallery( signatureBitmap )){
                    Toast.makeText( MainActivity.this, "Signature saved into gallery", Toast.LENGTH_SHORT ).show();
                }else {
                    Toast.makeText( MainActivity.this, "Signature NOT saved into gallery", Toast.LENGTH_SHORT ).show();
                    
                }
            }
        } );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:{
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText( this, "Cannot write image to external storage", Toast.LENGTH_SHORT ).show();
                }
            }
        }
    }

    public File getAlbumStorageDir(String albumName){
        File file = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ), albumName );
        if (!file.mkdirs()){
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }
    
    public void saveBitmaptoJPG(Bitmap bitmap, File photo) throws Exception{
        Bitmap bitmap1 = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888 );
        Canvas canvas = new Canvas( bitmap1 );
        canvas.drawColor( Color.WHITE );
        canvas.drawBitmap( bitmap, 0, 0, null );
        OutputStream stream = new FileOutputStream( photo );
        bitmap1.compress( Bitmap.CompressFormat.JPEG, 80, stream );
        stream.close();
    }
    public boolean addJPGSignatureToGallery(Bitmap signature){
        boolean result = false;
        
        try {
            File photo = new File(getAlbumStorageDir( "SignaturePad" )
                    , String.format(System.currentTimeMillis() + "signature_id.jpg") );
            
            saveBitmaptoJPG( signature, photo );
            scanMediaFile( photo );
            result = true;
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }
    
    private void scanMediaFile(File photo){
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile( photo );
        mediaScanIntent.setData( contentUri );
        MainActivity.this.sendBroadcast( mediaScanIntent );
    }
}
