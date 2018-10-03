package eu.miaounyan.isthereanynetwork.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import eu.miaounyan.isthereanynetwork.R;

public class MapAdapter extends ArrayAdapter<Integer> {
    public MapAdapter(Context context, List<Integer> colors){
        super(context, 0, colors);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell, parent, false);
        }

        int color = getItem(position);

        convertView.setBackgroundColor(color);

        return convertView;
    }
}
