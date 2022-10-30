package com.ekattorit.attendance.ui.employee.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ekattorit.attendance.FaceEntity;
import com.ekattorit.attendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.attendance.utils.AppConfig;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.databinding.ItemViewEmployeeListBinding;

import java.util.ArrayList;

public class OfflineEmployeeListAdapter extends RecyclerView.Adapter<OfflineEmployeeListAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private ArrayList<FaceEntity> employeeList;
    private boolean hasDeletePermission;
    private OnOfflineEmployeeItemClickListener onItemClickListener;


    public OfflineEmployeeListAdapter(Context context, ArrayList<FaceEntity> employeeList, boolean hasDeletePermission,
                                      OnOfflineEmployeeItemClickListener onItemClickListener) {
        this.context = context;
        this.employeeList = employeeList;
        this.hasDeletePermission = hasDeletePermission;
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_employee_list, parent, false);
        ItemViewEmployeeListBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FaceEntity empDetails = employeeList.get(position);

        if (hasDeletePermission){
            holder.binding.ivDelete.setVisibility(View.VISIBLE);
        }else {
            holder.binding.ivDelete.setVisibility(View.GONE);
        }

        holder.binding.tvEmpName.setText(empDetails.userName);
        holder.binding.tvEmpId.setText(String.valueOf(empDetails.employeeId));
        //holder.binding.tvEmpAddress.setText(empDetails.);

        holder.binding.ivEmpImg.setImageBitmap(empDetails.headImg);

        holder.binding.ivDelete.setOnClickListener(v->{
            onItemClickListener.onDeleteClick(empDetails, position);
        });


    }


    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewEmployeeListBinding binding;

        MyViewHolder(@NonNull ItemViewEmployeeListBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }

    public void updateList( ArrayList<FaceEntity> list){
        employeeList = list;
        notifyDataSetChanged();
        if (employeeList.size() == 0) {
            onItemClickListener.showErrorMsg();
        }
    }

    public  interface OnOfflineEmployeeItemClickListener{
        void onDeleteClick(FaceEntity empDetails, int position);
        void showErrorMsg();
    }


    public void removeAt(int position) {
        Log.d(TAG, "removeAt: Length: "+position);
        Log.d(TAG, "removeAt: Size: "+employeeList.size());
        employeeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, employeeList.size());
        //notifyDataSetChanged();

        if (employeeList.size() == 0) {
            onItemClickListener.showErrorMsg();
        }
    }


}
