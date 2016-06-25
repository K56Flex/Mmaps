package dg.shenm233.mmaps.viewmodel.card;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dg.shenm233.mmaps.R;

public class FavoriteLocationCard extends Card<FavoriteLocationCard.ViewHolder> {
    private Context mContext;
    private String mName;
    private String mDistrict;

    public FavoriteLocationCard(Context context) {
        super(context);
        mContext = context;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setDistrict(String district) {
        mDistrict = district;
    }

    public String getName() {
        return mName;
    }

    public String getDistrict() {
        return mDistrict;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.favorite_location_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        ViewHolder vh = (ViewHolder) viewHolder;
        vh.mNameView.setText(mName);
        vh.mDistrictView.setText(mDistrict);
    }

    static class ViewHolder extends Card.CardViewHolder {
        TextView mNameView;
        TextView mDistrictView;

        public ViewHolder(View itemView) {
            super(itemView);
            mNameView = (TextView) itemView.findViewById(R.id.favorite_item_name);
            mDistrictView = (TextView) itemView.findViewById(R.id.favorite_item_district);
            View removeButton = itemView.findViewById(R.id.favorite_item_remove);
            removeButton.setOnClickListener(this);
        }
    }
}
