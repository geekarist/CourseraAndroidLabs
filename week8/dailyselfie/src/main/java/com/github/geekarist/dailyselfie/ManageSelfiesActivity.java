package com.github.geekarist.dailyselfie;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;
import static java.lang.String.format;


public class ManageSelfiesActivity extends ListActivity {

    public static final File STORAGE_DIR = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DailySelfie");
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = ManageSelfiesActivity.class.getSimpleName();
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_selfies);

        // See http://stackoverflow.com/a/12329651/1665730
        Cursor mCursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                format("%s LIKE ?", MediaStore.Audio.Media.DATA),
                new String[]{"%DailySelfie%"}, MediaStore.MediaColumns.DATE_MODIFIED);
        startManagingCursor(mCursor);

        ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.two_line_list_item, mCursor,
                new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME, MediaStore.Images.ImageColumns.DATA},
                new int[]{R.id.text1, R.id.text2});
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor item = (Cursor) l.getItemAtPosition(position);
        String imgPath = item.getString(item.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
        Toast.makeText(this, imgPath, LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_selfies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_take_picture) {
            takePicture();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File pictureFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (IOException e) {
                Toast.makeText(this, R.string.error_taking_picture, LENGTH_LONG).show();
                Log.e(TAG, getString(R.string.error_taking_picture), e);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        if (STORAGE_DIR.mkdirs() || STORAGE_DIR.isDirectory()) {
            File image = File.createTempFile(imageFileName, ".jpg", STORAGE_DIR);
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }

        return null;
    }

    private void galleryAddPic() {
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        sendBroadcast(mediaScanIntent);
    }

}
