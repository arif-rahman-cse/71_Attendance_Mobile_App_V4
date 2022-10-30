package com.ekattorit.attendance.ui.employee

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekattorit.attendance.FaceEntity
import com.ekattorit.attendance.R
import com.ekattorit.attendance.databinding.OnlineEmployeeListBinding
import com.ekattorit.attendance.retrofit.RetrofitClient
import com.ekattorit.attendance.ui.MainActivity
import com.ekattorit.attendance.ui.employee.adapter.EmployeeListAdapter
import com.ekattorit.attendance.ui.scan.model.RpEmpDetails
import com.ekattorit.attendance.utils.UserCredentialPreference

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class OnlineEmployeeList : AppCompatActivity() {

    companion object {
        private const val TAG = "OnlineEmployeeList"
    }

    private lateinit var binding: OnlineEmployeeListBinding
    private val empDetailsList: ArrayList<RpEmpDetails> = ArrayList()
    private var employeeListAdapter: EmployeeListAdapter? = null
    var userCredentialPreference: UserCredentialPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.online_employee_list)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)
        binding = DataBindingUtil.setContentView(this, R.layout.online_employee_list)
        binding.toolbar.title.text = getString(R.string.employee_database)

        initRecyclerView()
        getEmployee(userCredentialPreference!!.userId)




        binding.etEmployeeSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                // filter your list from your input
                filterEmployee(s.toString().lowercase(Locale.getDefault()))
            }
        })

        binding.toolbar.backButton.setOnClickListener { onBackPressed() }
    }


    private fun filterEmployee(query: String) {
        val temp: ArrayList<RpEmpDetails> =ArrayList()

        for (item in empDetailsList) {
            if (item.empName.lowercase(Locale.ROOT).contains(query) || item.empId.lowercase(Locale.ROOT).contains(query)) {
                temp.add(item)
            }
        }
        //update recyclerview
        //update recyclerview
        employeeListAdapter!!.updateList(temp)
    }

    private fun getEmployee(userId: Int) {
        val employees = RetrofitClient.getInstance().api.getEmployee(userId)
        employees.enqueue(object : Callback<ArrayList<RpEmpDetails>> {
            override fun onResponse(
                call: Call<ArrayList<RpEmpDetails>>,
                response: Response<ArrayList<RpEmpDetails>>
            ) {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "onResponse: Code Word:" + response.code())
                if (response.isSuccessful && response.code() == 200) {
                    empDetailsList.clear()
                    assert(response.body() != null)
                    if (response.body()!!.isNotEmpty()) {
                        binding.errorView.visibility = View.GONE
                        empDetailsList.addAll(response.body()!!)
                        employeeListAdapter!!.notifyDataSetChanged()
                    } else {
                        binding.errorView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<RpEmpDetails>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "onFailure: Error: " + t.message)
            }
        })
    }

    private fun initRecyclerView() {
        employeeListAdapter =
            EmployeeListAdapter(
                this,
                empDetailsList
            )
        binding.rvEmployeeList.adapter = employeeListAdapter
        binding.rvEmployeeList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}