package com.github.geekarist.modernartuserinterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
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
        setupChangeColorsSeekbar();
    }

    private void setupChangeColorsSeekbar() {
        SeekBar changeColors = (SeekBar) findViewById(R.id.change_colors);
        changeColors.setMax(360);
        final int leftTopRectOriginalColor = viewBackgroundColor(R.id.soft_rectangle_left_top);
        final int leftBottomRectOriginalColor = viewBackgroundColor(R.id.soft_rectangle_left_bottom);
        final int rightTopRectOriginalColor = viewBackgroundColor(R.id.heavy_rectangle_right_top);
        final int rightBottomRectOriginalColor = viewBackgroundColor(R.id.heavy_rectangle_right_bottom);

        changeColors.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        findViewById(R.id.soft_rectangle_left_top).setBackgroundColor(shiftColor(leftTopRectOriginalColor, progress));
                        findViewById(R.id.soft_rectangle_left_bottom).setBackgroundColor(shiftColor(leftBottomRectOriginalColor, progress));
                        findViewById(R.id.heavy_rectangle_right_top).setBackgroundColor(shiftColor(rightTopRectOriginalColor, progress));
                        findViewById(R.id.heavy_rectangle_right_bottom).setBackgroundColor(shiftColor(rightBottomRectOriginalColor, progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
    }

    private int shiftColor(int color, int offset) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = (hsv[0] + offset) % 360;
        return Color.HSVToColor(hsv);
    }

    private int viewBackgroundColor(int soft_rectangle_left_top) {
        return ((ColorDrawable) findViewById(soft_rectangle_left_top).getBackground()).getColor();
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
            showVisitMomaDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showVisitMomaDialog() {
        new DialogFragment() {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.visit_moma_question)
                        .setPositiveButton(R.string.visit_moma_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getBaseContext(), R.string.visit_moma_yes, Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(R.string.visit_moma_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getBaseContext(), R.string.visit_moma_no, Toast.LENGTH_LONG).show();
                            }
                        });
                return builder.create();
            }
        }.show(getFragmentManager(), "ViewRectanglesActivity");
    }
}
