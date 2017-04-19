package com.itheima.slidemenu99;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.menu_listview)
    ListView menuListview;
    @BindView(R.id.iv_head)
    ImageView ivHead;
    @BindView(R.id.main_listview)
    ListView mainListview;
    @BindView(R.id.slideMenu)
    SlideMenu slideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        menuListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                Constant.sCheeseStrings) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }
        });
        mainListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                Constant.NAMES));

        slideMenu.setOnSlideListener(new SlideMenu.OnSlideListener() {
            @Override
            public void onDragging(float percent) {
                Log.e(TAG, "onDragging: percent: " + percent);
                ivHead.setRotationY(percent * 720);
            }

            @Override
            public void onOpen() {
                Toast.makeText(MainActivity.this, "Open", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClose() {
                Toast.makeText(MainActivity.this, "Close", Toast.LENGTH_SHORT).show();
                ViewCompat.animate(ivHead).translationXBy(50)
                        .setDuration(800).setInterpolator(new CycleInterpolator(4))
                        .start();
            }
        });
    }

    @OnClick(R.id.iv_head)
    public void onViewClicked() {
        slideMenu.toogle();
    }
}
