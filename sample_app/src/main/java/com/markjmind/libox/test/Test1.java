package com.markjmind.libox.test;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.markjmind.libox.R;
import com.markjmind.uni.LoadViewListener;
import com.markjmind.uni.UpdateEvent;
import com.markjmind.uni.Viewer;
import com.markjmind.uni.ViewerBuilder;
import com.markjmind.uni.annotiation.Layout;

@Layout(R.layout.test1)
public class Test1 extends Viewer {

    TextView textView1;

    @Override
    public void onBind(int requestCode, ViewerBuilder build) {
        build.setAsync(true)
             .setPreLayout(true)
             .setLoadLayout(R.layout.progress, new LoadViewListener() {
                 @Override
                 public void loadCreate(int requestCode, View loadView) {
                     ((TextView)loadView.findViewById(R.id.progress_text)).setText("");
                 }

                 @Override
                 public void loadUpdate(int requestCode, View loadView, Object value) {
                     ((ProgressBar)loadView.findViewById(R.id.progress)).setProgress((int) value);
                     ((TextView)loadView.findViewById(R.id.progress_text)).setText((int)value+"/100%");
                 }

                 @Override
                 public void loadDestroy(int requestCode) {

                 }

             })
             .addParam("p2", "내부");
    }

    @Override
    public void onPre(int requestCode) {
        textView1 = (TextView)findViewById(R.id.textView1);
        textView1.setText("Loading..");
    }

    @Override
    public boolean onLoad(int requestCode, UpdateEvent event) {
        for(int i=0;i<=100;i++) {
            try {
                event.update(i);
                Thread.sleep(20);
            } catch (InterruptedException e) {}
        }
        return true;
    }

    @Override
    public void onCancelled(int requestCode) {

    }

    @Override
    public void onPost(int requestCode) {
        textView1.setText("p1:" + getParamString("p1") + "\np2:" + getParamString("p2"));
    }

    @Override
    public void onFail(int requestCode, Exception e) {
        Toast.makeText(getContext(), "실패", Toast.LENGTH_LONG).show();
    }
}
