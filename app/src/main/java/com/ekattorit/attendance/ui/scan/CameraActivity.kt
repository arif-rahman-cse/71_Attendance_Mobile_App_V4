package com.ekattorit.attendance.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Camera
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
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.ekattorit.attendance.*
import com.ekattorit.attendance.databinding.ActivityCameraBinding
import com.ekattorit.attendance.retrofit.RetrofitClient
import com.ekattorit.attendance.ui.MainActivity
import com.ekattorit.attendance.ui.scan.model.RpNewScan2
import com.ekattorit.attendance.utils.AppConfig.*
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor
import com.google.android.material.snackbar.Snackbar
import com.ttv.face.FaceEngine
import com.ttv.face.FaceFeature
import com.ttv.face.FaceResult
import com.ttv.face.SearchResult
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import io.fotoapparat.selector.front
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


    //Calculate face distance from camera
    var context: Context? = null
    var F:Float = 1f //focal length
    var sensorX:Float = 0f
    var sensorY:Float = 0f
    var angleX:Float = 0f
    var angleY:Float = 0f
    val IMAGE_WIDTH:Int = 1024
    val IMAGE_HEIGHT:Int = 1024
    val RIGHT_EYE:Int = 0
    val LEFT_EYE:Int = 1
    val AVERAGE_EYE_DISTANCE:Int = 63 // in mm

    private lateinit var cameraSource: CameraSource
    private var isValidDistance:Boolean = false



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
                    //binding.animationView.visibility = View.VISIBLE
                    binding.status.text = "Face Match"
                    binding.status.setTextColor(Color.GREEN)
                    //Calculate the distance of face
                    //binding.messageView.visibility = View.VISIBLE
                    frontFotoapparat!!.stop()
                    //Toast.makeText(applicationContext, "Detection has stopped!", Toast.LENGTH_SHORT).show()
                    calculateFaceDistance()


                } else {
                    //binding.animationView.visibility = View.GONE
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
                    val successMessage: String = response.body()!!.message
                    showConfirmation(successMessage)
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
            binding.mainView, msg,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction("ঠিক আছে") {

            }
        bar.show()
        frontFotoapparat!!.start()
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
            .lensPosition(front())
            .frameProcessor(SampleFrameProcessor())
            .previewResolution { Resolution(1280, 720) }
            .build()

        binding.btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
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
            val faceResults: List<FaceResult> = FaceEngine.getInstance(appCtx).detectFace(frame.image, frame.size.width, frame.size.height)
            if (faceResults.isNotEmpty()) {
                FaceEngine.getInstance(appCtx).livenessProcess(frame.image, frame.size.width, frame.size.height, faceResults)
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
                FaceEngine.getInstance(appCtx).extractFeature(nv21Data, width, height, false, faceResults)
                val result: SearchResult = FaceEngine.getInstance(appCtx).searchFaceFeature(FaceFeature(faceResults[0].feature))
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


    private fun calculateFaceDistance() {
        Log.d(TAG, "calculateFaceDistance: Called")
        val camera: Camera = frontCam()
        val campar = camera.parameters
        F = campar.focalLength
        angleX = campar.horizontalViewAngle
        angleY = campar.verticalViewAngle
        sensorX = (kotlin.math.tan(Math.toRadians((angleX / 2).toDouble())) * 2 * F).toFloat()
        sensorY = (kotlin.math.tan(Math.toRadians((angleY / 2).toDouble())) * 2 * F).toFloat()
        camera.stopPreview()
        camera.release()
        Log.d(TAG,
            "calculateFaceDistance: F: $F angleX$angleX angleY$angleY sensorX$sensorX sensorY$sensorY"
        )
        //textView = findViewById(R.id.text)
        createCameraSource()

    }

    private fun frontCam(): Camera {
        var cameraCount = 0
        var cam: Camera? = null
        val cameraInfo = Camera.CameraInfo()
        cameraCount = Camera.getNumberOfCameras()
        for (camIdx in 0 until cameraCount) {
            Camera.getCameraInfo(camIdx, cameraInfo)
            Log.v("CAMID", camIdx.toString() + "")
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx)
                } catch (e: RuntimeException) {
                    Log.e("FAIL", "Camera failed to open: " + e.localizedMessage)
                }
            }
        }
        Log.d(TAG, "frontCam: "+cam)
        return cam!!
    }

    private fun createCameraSource() {
        Log.d(TAG, "createCameraSource: Called")
        val detector = FaceDetector.Builder(this)
            .setTrackingEnabled(true)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.FAST_MODE)
            .build()
        detector.setProcessor(LargestFaceFocusingProcessor(detector, FaceTracker()))
        cameraSource = CameraSource.Builder(this, detector)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build()
        println(cameraSource.previewSize)
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            cameraSource.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inner class FaceTracker  constructor() :
        Tracker<Face>() {
        override fun onUpdate(detections: Detector.Detections<Face>, face: Face) {
            Log.d(TAG, "onUpdate: Called")
            val leftEyePos = face.landmarks[LEFT_EYE].position
            val rightEyePos = face.landmarks[RIGHT_EYE].position
            val deltaX = Math.abs(leftEyePos.x - rightEyePos.x)
            val deltaY = Math.abs(leftEyePos.y - rightEyePos.y)
            val distance: Float
            distance = if (deltaX >= deltaY) {
                F * (AVERAGE_EYE_DISTANCE / sensorX) * (IMAGE_WIDTH / deltaX)
            } else {
                F * (AVERAGE_EYE_DISTANCE / sensorY) * (IMAGE_HEIGHT / deltaY)
            }

            showStatus(distance)
        }

        override fun onMissing(detections: Detector.Detections<Face>) {
            Log.d(TAG, "onMissing: Called")
            super.onMissing(detections)
            //showStatus("face not detected")
        }

        override fun onDone() {
            super.onDone()
        }
    }

    fun showStatus(distance: Float) {
        runOnUiThread {
            Log.d(TAG,
                "showStatus: Distance: $distance"
            )

            if (distance > 700) {
                insertAttendance(recogEmployeeId)


            }else{
                binding.messageView.visibility = View.VISIBLE
                binding.tvInfo.text = getString(R.string.distance_warning)
                cameraSource.stop()

            }
        }
    }


}