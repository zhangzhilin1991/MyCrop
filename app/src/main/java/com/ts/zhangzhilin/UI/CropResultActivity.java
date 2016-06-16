package com.ts.zhangzhilin.UI;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.ts.zhangzhilin.mycrop.R;

/**
 * Created by zhangzhilin on 6/15/16.
 */
public class CropResultActivity extends AppCompatActivity {

        private static final String TAG = "CropResultActivity";
        private String mImageName="裁剪结果：";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_result);

            ((ImageView) findViewById(R.id.image_view_preview)).setImageURI(getIntent().getData());

//            final BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(new File(getIntent().getData().getPath()).getAbsolutePath(), options);
             String mTempName=getIntent().getData().getLastPathSegment();
            if (mTempName!=null){
                mImageName=mTempName;
            }

            setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle(mImageName);
            }
        }

        @Override
        public boolean onCreateOptionsMenu(final Menu menu) {
            //getMenuInflater().inflate(R.menu.menu_result, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
            return super.onOptionsItemSelected(item);
        }

    }