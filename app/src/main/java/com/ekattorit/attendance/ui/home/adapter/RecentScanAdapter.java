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
        holder.binding.tvEmployeeName.setText(scanItem.getEmployee().getEmpName());

        String inTime = scanItem.getFirstScan();
        String outTime = scanItem.getLastScan();


        if (inTime != null && outTime != null) {

            try {
                holder.binding.tvFirstScan.setText(DateTimeFormat.timeFormat(inTime));

                if (inTime.substring(0, 5).equals(outTime.substring(0, 5))) {
                    holder.binding.mainView.setBackgroundColor(context.getResources().getColor(R.color.yellow_light));
                    holder.binding.tvLastScan.setText("-");

                } else {
                    holder.binding.mainView.setBackgroundColor(0);
                    holder.binding.tvLastScan.setText(DateTimeFormat.timeFormat(outTime));
                }

                if (DateTimeFormat.timeDiff(inTime, outTime) >= timeDiff) {
                    holder.binding.ivStatus.setImageResource(R.drawable.ic_verified);
                } else {
                    holder.binding.ivStatus.setImageResource(R.drawable.ic_round_warning);
                }


            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvFirstScan.setText("-");
            holder.binding.tvLastScan.setText("-");
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
