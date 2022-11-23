package com.ekattorit.attendance.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ekattorit.attendance.DBHelper
import com.ekattorit.attendance.FaceEntity
import com.ekattorit.attendance.R
import com.ekattorit.attendance.Utils
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
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.ttv.face.FaceEngine
import com.ttv.face.FaceFeatureInfo
import com.ttv.face.FaceResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.text.MessageFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    var userCredentialPreference: UserCredentialPreference? = null
    private val scanItemList: ArrayList<ScanItem> = ArrayList()
    private var recentScanAdapter: RecentScanAdapter? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    var isScanFaceCircleClicked = false
    private val LOCATION_PERMISSION_ID = 102


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
    private var myExecutor: ExecutorService? = null
    private var myHandler: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
            isScanFaceCircleClicked = true
            getUserCurrentLocation()

        }

        binding.checkEmployee.setOnClickListener {


            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                val intent = Intent(this, EmployeeCardScanActivity::class.java)
                intent.putExtra(AppConfig.IS_FROM_ADD_FACE, false)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent)

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
                val scrolledItem = currentItems + scrollOutItems
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
        recentScanAdapter =
            RecentScanAdapter(this, scanItemList, userCredentialPreference!!.attendanceTimeDiff)
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
            .getRecentScan(
                userCredentialPreference!!.userToken,
                currentPage + 1,
                userCredentialPreference!!.userId
            )
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
                mydb!!.deleteAllUser()
                mydb!!.getAllUsers()
                Log.d(TAG, "onNavigationItemSelected: Local User Size: ${userLists.size}")
                AppProgressBar.messageProgressFixed(
                    context,
                    "সার্ভার থেকে ফেস ডাউনলোড হচ্ছে... কিছু সময় লাগতে পারে। অনুগ্রহ করে অ্যাপ বন্ধ করবেন না।"
                )
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
        val rpShift = RetrofitClient.getInstance().api.getEmployeeFace(
            userCredentialPreference!!.userToken,
            userId
        )
        rpShift.enqueue(object : Callback<ArrayList<RpItemFace?>?> {
            override fun onResponse(
                call: Call<ArrayList<RpItemFace?>?>,
                response: Response<ArrayList<RpItemFace?>?>
            ) {
                if (response.isSuccessful && response.code() == 200) {
                    Log.d(TAG, "onResponse: Success")
                    if (response.body()!!.size > 0) {
                        Log.d(TAG, "onResponse: Face Size: " + response.body()!!.size)
                        val size = response.body()!!.size
                        for (item in response.body()!!) {
                            insertFaceInLocalDB(item, size)
                            Log.d(TAG, "onResponse: Item: ${item!!.empId}")

                        }
                        Log.d(TAG, "onResponse: Iteration done")
                        //AppProgressBar.hideMessageProgress()
                    } else {
                        AppProgressBar.hideMessageProgress()
                    }
                } else {
                    AppProgressBar.hideMessageProgress()
                }
            }

            override fun onFailure(call: Call<ArrayList<RpItemFace?>?>, t: Throwable) {
                Log.d(TAG, "onFailure: Error: " + t.message)
                AppProgressBar.hideMessageProgress()
            }
        })
    }


    private fun insertFaceInLocalDB(item: RpItemFace?, size: Int) {

        try {
            Log.d(TAG, "onResponse: syncing...")
            val url = AppConfig.Base_URL_ONLINE_IMG + item!!.face

            myExecutor!!.execute {
                val bitmap = mLoad(url)
                //Log.d(TAG, "onCreate: Image loaded")
                myHandler!!.post {
                    //Log.d(TAG, "onCreate: Image loaded")
                    if (bitmap != null) {
                        //Log.d(TAG, "onCreate: Detect Image")
                        val faceResults: MutableList<FaceResult> =
                            FaceEngine.getInstance(this).detectFace(bitmap)
                        if (faceResults.count() == 1) {
                            FaceEngine.getInstance(context)
                                .extractFeature(bitmap, true, faceResults)
                            //val result: SearchResult = FaceEngine.getInstance(this).searchFaceFeature(FaceFeature(faceResults[0].feature))
                            val cropRect =
                                Utils.getBestRect(bitmap.width, bitmap.height, faceResults[0].rect)
                            val headImg = Utils.crop(
                                bitmap,
                                cropRect.left,
                                cropRect.top,
                                cropRect.width(),
                                cropRect.height(),
                                120,
                                120
                            )

                            val user_id = mydb!!.insertUser(
                                item.empName,
                                item.empId,
                                headImg,
                                faceResults[0].feature
                            )
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
                            Log.d(
                                TAG,
                                "insertFaceInLocalDB: Size Local: ${userLists.size} And Size Server: $size"
                            )

                            if (userLists.size >= size) {
                                AppProgressBar.hideMessageProgress()
                                AppProgressBar.userActionSuccessPb(
                                    context,
                                    "ফেইস সিঙ্ক সফল হয়েছে !"
                                )
                            }


                        } else {
                            Log.d(TAG, "insertFaceInLocalDB: No Face Detect!!")
                            Toast.makeText(
                                context,
                                "No Face Detect for employee " + item.empName,
                                Toast.LENGTH_SHORT
                            ).show()
                            AppProgressBar.hideMessageProgress()
                        }
                    } else {
                        AppProgressBar.hideMessageProgress()
                        Toast.makeText(context, "Face Download Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            //handle exception
            AppProgressBar.hideMessageProgress()
            Log.d(TAG, "insertFaceInLocalDB: ${e.message}")
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

    }


    private fun mLoad(string: String): Bitmap? {
        Log.d(TAG, "mLoad: Loading...$string")
        //val url: URL = mStringToURL(string)!!
//        val connection: HttpURLConnection?
//        try {
//            connection = url.openConnection() as HttpURLConnection
//            connection.connect()
//            val inputStream: InputStream = connection.inputStream
//            val bufferedInputStream = BufferedInputStream(inputStream)
//            return BitmapFactory.decodeStream(bufferedInputStream)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
//        }
        //AppProgressBar.hideMessageProgress()
        //return null

//        return Glide.with(context!!)
//            .asBitmap()
//            .load(string)
//            .into(object : CustomTarget<Bitmap?>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    @Nullable transition: Transition<in Bitmap?>?
//                ) {
//                }
//
//                override fun onLoadCleared(@Nullable placeholder: Drawable?) {}
//            })
        return Glide.with(applicationContext).asBitmap().load(string).submit(120, 120).get();

//        try {
//            return Glide
//                .with(context!!)
//                .asBitmap()
//                .load(string)
//                .submit()
//                .get()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error: " + e.printStackTrace(), Toast.LENGTH_LONG).show()
//        }
//        return null
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

    private fun getUserCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                //AppUtils.showMessageProgress(OutletVerificationActivity.this, "অপেক্ষা করুন ...");
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mFusedLocationClient!!.lastLocation.addOnCompleteListener { task: Task<Location?> ->
                        val location = task.result
                        if (location == null) {
                            Log.d(TAG, "getUserCurrentLocation: Location is null")
                            //getUserCurrentLocation();
                        } else {
                            Log.d(TAG, "getLastLocation: onComplete: " + location.latitude)
                            Log.d(TAG, "getLastLocation: onComplete: " + location.longitude)
                            //getAddress(location.getLatitude(), location.getLongitude());
                        }
                        // Always call New Location data because FusedLocationProviderClient hold previous location information.
                        Log.d(TAG, "getUserCurrentLocation: Request new Location")
                        requestNewLocationData()
                    }
                } else {
                    // Permission is not granted. Request for permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_ID
                    )
                }
            } else {
                val sweetAlertDialog =
                    SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                sweetAlertDialog.setTitleText("নির্দেশনা!!!")
                    .setContentText("আপনার ফোনের লোকেশন বন্ধ আছে । অনুগ্রহ করে লোকেশন এনাবল করুন ।")
                    .setConfirmText("এনাবল করুন")
                    .setConfirmClickListener { sDialog: SweetAlertDialog ->
                        Log.d(TAG, "onClick: User Agreed!")
                        val intent =
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                        sDialog.dismissWithAnimation()
                    }.show()
                sweetAlertDialog.setCancelable(false)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_ID
            )
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestNewLocationData() {
        AppProgressBar.showMessageProgress(context, "আপনার অবস্থান যাচাই করা হচ্ছে...")
        Log.d(TAG, "requestNewLocationData: Request new Location data ")
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = (5 * 1000).toLong()
        mLocationRequest.fastestInterval = 3000
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "requestNewLocationData: Request new Location data update")
            //Activate Looper
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            Looper.myLooper()?.let {
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    it
                )
            }
        } else {
            // Permission is not granted. Request for permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_ID
            )
        }
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.d(TAG, "onLocationResult: onComplete: " + mLastLocation.latitude)
            Log.d(TAG, "onLocationResult: onComplete: " + mLastLocation.longitude)
            val outletLat = mLastLocation.latitude
            val outletLong = mLastLocation.longitude

            //getAddress(outletLat, outletLong);
            Log.d(TAG, "onLocationResult: LAT: $outletLat")
            Log.d(TAG, "onLocationResult: LONG: $outletLong")
            if (isScanFaceCircleClicked) {
                checkSupervisorRadius(outletLat, outletLong)
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
            Log.d(TAG, "onLocationAvailability: " + locationAvailability.isLocationAvailable)
        }
    }

    private fun checkSupervisorRadius(userCurrentLat: Double, userCurrentLong: Double) {
        Log.d(TAG, "checkSupervisorRadius: User Current Lat: $userCurrentLat")
        Log.d(TAG, "checkSupervisorRadius: User Current Long: $userCurrentLong")
        val superVisorLat = userCredentialPreference!!.superVisorLatitude
        val superVisorLong = userCredentialPreference!!.superVisorLongitude

        Log.d(TAG, "checkSupervisorRadius: Assign Lat: $superVisorLat")
        Log.d(TAG, "checkSupervisorRadius: Assign Long: $superVisorLong")

        if (superVisorLat > 0.0 && superVisorLong > 0.0) {
            if (userCurrentLat > 0 && userCurrentLong > 0) {
                val startPoint = Location("locationA")
                startPoint.latitude = userCurrentLat
                startPoint.longitude = userCurrentLong
                val endPoint = Location("locationA")
                endPoint.latitude = superVisorLat.toDouble()
                endPoint.longitude = superVisorLong.toDouble()
                val distance = startPoint.distanceTo(endPoint).toDouble()
                Log.d(TAG, "checkOutletRadius: Distance in Meters: $distance")
                if (distance <= userCredentialPreference!!.superVisorRange) {
                    //AppProgressBar.hideMessageProgress();
                    Log.d(TAG, "checkOutletRadius: Distance in Meters: $distance")
                    getAddress(userCurrentLat, userCurrentLong)
                } else {
                    AppProgressBar.hideMessageProgress()
                    // mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    isScanFaceCircleClicked = false

                    //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    val sDialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    sDialog.setCancelable(false)
                    sDialog.setTitleText("আপনাকে অবশ্যই " + userCredentialPreference!!.superVisorWard.toString() + " নাম্বার ওয়ার্ড এর সীমার মধ্যে থাকতে হবে")
                        .setContentText(
                            userCredentialPreference!!.superVisorWard.toString() + " নাম্বার ওয়ার্ড এর সীমানা থেকে " + String.format(
                                Locale.getDefault(), "%.1f", distance
                            ) + " মিটার দূরে আছেন "
                        )
                        .setConfirmText("রিফ্রেশ")
                        .setCancelText("বাতিল")
                        .showCancelButton(false)
                        .setConfirmButtonBackgroundColor(Color.RED)
                        .setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->
                            Log.d(TAG, "onClick: Stay Here!")
                            //AppProgressBar.showMessageProgress(HomeActivity.this, "লোকেশন রিফ্রেশ হচ্ছে ... ");
                            isScanFaceCircleClicked = true
                            getUserCurrentLocation()
                            sweetAlertDialog.dismissWithAnimation()
                        }.show()
                }
            } else {
                AppProgressBar.hideMessageProgress()
                isScanFaceCircleClicked = false
                AppProgressBar.userAttentionPb(this, "আপনার লোকেশন পাওয়া যায়নি আবার চেষ্টা করুন ")
            }
        } else {
            AppProgressBar.hideMessageProgress()
            isScanFaceCircleClicked = false
            AppProgressBar.userAttentionPb(
                this,
                "সুপারভাইজার এর লোকেশন পাওয়া যায়নি সাপোর্ট এ যোগাযোগ করুন "
            )
        }
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        AppProgressBar.hideMessageProgress()
        Log.d(TAG, "getAddress: called")
        //AppProgressBar.hideMessageProgress();
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val addressSize = addresses.size
            if (addressSize > 0) {
                val sb = StringBuilder()
                addresses.forEachIndexed { index, item ->
                    println("index = $index, item = ${item.getAddressLine(index)} ")
                    sb.append(item.getAddressLine(index))
                }
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(AppConfig.ADDRESS, sb.toString())
                intent.putExtra(AppConfig.LATITUDE, latitude)
                intent.putExtra(AppConfig.LONGITUDE, longitude)
                mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
                startActivity(intent)
                finish()
            }
        } catch (e: IOException) {
            Log.d(TAG, "getAddress: IOException: " + e.message)
        }
    }

    override fun onBackPressed() {
        finish()
    }
}