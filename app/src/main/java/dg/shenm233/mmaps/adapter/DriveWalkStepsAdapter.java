package dg.shenm233.mmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.WalkStep;

import java.util.List;

import dg.shenm233.mmaps.R;

public class DriveWalkStepsAdapter extends BaseRecyclerViewAdapter<DriveWalkStepsAdapter.StepViewHolder> {
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

    public void setDriveStepList(List<DriveStep> driveSteps) {
        mDriveStepList = driveSteps;
        mWalkStepList = null;
        notifyDataSetChanged();
    }

    public void setWalkStepList(List<WalkStep> walkSteps) {
        mDriveStepList = null;
        mWalkStepList = walkSteps;
        notifyDataSetChanged();
    }

    public void clear() {
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
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup stepView = (ViewGroup) mLayoutInflater.inflate(R.layout.drive_walk_step_item, parent, false);
        return new StepViewHolder(stepView, adapterListener);
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        if (position == 0) {
            holder.mDirection.setImageResource(getIconFromPlace(mStartingPointText));
            holder.mInstruction.setText(mStartingPointText);
            return;
        }
        if (position == getItemCount() - 1) {
            holder.mDirection.setImageResource(getIconFromPlace(mDestPointText));
            holder.mInstruction.setText(mDestPointText);
            return;
        }

        holder.mDirection.setImageDrawable(null);
        if (mDriveStepList != null) {
            DriveStep driveStep = mDriveStepList.get(position - 1);
            holder.mInstruction.setText(driveStep.getInstruction());
        } else if (mWalkStepList != null) {
            WalkStep walkStep = mWalkStepList.get(position - 1);
            holder.mInstruction.setText(walkStep.getInstruction());
        }
    }

    private int getIconFromPlace(String s) {
        return mContext.getText(R.string.my_location).equals(s) ?
                R.drawable.ic_my_location : R.drawable.ic_place;
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

    protected static class StepViewHolder extends BaseRecyclerViewAdapter.BaseViewHolder {
        protected ImageView mDirection;
        protected TextView mInstruction;
        protected TextView mDistance;

        public StepViewHolder(ViewGroup itemView, OnItemClickListener l) {
            super(itemView, l);
            mDirection = (ImageView) itemView.findViewById(R.id.step_direction_icon);
            mInstruction = (TextView) itemView.findViewById(R.id.step_instruction);
            mInstruction.setOnClickListener(this);
            mDistance = (TextView) itemView.findViewById(R.id.step_distance);
        }
    }
}
