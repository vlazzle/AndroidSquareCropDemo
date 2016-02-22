package com.example.vlad.matrix;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private ImageView mLandscapeCropped;
    private ImageView mPortraitCropped;
    private ImageView mLandscapeUncropped;
    private ImageView mPortraitUncropped;

    private ImageCallback mLandscapeCallback;
    private ImageCallback mPortraitCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLandscapeCropped = (ImageView) findViewById(R.id.landscape_cropped);
        mPortraitCropped = (ImageView) findViewById(R.id.portrait_cropped);
        mLandscapeUncropped = (ImageView) findViewById(R.id.landscape_uncropped);
        mPortraitUncropped = (ImageView) findViewById(R.id.portrait_uncropped);

        // Need to keep strong references so these stay around as long as the activity.
        mLandscapeCallback = new ImageCallback(mLandscapeCropped, mLandscapeUncropped);
        mPortraitCallback = new ImageCallback(mPortraitCropped, mPortraitUncropped);
        new DrawableLoaderTask(mLandscapeCallback).execute(R.drawable.landscape_landscape);
        new DrawableLoaderTask(mPortraitCallback).execute(R.drawable.cat_portrait);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleVisibility(mLandscapeUncropped);
                toggleVisibility(mPortraitUncropped);
            }
        });
    }

    private static void toggleVisibility(@NonNull View view) {
        final int visibility = view.isShown() ? View.GONE : View.VISIBLE;
        view.setVisibility(visibility);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    private static class DrawableLoaderTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageCallback> mImageCallbackRef;

        DrawableLoaderTask(@NonNull ImageCallback imageCallback) {
            mImageCallbackRef = new WeakReference<>(imageCallback);
        }

        @Nullable
        @Override
        protected Bitmap doInBackground(@NonNull @DrawableRes Integer... params) {
            final ImageCallback imageCallback = mImageCallbackRef.get();
            if (imageCallback != null) {
                final Resources resources = imageCallback.getResources();
                final Integer drawableId = params[0];
                return BitmapFactory.decodeResource(resources, drawableId);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable Bitmap bm) {
            if (bm == null) {
                return;
            }
            final ImageCallback imageCallback = mImageCallbackRef.get();
            if (imageCallback != null) {
                imageCallback.setImage(bm);
            }
        }
    }

    private class ImageCallback {
        @NonNull private final ImageView mCroppedImageView;
        @NonNull private final ImageView mUncroppedImageView;

        protected ImageCallback(@NonNull ImageView croppedImageView, @NonNull ImageView uncroppedImageView) {
            mCroppedImageView = croppedImageView;
            mUncroppedImageView = uncroppedImageView;
        }

        Resources getResources() {
            return MainActivity.this.getResources();
        }

        void setImage(@NonNull final Bitmap bm) {
            mCroppedImageView.post(new Runnable() {
                @Override
                public void run() {
                    final int viewDimension = mCroppedImageView.getWidth();
                    if (viewDimension != 0) {
                        final Matrix m = getMatrix(bm, viewDimension);
                        mCroppedImageView.setImageMatrix(m);
                        mCroppedImageView.setImageBitmap(bm);
                    }
                }
            });

            mUncroppedImageView.setImageBitmap(bm);
        }

        @NonNull
        private Matrix getMatrix(@NonNull Bitmap bm, int targetDimension) {
            final int dwidth = bm.getWidth();
            final int dheight = bm.getHeight();

            /** @see ImageView #configureBounds */

            float scale;
            float dx = 0, dy = 0;
            if (dwidth * targetDimension > targetDimension * dheight) {
                scale = (float) targetDimension / (float) dheight;
                dx = (targetDimension - dwidth * scale) * 0.5f;
            } else {
                scale = (float) targetDimension / (float) dwidth;
                dy = (targetDimension - dheight * scale) * 0.5f;
            }

            final Matrix m = new Matrix();
            m.setScale(scale, scale);
            m.postTranslate(Math.round(dx), Math.round(dy));
            return m;
        }
    }
}
