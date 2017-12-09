package by.epam.androiddrawing.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import by.epam.androiddrawing.R;
import by.epam.androiddrawing.graph.CustomGraph;

public class MainActivity extends AppCompatActivity {

    //TextView tw;
    CustomGraph customGraph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customGraph = (CustomGraph) findViewById(R.id.customGraph);

        float[] x = new float[100];
        float[] y = new float[100];
        for(int i=0;i<100;i++){
            x[i]=i/10f;
            y[i]=(float)(10*Math.sin(x[i]));
        }
        customGraph.setData(x,y);
    }
}
