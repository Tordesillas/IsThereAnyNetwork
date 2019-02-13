package eu.miaounyan.isthereanynetwork.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.model.RankItem;

public class RankingListAdapter extends RecyclerView.Adapter<RankingListAdapter.ViewHolder> {
    private List<RankItem> rankItems;

    public RankingListAdapter(List<RankItem> myList) {
        this.rankItems = myList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.operatorName.setText(rankItems.get(position).getOperatorName());
        holder.signalStrength.setText(Math.round(rankItems.get(position).getSignalStrength())+"");
        setOperatorPicture(holder.operatorPic, rankItems.get(position).getOperatorName());
    }

    private void setOperatorPicture(ImageView image, String operatorName) {
        switch (operatorName) {
            case "Bouygues Telecom":
                image.setImageResource(R.drawable.bouygues); break;
            case "Orange F":
                image.setImageResource(R.drawable.orange); break;
            case "Free":
                image.setImageResource(R.drawable.free); break;
            case "F SFR":
                image.setImageResource(R.drawable.sfr); break;
        }
    }

    @Override
    public int getItemCount() {
        return rankItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView operatorName;
        TextView signalStrength;
        ImageView operatorPic;

        ViewHolder(View itemView) {
            super(itemView);
            operatorName = itemView.findViewById(R.id.operatorName);
            signalStrength = itemView.findViewById(R.id.signalStrength);
            operatorPic = itemView.findViewById(R.id.operatorPic);
        }
    }
}