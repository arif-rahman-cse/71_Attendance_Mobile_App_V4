package com.ekattorit.attendance.ui.home.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ekattorit.attendance.utils.DateTimeFormat;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.ui.home.model.ScanItem;
import com.ekattorit.attendance.databinding.ItemViewRecentScanBinding;

import java.text.ParseException;
import java.util.List;


public class RecentScanAdapter extends RecyclerView.Adapter<RecentScanAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private List<ScanItem> scanItemList;
    private float timeDiff;


    public RecentScanAdapter(Context context, List<ScanItem> scanItemList, float timeDiff) {
        this.context = context;
        this.scanItemList = scanItemList;
        this.timeDiff = timeDiff;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_recent_scan, parent, false);
        ItemViewRecentScanBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ScanItem scanItem = scanItemList.get(position);
        holder.binding.tvCardNo.setText(scanItem.getEmployee().getEmpId());
        holder.binding.tvEmployeeName.setText(scanItem.getEmployee().getEmpNameEn());

        String inTime = scanItem.getFirstInTime();
        String outTime = scanItem.getFirstOutTime();




        if (inTime != null) {

            try {
                holder.binding.tvFirstInScan.setText(DateTimeFormat.timeFormat(inTime));

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvFirstInScan.setText("-");
            //.binding.tvFirstOutScan.setText("-");
        }

        if (outTime != null && inTime != null) {

            try {
                //holder.binding.tvFirstOutScan.setText(DateTimeFormat.timeFormat(outTime));

                if (inTime.substring(0, 5).equals(outTime.substring(0, 5))) {
                    holder.binding.mainView.setBackgroundColor(context.getResources().getColor(R.color.yellow_light));
                    holder.binding.tvFirstOutScan.setText("-");

                } else {
                    holder.binding.mainView.setBackgroundColor(0);
                    holder.binding.tvFirstOutScan.setText(DateTimeFormat.timeFormat(outTime));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvFirstOutScan.setText("-");
            //.binding.tvFirstOutScan.setText("-");
        }

        String secondInTime = scanItem.getSecondInTime();
        String secondOutTime = scanItem.getSecondOutTime();

        if (secondInTime != null) {

            try {
                holder.binding.tvSecondInScan.setText(DateTimeFormat.timeFormat(secondInTime));

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvSecondInScan.setText("-");
            //.binding.tvFirstOutScan.setText("-");
        }

        if (secondOutTime != null && secondInTime != null) {

            try {
                //holder.binding.tvSecondOutScan.setText(DateTimeFormat.timeFormat(secondOutTime));

                if (secondInTime.substring(0, 5).equals(secondOutTime.substring(0, 5))) {
                    holder.binding.mainView.setBackgroundColor(context.getResources().getColor(R.color.yellow_light));
                    holder.binding.tvSecondOutScan.setText("-");

                } else {
                    holder.binding.mainView.setBackgroundColor(0);
                    holder.binding.tvSecondOutScan.setText(DateTimeFormat.timeFormat(secondOutTime));
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvSecondOutScan.setText("-");
            //.binding.tvFirstOutScan.setText("-");
        }

    }


    @Override
    public int getItemCount() {
        return scanItemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewRecentScanBinding binding;

        MyViewHolder(@NonNull ItemViewRecentScanBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }


}
