/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.viewmodel.card;

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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
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
