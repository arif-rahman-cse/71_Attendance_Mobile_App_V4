package com.ttv.facerecog.ui
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.ttv.face.FaceEngine
import com.ttv.face.FaceFeatureInfo
import com.ttv.face.FaceResult
import com.ttv.facerecog.*
import com.ttv.facerecog.databinding.ActivityMainBinding
import com.ttv.facerecog.ui.report.AttendanceSummary
import com.ttv.facerecog.ui.report.DailyAttendanceStatus
import com.ttv.facerecog.ui.report.SingleRangeAttendance
import com.ttv.facerecog.ui.scan.CameraActivity
import com.ttv.facerecog.utils.AppConfig
import com.ttv.facerecog.utils.EmployeeFacePreference
import com.ttv.facerecog.utils.UserCredentialPreference
import java.text.MessageFormat

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    companion object {
        lateinit var userLists: ArrayList<FaceEntity>
        private const val TAG = "MainActivity"
    }
    private lateinit var binding: ActivityMainBinding
    var userCredentialPreference: UserCredentialPreference? = null
    var employeeFacePreference: EmployeeFacePreference? = null
    private var context: Context? = null
    private var mydb: DBHelper? = null

    init {
        userLists = ArrayList(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        employeeFacePreference = EmployeeFacePreference.getPreferences(this)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.navigationDrawer.setNavigationItemSelectedListener(this)
        binding.tvTotalScanCard.text =
            MessageFormat.format(
                "মোট স্ক্যান হয়েছে {0} জন",
                userCredentialPreference!!.totalScan
            )

        context = this
        FaceEngine.getInstance(this).setActivation("")
        FaceEngine.getInstance(this).init(2)
        mydb = DBHelper(this)
        mydb!!.getAllUsers()

        //val btnRegister = findViewById<Button>(R.id.btnRegister)
        binding.btnRegister.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_PICK)
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
        }

        //val btnVerify = findViewById<Button>(R.id.btnVerify)
        binding.cvScanCircle.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, 2)
        }
        binding.btnVerify.isEnabled = false;

        //val btnUsers = findViewById<Button>(R.id.btnUser)
        binding.btnUser.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }

        binding.drawerMenuBtn.setOnClickListener { v ->
            val userName = findViewById<TextView>(R.id.tv_name)
            val phone = findViewById<TextView>(R.id.tv_phone)
            val profileImage =
                findViewById<ImageView>(R.id.circleImageView)
            userName.text = userCredentialPreference!!.name
            phone.text = userCredentialPreference!!.userPhone
            Log.d(TAG, "onCreate: URL: " + AppConfig.Base_URL_ONLINE_IMG + userCredentialPreference!!.profileUrl
            )
            Glide.with(this)
                .load(AppConfig.Base_URL_ONLINE_IMG.toString() + "/media/" + userCredentialPreference!!.profileUrl)
                .placeholder(R.drawable.loading_01)
                .centerInside()
                .error(R.drawable.default_profile_pic)
                .into(profileImage)
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onResume() {
        super.onResume()

        findViewById<Button>(R.id.btnVerify).isEnabled = userLists.size > 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                var bitmap: Bitmap = ImageRotator.getCorrectlyOrientedImage(this, data?.data!!)
                val faceResults:MutableList<FaceResult> = FaceEngine.getInstance(this).detectFace(bitmap)
                if(faceResults.count() == 1) {
                    FaceEngine.getInstance(this).extractFeature(bitmap, true, faceResults)

                    val userName = String.format("User%03d", userLists.size + 1)
                    val cropRect =
                        Utils.getBestRect(bitmap.width, bitmap.height, faceResults.get(0).rect)
                    val headImg = Utils.crop(
                        bitmap,
                        cropRect.left,
                        cropRect.top,
                        cropRect.width(),
                        cropRect.height(),
                        120,
                        120
                    )

                    val inputView = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_input_view, null, false)
                    val editText = inputView.findViewById<EditText>(R.id.et_user_name)
                    val ivHead = inputView.findViewById<ImageView>(R.id.iv_head)
                    ivHead.setImageBitmap(headImg)
                    editText.setText(userName)
                    val confirmUpdateDialog: AlertDialog = AlertDialog.Builder(context!!)
                        .setView(inputView)
                        .setPositiveButton(
                            "OK", null
                        )
                        .setNegativeButton(
                            "Cancel", null
                        )
                        .create()
                    confirmUpdateDialog.show()
                    confirmUpdateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener { v: View? ->
                            val s = editText.text.toString()
                            if (TextUtils.isEmpty(s)) {
                                editText.error = application.getString(R.string.name_should_not_be_empty)
                                return@setOnClickListener
                            }

                            var exists:Boolean = false
                            for(user in userLists) {
                                if(TextUtils.equals(user.userName, s)) {
                                    exists = true
                                    break
                                }
                            }

                            if(exists) {
                                editText.error = application.getString(R.string.duplicated_name)
                                return@setOnClickListener
                            }

                            val user_id = mydb!!.insertUser(s, headImg, faceResults.get(0).feature)
                            val face = FaceEntity(user_id, s, headImg, faceResults.get(0).feature)
                            userLists.add(face)

                            val faceFeatureInfo = FaceFeatureInfo(
                                user_id,
                                faceResults.get(0).feature
                            )

                            FaceEngine.getInstance(this).registerFaceFeature(faceFeatureInfo)

                            confirmUpdateDialog.cancel()

                            findViewById<Button>(R.id.btnVerify).isEnabled = userLists.size > 0
                            Toast.makeText(this, "Register succeed!", Toast.LENGTH_SHORT).show()
                        }

                } else if(faceResults.count() > 1) {
                    Toast.makeText(this, "Multiple face detected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No face detected!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                //handle exception
                e.printStackTrace()
            }
        } else if(requestCode == 2 && resultCode == RESULT_OK) {
            val verifyResult = data!!.getIntExtra ("verifyResult", 0)
            val recogName = data.getStringExtra ("verifyName")
            if(verifyResult == 1) {
                Toast.makeText(this, "Verify succeed! " + recogName, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Verify failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.log_out) {
            logout()
            return true
        } else if (id == R.id.daily_attendance_report) {
            val intent = Intent(this, DailyAttendanceStatus::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.single_attendance_report) {
            val intent = Intent(this, SingleRangeAttendance::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.attendance_summery) {
            val intent = Intent(this, AttendanceSummary::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.support_number) {
            val intent = Intent(this, SupportActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.id_add_new_face) {
            closeDrawer()
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
            //finish();
            return true
        } else if (id == R.id.sync_face) {
            //closeDrawer();
            //syncFace()
            //finish();
            return true
        }

        return false
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun logout() {
        TODO("Not yet implemented")
    }
}