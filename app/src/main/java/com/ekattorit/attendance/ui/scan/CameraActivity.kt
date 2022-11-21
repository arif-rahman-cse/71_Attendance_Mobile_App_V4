package com.ekattorit.attendance.ui.scan

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ekattorit.attendance.*
import com.ekattorit.attendance.databinding.ActivityCameraBinding
import com.ekattorit.attendance.retrofit.RetrofitClient
import com.ekattorit.attendance.ui.MainActivity
import com.ekattorit.attendance.ui.scan.model.RpNewScan2
import com.ekattorit.attendance.utils.AppConfig.*
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.material.snackbar.Snackbar
import com.ttv.face.FaceEngine
import com.ttv.face.FaceFeature
import com.ttv.face.FaceResult
import com.ttv.face.SearchResult
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import io.fotoapparat.selector.back
import io.fotoapparat.util.FrameProcessor
import io.fotoapparat.view.CameraView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"

    var userCredentialPreference: UserCredentialPreference? = null
    private lateinit var binding: ActivityCameraBinding
    private val permissionsDelegate = PermissionsDelegate(this)
    private var hasPermission = false

    private var appCtx: Context? = null
    private var cameraView: CameraView? = null
    private var rectanglesView: FaceRectView? = null
    private var faceRectTransformer: FaceRectTransformer? = null
    private var frontFotoapparat: Fotoapparat? = null
    private var startVerifyTime: Long = 0
    private var mydb: DBHelper? = null
    private var recogName: String = ""
    private var recogEmployeeId: String = ""

    private var supervisorCurrentAddress: String? = null
    private var latitude: Double = 0.0;
    private var longitude: Double = 0.0


    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val i: Int = msg.what
            //val j : Any = msg.obj


            if (i == 0) {
                val drawInfoList = ArrayList<FaceRectView.DrawInfo>();
                val detectionResult = msg.obj as ArrayList<FaceResult>

                for (faceResult in detectionResult) {
                    val rect: Rect = faceRectTransformer!!.adjustRect(faceResult.rect)
                    var drawInfo: FaceRectView.DrawInfo

                    if (faceResult.liveness == 1)
                        drawInfo = FaceRectView.DrawInfo(rect, 0, 0, 1, Color.GREEN, null);
                    else
                        drawInfo = FaceRectView.DrawInfo(rect, 0, 0, 0, Color.RED, null);

                    drawInfoList.add(drawInfo)
                }

                rectanglesView!!.clearFaceInfo()
                rectanglesView!!.addFaceInfo(drawInfoList)
            } else if (i == 1) {
                val verifyResult = msg.obj as Int
//                val intent = Intent()
//                intent.putExtra("verifyResult", verifyResult);
//                intent.putExtra("verifyName", recogName)
//                setResult(RESULT_OK, intent)
//                finish()
                if (verifyResult == 1) {
                    binding.animationView.visibility = View.VISIBLE
                    binding.status.text = "Face Match"
                    binding.status.setTextColor(Color.GREEN)
                    insertAttendance(recogEmployeeId)


                } else {
                    binding.animationView.visibility = View.GONE
                    binding.status.text = "Face Match Failed"
                    binding.status.setTextColor(Color.RED)


                }
            }
        }
    }

    private fun insertAttendance(employeeId: String) {
        //Log.d(TAG, "insertAttendance: Insert Attendance")

        val newScanCall: Call<RpNewScan2> = RetrofitClient
            .getInstance()
            .api
            .addNewAttendance(
                userCredentialPreference!!.userToken,
                employeeId,
                latitude,
                longitude,
                supervisorCurrentAddress,
                userCredentialPreference!!.userId
            )

        newScanCall.enqueue(object : Callback<RpNewScan2?> {
            override fun onResponse(call: Call<RpNewScan2?>, response: Response<RpNewScan2?>) {
                Log.d(TAG, "onResponse: Code: " + response.code())
                //Log.d(TAG, "onResponse: data: "+ response.body().toString());
                if (response.code() == 201 || response.code() == 200) {
                    assert(response.body() != null)
                    val employeeName: String = response.body()!!.employeeName
                    showConfirmation(employeeName)
                } else {
                    try {
                        Log.d(TAG, "onResponse: Code: " + response.errorBody()!!.string())

                        Toast.makeText(
                            applicationContext,
                            " কিছু একটা সমস্যা হয়েছে " + response.errorBody()!!.string(),
                            Toast.LENGTH_SHORT
                        ).show()
                        //showConfirmation(employeeName)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<RpNewScan2?>, t: Throwable) {
                Toast.makeText(
                    applicationContext,
                    " কিছু একটা সমস্যা হয়েছে " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "onFailure: Error: " + t.message)
            }
        })


    }

    private fun showConfirmation(msg: String) {

        val bar = Snackbar.make(
            binding.mainView,
            "$msg এর উপস্থিতি নিশ্চিত হয়েছে।",
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("OK") {

            }
        bar.show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true);
        supportActionBar!!.title = getString(R.string.live_face_verification)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)

        supervisorCurrentAddress = intent.getStringExtra(ADDRESS)
        latitude = intent.getDoubleExtra(LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(LONGITUDE, 0.0)

        appCtx = applicationContext
        cameraView = findViewById<View>(R.id.camera_view) as CameraView
        rectanglesView = findViewById<View>(R.id.rectanglesView) as FaceRectView

        mydb = DBHelper(appCtx)

        hasPermission = permissionsDelegate.hasPermissions()
        if (hasPermission) {
            cameraView!!.visibility = View.VISIBLE
        } else {
            permissionsDelegate.requestPermissions()
        }

        frontFotoapparat = Fotoapparat.with(this)
            .into(cameraView!!)
            .lensPosition(back())
            .frameProcessor(SampleFrameProcessor())
            .previewResolution { Resolution(1280, 720) }
            .build()
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
        return true
    }

    override fun onStart() {
        super.onStart()
        if (hasPermission) {
            frontFotoapparat!!.start()
        }
    }


    override fun onStop() {
        super.onStop()
        if (hasPermission) {
            try {
                frontFotoapparat!!.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionsDelegate.hasPermissions() && !hasPermission) {
            hasPermission = true
            cameraView!!.visibility = View.VISIBLE
            frontFotoapparat!!.start()
        } else {
            permissionsDelegate.requestPermissions()
        }
    }

    fun adjustPreview(frameWidth: Int, frameHeight: Int, rotation: Int): Boolean {
        if (faceRectTransformer == null) {
            val frameSize: Size = Size(frameWidth, frameHeight);
            if (cameraView!!.measuredWidth == 0)
                return false;

            adjustPreviewViewSize(cameraView!!, rectanglesView!!);

            faceRectTransformer = FaceRectTransformer(
                frameSize.width, frameSize.height,
                cameraView!!.layoutParams.width, cameraView!!.layoutParams.height,
                rotation, 0, false,
                false,
                false
            );

            return true;
        }

        return true;
    }

    private fun adjustPreviewViewSize(
        previewView: View,
        faceRectView: FaceRectView,
    ): ViewGroup.LayoutParams? {
        val layoutParams = previewView.layoutParams
        val measuredWidth = previewView.measuredWidth
        val measuredHeight = previewView.measuredHeight
        layoutParams.width = measuredWidth
        layoutParams.height = measuredHeight

        faceRectView.layoutParams.width = measuredWidth
        faceRectView.layoutParams.height = measuredHeight
        return layoutParams
    }

    /* access modifiers changed from: private */ /* access modifiers changed from: public */
    private fun sendMessage(w: Int, o: Any) {
        val message = Message()
        message.what = w
        message.obj = o
        mHandler.sendMessage(message)
    }

    inner class SampleFrameProcessor : FrameProcessor {
        var frThreadQueue: LinkedBlockingQueue<Runnable>? = null
        var frExecutor: ExecutorService? = null

        init {
            frThreadQueue = LinkedBlockingQueue<Runnable>(1)
            frExecutor = ThreadPoolExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS, frThreadQueue
            ) { r: Runnable? ->
                val t = Thread(r)
                t.name = "frThread-" + t.id
                t
            }
        }

        override fun invoke(frame: Frame) {
            val faceResults: List<FaceResult> = FaceEngine.getInstance(appCtx)
                .detectFace(frame.image, frame.size.width, frame.size.height)
            if (faceResults.isNotEmpty()) {
                FaceEngine.getInstance(appCtx)
                    .livenessProcess(frame.image, frame.size.width, frame.size.height, faceResults)
                if (frThreadQueue!!.remainingCapacity() > 0) {
                    frExecutor!!.execute(
                        FaceRecognizeRunnable(
                            frame.image,
                            frame.size.width,
                            frame.size.height,
                            faceResults
                        )
                    )
                }
            }
            if (adjustPreview(frame.size.width, frame.size.height, frame.rotation))
                sendMessage(0, faceResults)

        }
    }

    inner class FaceRecognizeRunnable(
        nv21Data_: ByteArray,
        width_: Int,
        height_: Int,
        faceResults_: List<FaceResult>
    ) : Runnable {
        val nv21Data: ByteArray
        val width: Int
        val height: Int
        val faceResults: List<FaceResult>

        init {
            nv21Data = nv21Data_
            width = width_
            height = height_
            faceResults = faceResults_
        }

        override fun run() {
            if (startVerifyTime == 0.toLong())
                startVerifyTime = System.currentTimeMillis()

            var exists = false
            try {
                FaceEngine.getInstance(appCtx)
                    .extractFeature(nv21Data, width, height, false, faceResults)
                val result: SearchResult = FaceEngine.getInstance(appCtx)
                    .searchFaceFeature(FaceFeature(faceResults[0].feature))
                if (result.maxSimilar > 0.8f) {
                    for (user in MainActivity.userLists) {
                        if (user.user_id == result.faceFeatureInfo!!.searchId && faceResults[0].liveness == 1) {
                            exists = true
                            recogName = user.userName
                            recogEmployeeId = user.employeeId
                        }
                    }
                }
            } catch (e: Exception) {
            }

            if (exists) {
                sendMessage(1, 1)   //success
                Thread.sleep(3000)
                //FaceRecognizeRunnable.postDelayed(this,1000)
            } else {
                if (System.currentTimeMillis() - startVerifyTime > 3000) {
                    sendMessage(1, 0)   //fail
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}