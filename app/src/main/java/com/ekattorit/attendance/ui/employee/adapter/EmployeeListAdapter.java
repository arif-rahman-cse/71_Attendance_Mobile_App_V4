package com.ekattorit.attendance.ui.employee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ekattorit.attendance.FaceEntity;
import com.ekattorit.attendance.utils.AppConfig;
import com.ekattorit.attendance.R;
import com.ekattorit.attendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.attendance.databinding.ItemViewEmployeeListBinding;

import java.util.ArrayList;

public class EmployeeListAdapter extends RecyclerView.Adapter<EmployeeListAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private ArrayList<RpEmpDetails> employeeList;


    public EmployeeListAdapter(Context context, ArrayList<RpEmpDetails> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
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
        RpEmpDetails empDetails = employeeList.get(position);

        holder.binding.tvEmpName.setText(empDetails.getEmpName());
        holder.binding.tvEmpId.setText(empDetails.getEmpId());
        holder.binding.tvEmpAddress.setText(empDetails.getAddress());

        Glide.with(context)
                .load(AppConfig.Base_URL_ONLINE_IMG + empDetails.getEmployeeImg())
                .placeholder(R.drawable.loading_01)
                .centerCrop()
                .error(R.drawable.default_profile_pic)
                .into(holder.binding.ivEmpImg);



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

    public void updateList( ArrayList<RpEmpDetails> list){
        employeeList = list;
        notifyDataSetChanged();
    }




}
