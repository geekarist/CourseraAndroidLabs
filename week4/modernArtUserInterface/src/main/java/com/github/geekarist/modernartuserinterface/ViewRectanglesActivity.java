package com.github.geekarist.modernartuserinterface;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;


public class ViewRectanglesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rectangles);

        SeekBar changeColors = (SeekBar) findViewById(R.id.change_colors);
        final int leftTopRectOriginalColor = ((ColorDrawable) findViewById(R.id.soft_rectangle_left_top).getBackground()).getColor();
        final int leftBottomRectOriginalColor = ((ColorDrawable) findViewById(R.id.soft_rectangle_left_bottom).getBackground()).getColor();
        final int rightTopRectOriginalColor = ((ColorDrawable) findViewById(R.id.heavy_rectangle_right_top).getBackground()).getColor();
        final int rightBottomRectOriginalColor = ((ColorDrawable) findViewById(R.id.heavy_rectangle_right_bottom).getBackground()).getColor();

        changeColors.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                findViewById(R.id.soft_rectangle_left_top).setBackgroundColor(leftTopRectOriginalColor + progress);
                findViewById(R.id.soft_rectangle_left_bottom).setBackgroundColor(leftBottomRectOriginalColor + progress);
                findViewById(R.id.heavy_rectangle_right_top).setBackgroundColor(rightTopRectOriginalColor + progress);
                findViewById(R.id.heavy_rectangle_right_bottom).setBackgroundColor(rightBottomRectOriginalColor + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_rectangles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_more_info) {
            Toast.makeText(getBaseContext(), "More informations clicked", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
