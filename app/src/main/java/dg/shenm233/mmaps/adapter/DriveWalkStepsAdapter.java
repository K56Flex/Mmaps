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

package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.WalkStep;

import java.util.List;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.util.CommonUtils;
import dg.shenm233.mmaps.viewholder.BaseRecyclerViewHolder;
import dg.shenm233.mmaps.viewholder.OnViewClickListener;

public class DriveWalkStepsAdapter extends BaseRecyclerViewAdapter<DriveWalkStepsAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private List<DriveStep> mDriveStepList = null;
    private List<WalkStep> mWalkStepList = null;

    public DriveWalkStepsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private String mStartingPointText;
    private String mDestPointText;

    public void setStartingPointText(String s) {
        mStartingPointText = s;
    }

    public void setDestPointText(String s) {
        mDestPointText = s;
    }

    private LatLonPoint mStartingPoint;
    private LatLonPoint mDestPoint;

    public void setStartingPoint(LatLonPoint startingPoint) {
        mStartingPoint = startingPoint;
    }

    public void setDestPoint(LatLonPoint destPoint) {
        mDestPoint = destPoint;
    }

    public synchronized void setDriveStepList(List<DriveStep> driveSteps) {
        mDriveStepList = driveSteps;
        mWalkStepList = null;
    }

    public synchronized void setWalkStepList(List<WalkStep> walkSteps) {
        mDriveStepList = null;
        mWalkStepList = walkSteps;
    }

    public synchronized void clear() {
        mDriveStepList = null;
        mWalkStepList = null;
    }

    /**
     * 根据item位置返回对应DriveStep
     *
     * @param position 范围1~getItemCount() - 2，注意0或者getItemCount() - 1
     * @return 当position < 0 或 >= getItemCount() 返回 null
     * 当position = 0 或 = getItemCount() - 1 返回 null(由于item位置分别为起始点和终点)
     */
    public DriveStep getDriveStepAt(int position) {
        if (mDriveStepList == null) {
            return null;
        }

        if (position <= 0 || position >= getItemCount() - 1) {
            return null;
        } else {
            return mDriveStepList.get(position - 1);
        }
    }

    /**
     * 根据item位置返回对应WalkStep
     *
     * @param position 范围1~getItemCount() - 2，注意0或者getItemCount() - 1
     * @return 当position < 0 或 >= getItemCount() 返回 null
     * 当position = 0 或 = getItemCount() - 1 返回 null(由于item位置分别为起始点和终点)
     */
    public WalkStep getWalkStepAt(int position) {
        if (mWalkStepList == null) {
            return null;
        }

        if (position <= 0 || position >= getItemCount() - 1) {
            return null;
        } else {
            return mWalkStepList.get(position - 1);
        }
    }

    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        ViewGroup stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.drive_walk_step_item, parent, false);
        return new ViewHolder(stepView);
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        if (position == 0) {
            holder.mDirection.setImageResource(getIconFromPlace(mStartingPointText));
            holder.mInstruction.setText(mStartingPointText);
            holder.setTag(mStartingPoint);
            return;
        }
        if (position == getItemCount() - 1) {
            holder.mDirection.setImageResource(getIconFromPlace(mDestPointText));
            holder.mInstruction.setText(mDestPointText);
            holder.setTag(mDestPoint);
            return;
        }

        if (mDriveStepList != null) {
            DriveStep driveStep = mDriveStepList.get(position - 1);
            setDirectionIcon(holder.mDirection, driveStep.getAction());
            holder.mInstruction.setText(driveStep.getInstruction());
            holder.setTag(driveStep);
        } else if (mWalkStepList != null) {
            WalkStep walkStep = mWalkStepList.get(position - 1);
            setDirectionIcon(holder.mDirection, walkStep.getAction());
            holder.mInstruction.setText(walkStep.getInstruction());
            holder.setTag(walkStep);
        }
    }

    private int getIconFromPlace(String s) {
        return mContext.getText(R.string.my_location).equals(s) ?
                R.drawable.ic_my_location : R.drawable.ic_place;
    }

    private void setDirectionIcon(ImageView view, String direction) {
        int resId = getIconResIdFromDirection(direction);
        if (resId == -1) {
            view.setImageDrawable(null);
        } else {
            view.setImageResource(resId);
        }
    }

    private int getIconResIdFromDirection(String s) {
        if (CommonUtils.isStringEmpty(s)) {
            return -1;
        }
        switch (s) {
            case "往前走":
            case "直行":
                return R.drawable.map_step_ahead;
            case "左转":
                return R.drawable.map_step_left;
            case "右转":
                return R.drawable.map_step_right;

            case "靠左":
            case "向左前方":
            case "向左前方行走":
            case "向左前方行驶":
                return R.drawable.map_step_left_front;

            case "靠右":
            case "向右前方":
            case "向右前方行走":
            case "向右前方行驶":
                return R.drawable.map_step_right_front;

            case "进入环岛":
            case "离开环岛":
                return R.drawable.map_step_roundabout;

            case "左转调头":
                return R.drawable.map_step_left_turn_round;
            case "右转调头":
                return R.drawable.map_step_right_turn_round;

            case "向左后方":
            case "向左后方行走":
            case "向左后方行驶":
                return R.drawable.map_step_left_behind;

            case "向右后方":
            case "向右后方行走":
            case "向右后方行驶":
                return R.drawable.map_step_right_behind;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (mDriveStepList == null && mWalkStepList == null) {
            return 0;
        } else if (mDriveStepList != null) {
            return mDriveStepList.size() + 2; // 为出发点和目的点预留的item位置
        } else {
            return mWalkStepList.size() + 2; // 为出发点和目的点预留的item位置
        }
    }

    static class ViewHolder extends BaseRecyclerViewHolder {
        ImageView mDirection;
        TextView mInstruction;
        TextView mDistance;

        public ViewHolder(ViewGroup itemView) {
            super(itemView);
            mDirection = (ImageView) itemView.findViewById(R.id.step_direction_icon);
            mInstruction = (TextView) itemView.findViewById(R.id.step_instruction);
            mDistance = (TextView) itemView.findViewById(R.id.step_distance);

            mInstruction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnViewClickListener listener = getOnViewClickListener();
                    if (listener != null) {
                        listener.onClick(v, getTag());
                    }
                }
            });
        }
    }
}
