package com.github.geekarist.dailyselfie;

import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;
import static java.lang.String.format;


public class ManageSelfiesActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final File STORAGE_DIR =
            new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                     "DailySelfie");

    public static final int REQUEST_CODE_ALARM_NOTIFICATION = 0;
    public static final int REQUEST_CODE_OPEN_APP = 1;
    public static final int REQUEST_CODE_IMAGE_CAPTURE = 2;
    private static final String TAG = ManageSelfiesActivity.class.getSimpleName();
    private static final int LOADER_ID = 0;

    private String mCurrentPhotoPath;
    private SimpleCursorAdapter mAdapter;

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /*
     * Load scaled down bitmap
     * See http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap
     */
    private static Bitmap decodeSampledBitmapFromFile(String imgPath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgPath, options);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.selfie_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
        // TODO: delete selfie
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_selfies);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        mAdapter = createAdapter(null);
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(createAlarmIntent());
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotifyService.NOTIFICATION_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long interval = AlarmManager.INTERVAL_HOUR / 360;
        Log.d(TAG, String.format("Will send a notification every %d ms", interval));
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                     SystemClock.elapsedRealtime() + interval,
                                     interval,
                                     createAlarmIntent());
    }

    private PendingIntent createAlarmIntent() {
        Intent sendNotificationIntent = new Intent(getApplicationContext(), NotifyService.class);
        sendNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getService(getApplicationContext(), REQUEST_CODE_ALARM_NOTIFICATION, sendNotificationIntent, 0);
    }

    /**
     * Display bitmap using a subclass of SimpleCursorAdapter.
     * See http://stackoverflow.com/a/460927/1665730
     */
    private SimpleCursorAdapter createAdapter(final Cursor mCursor) {
        return new SimpleCursorAdapter(this, R.layout.two_line_list_item, mCursor,
                                       new String[]{
                                               MediaStore.Images.ImageColumns.DISPLAY_NAME,
                                               MediaStore.Images.ImageColumns.DATA},
                                       new int[]{R.id.text1, R.id.text2},
                                       CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Cursor cursor = getCursor();
                if (cursor != null) {
                    String imgPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                    ImageView itemImageView = (ImageView) view.findViewById(R.id.imageView);
                    itemImageView.setImageBitmap(decodeSampledBitmapFromFile(imgPath, 100, 100));
                }
                return view;
            }
        };
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Cursor item = (Cursor) l.getItemAtPosition(position);
        String imgPath = item.getString(item.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
        Intent imgViewIntent = new Intent();
        imgViewIntent.setAction(Intent.ACTION_VIEW);
        imgViewIntent.setDataAndType(Uri.fromFile(new File(imgPath)), "image/jpeg");
        startActivity(imgViewIntent);
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
        if (requestCode == REQUEST_CODE_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
        }
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File pictureFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(pictureFile));
                startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAPTURE);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                                        format("%s LIKE ?", MediaStore.Images.Media.DATA),
                                        new String[]{"%DailySelfie%"}, MediaStore.MediaColumns.DATE_MODIFIED);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }
}
