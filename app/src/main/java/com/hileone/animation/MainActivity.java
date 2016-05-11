package com.hileone.animation;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hileone.animation.views.ShapeAnimationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ShapeAnimationView mShapeAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        mShapeAnimationView = (ShapeAnimationView) findViewById(R.id.shape_view);

        mShapeAnimationView.setInterval(30);
        mShapeAnimationView.setShapeName("smile_face.xml");
        mShapeAnimationView.setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.rose));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                mShapeAnimationView.display();
                break;
            case R.id.pause:
                if (mShapeAnimationView.isDisplayPausing()) {
                    mShapeAnimationView.resumeDisplay();
                } else {
                    mShapeAnimationView.pauseDislay();
                }
                break;
            case R.id.stop:
                mShapeAnimationView.stopDisplay();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shape_smileface:
                mShapeAnimationView.setShapeName("smile_face.xml");
                break;
            case R.id.shape_love:
                mShapeAnimationView.setShapeName("love.xml");
                break;
            case R.id.shape_doublestar:
                mShapeAnimationView.setShapeName("double_star.xml");
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
