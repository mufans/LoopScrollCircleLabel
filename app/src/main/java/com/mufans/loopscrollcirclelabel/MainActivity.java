package com.mufans.loopscrollcirclelabel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LooperScrollContainer looperScrollContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        looperScrollContainer = (LooperScrollContainer) findViewById(R.id.looperSrollContainer);
        CircleItem circleItem1 = new CircleItem("circle1",R.drawable.circle);
        CircleItem circleItem2 = new CircleItem("circle2",R.drawable.circle1);
        CircleItem circleItem3 = new CircleItem("circle3",R.drawable.circle2);
        CircleItem circleItem4 = new CircleItem("circle4",R.drawable.circle3);
      //  CircleItem circleItem5 = new CircleItem("circle5",R.drawable.circle4);
      //  CircleItem circleItem6 = new CircleItem("circle6",R.drawable.circle);
      //  CircleItem circleItem7 = new CircleItem("circle7",R.drawable.circle1);
        List<CircleItem> list = new ArrayList<>();
        list.add(circleItem1);
        list.add(circleItem2);
        list.add(circleItem3);
        list.add(circleItem4);
      //  list.add(circleItem5);
      //  list.add(circleItem6);
      //  list.add(circleItem7);
        CircleAdapter circleAdapter = new CircleAdapter(this, list);
        looperScrollContainer.setAdapter(circleAdapter);
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
}
