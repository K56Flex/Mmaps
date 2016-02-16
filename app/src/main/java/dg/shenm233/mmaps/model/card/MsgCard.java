package dg.shenm233.mmaps.model.card;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dg.shenm233.mmaps.R;

public class MsgCard extends Card<MsgCard.ViewHolder> {
    public final static int TYPE = 0x2b;

    private Context mContext;
    private String mText;

    public MsgCard(Context context) {
        super(context);
        mContext = context;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.msg_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void updateViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.updateViewHolder(viewHolder);
        ViewHolder vh = (ViewHolder) viewHolder;
        vh.mMsgText.setText(mText);
    }

    static class ViewHolder extends Card.CardViewHolder {
        private TextView mMsgText;

        public ViewHolder(View itemView) {
            super(itemView);
            mMsgText = (TextView) itemView.findViewById(R.id.msg_text);
        }
    }
}
