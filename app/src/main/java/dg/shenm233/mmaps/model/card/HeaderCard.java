package dg.shenm233.mmaps.model.card;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dg.shenm233.mmaps.R;

public class HeaderCard extends Card<HeaderCard.ViewHolder> {
    private Context mContext;
    private String mHeaderText;

    public HeaderCard(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.header_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        ((ViewHolder) viewHolder).mHeader.setText(mHeaderText);
    }

    public void setHeader(String s) {
        mHeaderText = s;
    }

    static class ViewHolder extends Card.CardViewHolder {
        private TextView mHeader;

        public ViewHolder(View itemView) {
            super(itemView);
            mHeader = (TextView) itemView.findViewById(R.id.header);
        }
    }
}
