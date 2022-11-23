package com.ekattorit.attendance.ui.employee

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.ekattorit.attendance.*
import com.ekattorit.attendance.databinding.ActivityEmployeeListV2Binding
import com.ekattorit.attendance.ui.MainActivity.Companion.userLists
import com.ekattorit.attendance.ui.employee.adapter.OfflineEmployeeListAdapter
import com.ekattorit.attendance.utils.AppConfig
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.material.snackbar.Snackbar
import com.ttv.face.FaceEngine
import java.util.*


class EmployeeListActivityV2 : AppCompatActivity(), OfflineEmployeeListAdapter.OnOfflineEmployeeItemClickListener {

    private lateinit var binding: ActivityEmployeeListV2Binding
    private var mydb: DBHelper? = null
    private var context: Context? = null
    private var offlineEmployeeListAdapter: OfflineEmployeeListAdapter? = null
    var userCredentialPreference: UserCredentialPreference? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_list_v2)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_employee_list_v2)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)

        if (userCredentialPreference!!.isFaceADDPermission){
            binding.addNewFaceBtn.visibility = View.VISIBLE
        }else{
            binding.addNewFaceBtn.visibility = View.GONE
        }

        binding.toolbar.title.text = getString(R.string.add_face)
        mydb = DBHelper(this)
        context = this
        mydb!!.getAllUsers()
        binding.tvTotalEmp.text = userLists.size.toString()
        initRecyclerView()


        if (userLists.isEmpty()) {
            binding.errorView.visibility = View.VISIBLE
            binding.errorView.text = getString(R.string.no_face_attached)
        } else {
            binding.errorView.visibility = View.GONE
        }

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


        binding.toolbar.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.addNewFaceBtn.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val intent = Intent(this, EmployeeCardScanActivity::class.java)
                intent.putExtra(AppConfig.IS_FROM_ADD_FACE, true)
                startActivity(intent)
                finish()


            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CAMERA_PERMISSION_CODE
                )
            }

//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_PICK
//            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, EmployeeCardScanActivity::class.java)
                intent.putExtra(AppConfig.IS_FROM_ADD_FACE, true)
                finish()
                startActivity(intent)
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Camera Permission Denied")
                //Toast.makeText(EmployeeCardScanActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun filterEmployee(query: String) {
        val temp: ArrayList<FaceEntity> = ArrayList()

        for (item in userLists) {
            if (item.userName.lowercase(Locale.ROOT).contains(query)||item.employeeId.lowercase(Locale.ROOT).contains(query)) {
                temp.add(item)
            }
        }
        //update recyclerview
        //update recyclerview
        offlineEmployeeListAdapter!!.updateList(temp)
    }

    private fun initRecyclerView() {
        offlineEmployeeListAdapter = OfflineEmployeeListAdapter(this, userLists,
            userCredentialPreference!!.isFaceRemovePermission,  this)
        binding.rvEmployeeList.adapter = offlineEmployeeListAdapter
        binding.rvEmployeeList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }


    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        private const val TAG = "EmployeeListActivityV2"
    }

    override fun onDeleteClick(empDetails: FaceEntity?, position : Int) {
        Log.d(TAG, "onDeleteClick: Clkied: ${empDetails!!.employeeId} pOSITION:$position")
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("কর্মী মুছুন !")
        alertDialog.setMessage("আপনি কি নিশ্চিত "+ empDetails.userName+"  কে মুছে ফেলতে চান?")
        alertDialog.setIcon(R.drawable.ic_delete)
        alertDialog.setButton(
            AlertDialog.BUTTON_NEGATIVE, "হ্যাঁ"
        ) { dialog: DialogInterface?, which: Int ->
            mydb!!.deleteUser(empDetails.employeeId)
            FaceEngine.getInstance(applicationContext).removeFaceFeature(empDetails.user_id)
            userLists.removeAt(userLists.indexOf(empDetails))
            offlineEmployeeListAdapter!!.updateList(userLists)
            alertDialog.dismiss()
            Snackbar.make(findViewById(android.R.id.content), "কর্মী সফল ভাবে মুছে ফেলা হয়েছে।", Snackbar.LENGTH_LONG).show();

        }
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "না "
        ) { dialog: DialogInterface?, which: Int ->
            Log.d(TAG, "didRemoveBtnPressed: Disagreed!")
            alertDialog.dismiss()
        }

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    override fun showErrorMsg() {
        binding.errorView.visibility = View.VISIBLE
        binding.errorView.text = getString(R.string.no_face_attached)
    }


}