package com.ekattorit.attendance.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.SuccessResult
import com.android.volley.toolbox.ImageRequest
import com.bumptech.glide.Glide
import com.ekattorit.attendance.*
import com.ekattorit.attendance.databinding.ActivityMainBinding
import com.ekattorit.attendance.retrofit.RetrofitClient
import com.ekattorit.attendance.ui.employee.EmployeeCardScanActivity
import com.ekattorit.attendance.ui.employee.EmployeeListActivityV2
import com.ekattorit.attendance.ui.employee.OnlineEmployeeList
import com.ekattorit.attendance.ui.employee.modle.RpItemFace
import com.ekattorit.attendance.ui.home.adapter.RecentScanAdapter
import com.ekattorit.attendance.ui.home.model.RpRecentScan
import com.ekattorit.attendance.ui.home.model.ScanItem
import com.ekattorit.attendance.ui.login.LoginActivity
import com.ekattorit.attendance.ui.report.AttendanceSummary
import com.ekattorit.attendance.ui.report.DailyAttendanceStatus
import com.ekattorit.attendance.ui.report.SingleRangeAttendance
import com.ekattorit.attendance.ui.scan.CameraActivity
import com.ekattorit.attendance.utils.AppConfig
import com.ekattorit.attendance.utils.AppProgressBar
import com.ekattorit.attendance.utils.EmployeeFacePreference
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.material.navigation.NavigationView
import com.ttv.face.FaceEngine
import com.ttv.face.FaceFeatureInfo
import com.ttv.face.FaceResult
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.MessageFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    var userCredentialPreference: UserCredentialPreference? = null
    private val scanItemList: ArrayList<ScanItem> = ArrayList()
    private var recentScanAdapter: RecentScanAdapter? = null


    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        lateinit var userLists: ArrayList<FaceEntity>
        private const val TAG = "MainActivity"
    }


    private var context: Context? = null
    private var mydb: DBHelper? = null

    init {
        userLists = ArrayList(0)
    }


    private var linearLayoutManager: LinearLayoutManager? = null
    private var isScrolling = false
    private var currentItems: Int = 0
    private var totalItems: Int = 0
    private var scrollOutItems: Int = 0
    private var currentPage: Int = 0
    private var lastPage: Int = 0

    //For Convert URL to Bitmap
    private var myExecutor : ExecutorService?=null
    private var myHandler : Handler?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)
        binding.navigationDrawer.setNavigationItemSelectedListener(this)


        // Declaring and initializing an Executor and a Handler
         myExecutor = Executors.newSingleThreadExecutor()
         myHandler = Handler(Looper.getMainLooper())

        context = this
        FaceEngine.getInstance(this).setActivation("")
        FaceEngine.getInstance(this).init(2)
        mydb = DBHelper(this)
        mydb!!.getAllUsers()

        initRecyclerView()
        getRecentScan()


        binding.cvScanCircle.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            finish()
            startActivity(intent)
        }

        binding.checkEmployee.setOnClickListener {


            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val intent = Intent(this, EmployeeCardScanActivity::class.java)
                intent.putExtra(AppConfig.IS_FROM_ADD_FACE, false)
                startActivity(intent)

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CAMERA_PERMISSION_CODE
                )
            }

        }



        binding.drawerMenuBtn.setOnClickListener {
            val userName = findViewById<TextView>(R.id.tv_name)
            val phone = findViewById<TextView>(R.id.tv_phone)
            val profileImage = findViewById<ImageView>(R.id.circleImageView)
            userName.text = userCredentialPreference!!.name
            phone.text = userCredentialPreference!!.userPhone

            Glide.with(this)
                .load(AppConfig.Base_URL_ONLINE_IMG.toString() + userCredentialPreference!!.profileUrl)
                .placeholder(R.drawable.loading_01)
                .centerInside()
                .error(R.drawable.default_profile_pic)
                .into(profileImage)
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }


        //Add pagination
        binding.rvDailySales.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d(TAG, "onScrollStateChanged: Scrolling")
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d(TAG, "onScrolled: Scrolling Completed")

                currentItems = linearLayoutManager!!.childCount
                totalItems = linearLayoutManager!!.itemCount
                scrollOutItems = linearLayoutManager!!.findFirstVisibleItemPosition()

                Log.d(TAG, "onScrolled: Current item: $currentItems")
                Log.d(TAG, "onScrolled: Total Item: $totalItems")
                Log.d(TAG, "onScrolled: Scroll Out Item: $scrollOutItems")
                var scrolledItem = currentItems + scrollOutItems
                Log.d(TAG, "onScrolled: total scrolled item: $scrolledItem")

                if (isScrolling && (currentItems + scrollOutItems >= totalItems)) {
                    isScrolling = false
                    Log.d(TAG, "onScrolled: Current Page: $currentPage")
                    Log.d(TAG, "onScrolled: Last Page: $lastPage")
                    if (currentPage != lastPage) {
                        getRecentScan()
                    } else {
                        Log.d(TAG, "onScrolled: No all loaded ")
                        //Snackbar.make(binding.mainView, "কোন ডেটা অবশিষ্ট নেই!", Snackbar.LENGTH_SHORT).show()
                        //Toast.makeText(context, "No Data left! ", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })


        //getRecentScan()

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


    private fun initRecyclerView() {
        recentScanAdapter = RecentScanAdapter(this, scanItemList, userCredentialPreference!!.attendanceTimeDiff)
        binding.rvDailySales.adapter = recentScanAdapter
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDailySales.layoutManager = linearLayoutManager
    }

    private fun getRecentScan() {
        binding.progressBar.visibility = View.VISIBLE
        Log.d(TAG, "getRecentScan: Current Page: $currentPage")
        Log.d(TAG, "getRecentScan: Last Page: $lastPage")
        val recentScanCall: Call<RpRecentScan> = RetrofitClient
            .getInstance()
            .api
            .getRecentScan(currentPage + 1, userCredentialPreference!!.userId)
        recentScanCall.enqueue(object : Callback<RpRecentScan?> {
            override fun onResponse(call: Call<RpRecentScan?>, response: Response<RpRecentScan?>) {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "onResponse: CODE: " + response.code())
                //Log.d(TAG, "onResponse: "+response.body().getResults().get(0).getFirstScan());
                if (response.isSuccessful && response.code() == 200) {
                    // scanItemList.clear()

                    if (response.body() != null) {

                        Log.d(TAG, "onResponse: Not Null")
                        if (response.body()!!.results.size > 0) {

                            currentPage = response.body()!!.currentPageNumber
                            lastPage = response.body()!!.totalPageNumber

                            Log.d(TAG, "getRecentScan2: Current Page: $currentPage")
                            Log.d(TAG, "getRecentScan2: Last Page: $lastPage")

                            binding.tvTotalScanCard.text = MessageFormat.format(
                                "মোট স্ক্যান হয়েছে {0} জন",
                                response.body()!!.count
                            )
                            //userCredentialPreference!!.totalScan = response.body()!!.count
                            binding.errorView.visibility = View.GONE
                            scanItemList.addAll(response.body()!!.results)
                            recentScanAdapter!!.notifyDataSetChanged()
                        } else {
                            Log.d(TAG, "onResponse: null")
                            binding.tvTotalScanCard.text =
                                MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", 0)
                            //userCredentialPreference!!.totalScan = 0
                            binding.errorView.visibility = View.VISIBLE
                        }
                    } else {
                        Log.d(TAG, "onResponse: null")
                        binding.tvTotalScanCard.text =
                            MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", 0)
                        //userCredentialPreference!!.totalScan = 0
                        binding.errorView.visibility = View.VISIBLE
                    }
                } else {
                    Log.d(TAG, "onResponse: Error: " + response.errorBody()!!.string())

                }
            }

            override fun onFailure(call: Call<RpRecentScan?>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "onResponse: " + t.message)
            }
        })
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.log_out -> {
                logout()
                return true
            }
            R.id.daily_attendance_report -> {
                closeDrawer()
                val intent = Intent(this, DailyAttendanceStatus::class.java)
                startActivity(intent)
                return true
            }
            R.id.single_attendance_report -> {
                closeDrawer()
                val intent = Intent(this, SingleRangeAttendance::class.java)
                startActivity(intent)
                return true
            }
            R.id.attendance_summery -> {
                closeDrawer()
                val intent = Intent(this, AttendanceSummary::class.java)
                startActivity(intent)
                return true
            }
            R.id.support_number -> {
                closeDrawer()
                startNewActivity(this, SupportActivity::class.java)
                return true
            }
            R.id.id_add_new_face -> {
                closeDrawer()
                startNewActivity(this, EmployeeListActivityV2::class.java)
                return true
            }
            R.id.id_worker -> {
                closeDrawer()
                startNewActivity(this, OnlineEmployeeList::class.java)
                return true
            }
            R.id.sync_face -> {
                closeDrawer()
                AppProgressBar.messageProgressFixed(context, "সার্ভার থেকে ফেস সিঙ্ক হচ্ছে... কিছু সময় লাগতে পারে। অনুগ্রহ করে অ্যাপ বন্ধ করবেন না।")
                syncFaceWithServer()

                return true
            }
            else -> return false
        }

    }

    private fun syncFaceWithServer() {
        getEmployee(userCredentialPreference!!.userId)
    }

    private fun getEmployee(userId: Int) {
        val rpShift = RetrofitClient.getInstance().api.getEmployeeFace(userId)
        rpShift.enqueue(object : Callback<java.util.ArrayList<RpItemFace?>?> {
            override fun onResponse(
                call: Call<java.util.ArrayList<RpItemFace?>?>,
                response: Response<java.util.ArrayList<RpItemFace?>?>
            ) {
                if (response.isSuccessful && response.code() == 200) {
                    Log.d(TAG, "onResponse: Success")
                    if (response.body()!!.size > 0) {
                        Log.d(TAG, "onResponse: Face Size: "+response.body()!!.size )
                        for (item in response.body()!!) {
                            insertFaceInLocalDB(item)
                            
                        }
                        AppProgressBar.hideMessageProgress()
                    }else{
                        AppProgressBar.hideMessageProgress()
                    }
                }else{
                    AppProgressBar.hideMessageProgress()
                }
            }

            override fun onFailure(call: Call<java.util.ArrayList<RpItemFace?>?>, t: Throwable) {
                Log.d(TAG, "onFailure: Error: " + t.message)
                AppProgressBar.hideMessageProgress()
            }
        })
    }


    private fun insertFaceInLocalDB(item: RpItemFace?) {

        try {
            Log.d(TAG, "onResponse: syncing...")
            val url = AppConfig.Base_URL_ONLINE_IMG + item!!.face

            myExecutor!!.execute {
                val bitmap = mLoad(url)
                //Log.d(TAG, "onCreate: Image loaded")
                myHandler!!.post {
                    //Log.d(TAG, "onCreate: Image loaded")
                    if(bitmap!=null){
                        //Log.d(TAG, "onCreate: Detect Image")
                        val faceResults: MutableList<FaceResult> = FaceEngine.getInstance(this).detectFace(bitmap)
                        if (faceResults.count() == 1) {
                            FaceEngine.getInstance(context).extractFeature(bitmap, true, faceResults)
                            //val result: SearchResult = FaceEngine.getInstance(this).searchFaceFeature(FaceFeature(faceResults[0].feature))
                            val cropRect = Utils.getBestRect(bitmap.width, bitmap.height, faceResults[0].rect)
                            val headImg = Utils.crop(
                                bitmap,
                                cropRect.left,
                                cropRect.top,
                                cropRect.width(),
                                cropRect.height(),
                                120,
                                120
                            )

                            val user_id = mydb!!.insertUser(item.empName, item.empId, headImg, faceResults[0].feature)
                            val _face = FaceEntity(
                                user_id,
                                item.empName,
                                item.empId,
                                bitmap,
                                faceResults[0].feature
                            )
                            userLists.add(_face)
                            val faceFeatureInfo = FaceFeatureInfo(user_id, faceResults[0].feature)
                            FaceEngine.getInstance(context).registerFaceFeature(faceFeatureInfo)
                            //Log.d(TAG, "insertFaceInLocalDB: Success!!")


                        } else {
                            Log.d(TAG, "insertFaceInLocalDB: No Face Detect!!")
                            Toast.makeText(context, "No Face Detect for employee $item!!.empName", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            //handle exception
            Log.d(TAG, "insertFaceInLocalDB: ${e.message}")
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }


    private fun mLoad(string: String): Bitmap? {
        Log.d(TAG, "mLoad: Loading...$string")
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }




    private fun startNewActivity(packageContext: Context, cls: Class<*>) {
        startActivity(Intent(packageContext, cls))
        //finish()
    }


    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START, true)
    }

    private fun logout() {
        userCredentialPreference!!.deleteSharedPreference(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        finish()
        startActivity(intent)

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Called")

    }
}