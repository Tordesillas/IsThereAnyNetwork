package eu.miaounyan.isthereanynetwork.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.model.RankItem;

public class RankingListAdapter extends ArrayAdapter<RankItem> {
    public RankingListAdapter(Context context, List<RankItem> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.rank_item, null);
        }

        RankItem item = getItem(position);

        ((TextView) convertView.findViewById(R.id.operatorName)).setText(item.getOperatorName());
        ((TextView) convertView.findViewById(R.id.signalStrength)).setText(item.getSignalStrength()+"");

        return convertView;
    }
}
