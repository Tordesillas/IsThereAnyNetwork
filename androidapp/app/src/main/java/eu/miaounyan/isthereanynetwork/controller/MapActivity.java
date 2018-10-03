package eu.miaounyan.isthereanynetwork.controller;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import java.util.LinkedList;
import java.util.List;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.model.SignalStrength;
import eu.miaounyan.isthereanynetwork.view.MapAdapter;

public class MapActivity extends Activity {
    private static final int NB_COLUMNS = 10;
    private static final int NB_ROWS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        /* Get colors */
        List<Integer> colors = new LinkedList<>();
        int color = 0;
        for (int i = 0; i < NB_COLUMNS*NB_ROWS; i++) {
            switch (i) {
                case 13:
                case 32:
                case 23:
                    colors.add(SignalStrength.LOW.getColor()); break;
                case 24:
                case 25:
                    colors.add(SignalStrength.MEDIUM.getColor()); break;
                case 26:
                case 27:
                case 36:
                case 37:
                case 28:
                    colors.add(SignalStrength.HIGH.getColor()); break;
                default:
                    colors.add(color);
            }
        }

        /* Grid creation */
        GridView grid = findViewById(R.id.grid);
        grid.setNumColumns(NB_COLUMNS);

        MapAdapter ma = new MapAdapter(this, colors);
        grid.setAdapter(ma);
    }
}
