package com.ekattorit.attendance.ui.employee

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.ekattorit.attendance.DBHelper
import com.ekattorit.attendance.FaceEntity
import com.ekattorit.attendance.R
import com.ekattorit.attendance.Utils
import com.ekattorit.attendance.databinding.ActivityAddEmployeeFaceBinding
import com.ekattorit.attendance.retrofit.RetrofitClient
import com.ekattorit.attendance.ui.MainActivity.Companion.userLists
import com.ekattorit.attendance.ui.employee.modle.RpNewFace
import com.ekattorit.attendance.utils.AppConfig
import com.ekattorit.attendance.utils.AppProgressBar
import com.ekattorit.attendance.utils.UserCredentialPreference
import com.google.android.material.snackbar.Snackbar
import com.ttv.face.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class AddEmployeeFace : AppCompatActivity() {
    private var photoFile: File? = null
    private var photoFileCompress: File? = null
    private var mCurrentPhotoPath: String? = null
    private var binding: ActivityAddEmployeeFaceBinding? = null
    private var mydb: DBHelper? = null
    private var context: Context? = null
    private var empId: String? = null
    private var empName: String? = null
    private var bitmap: Bitmap? = null
    var userCredentialPreference: UserCredentialPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_employee_face)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_employee_face)
        userCredentialPreference = UserCredentialPreference.getPreferences(this)
        mydb = DBHelper(this)
        context = this
        val scanIntent = intent
        empId = scanIntent.getStringExtra(AppConfig.EMP_ID)
        empName = scanIntent.getStringExtra(AppConfig.EMP_NAME)

        val empId = scanIntent.getStringExtra(AppConfig.EMP_ID)
        val empName = scanIntent.getStringExtra(AppConfig.EMP_NAME)
        val empDesignation = scanIntent.getStringExtra(AppConfig.DESIGNATION)
        val wordNo = scanIntent.getStringExtra(AppConfig.WORD_NO)

        setScanData(
            empId,
            empName,
            empDesignation,
            wordNo
        )

        captureImage()

        binding!!.addNewFaceBtn.setOnClickListener {
            saveNewFace()
        }

        binding!!.btnClose.setOnClickListener {
            val intent = Intent(this, EmployeeListActivityV2::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveNewFace() {

        try {
            val faceResults: MutableList<FaceResult> =
                FaceEngine.getInstance(this).detectFace(bitmap)
            //val detectResult: List<FaceResult> = FaceEngine.getInstance(this).extractFeature(bitmap, isRegister, faceResults)
            if (faceResults.count() == 1) {
                var faceAlreadyExists = false
                var idAlreadyExists = false
                var username = ""
                FaceEngine.getInstance(this).extractFeature(bitmap, true, faceResults)
                //val result: SearchResult = FaceEngine.getInstance(this).searchFaceFeature(FaceFeature(faceResults[0].feature))
                if (userLists.isEmpty()) {
                    for (user in userLists) {
                        if (TextUtils.equals(user.employeeId, empId)) {
                            idAlreadyExists = true
                            break
                        }
                    }

                } else {
                    val result: SearchResult = FaceEngine.getInstance(context)
                        .searchFaceFeature(FaceFeature(faceResults[0].feature))
                    if (result.maxSimilar > 0.8f) {
                        for (user in userLists) {
                            if (user.user_id == result.faceFeatureInfo!!.searchId) {
                                faceAlreadyExists = true
                                username = user.userName
                                break
                            }
                            if (TextUtils.equals(user.employeeId, empId)) {
                                idAlreadyExists = true
                                break
                            }
                        }
                    }
                }


                val cropRect =
                    Utils.getBestRect(bitmap!!.width, bitmap!!.height, faceResults[0].rect)
                val headImg = Utils.crop(
                    bitmap,
                    cropRect.left,
                    cropRect.top,
                    cropRect.width(),
                    cropRect.height(),
                    120,
                    120
                )
                /*
                for (user in userLists) {
                    if (TextUtils.equals(user.employeeId, empId)) {
                        idAlreadyExists = true
                        break
                    }
                }

                 */
                if (faceAlreadyExists) {
                    showMessageIdAlreadyRegister("ইতিমধ্যে $username এর ফেইস যুক্ত আছে ।")
                } else if (idAlreadyExists) {
                    showMessageIdAlreadyRegister("ইতিমধ্যে এই কর্মীর আইডি যুক্ত আছে ।")

                } else {
                    //findViewById<Button>(R.id.btnVerify).isEnabled = MainActivity.userLists.size > 0
                    AppProgressBar.messageProgressFixed(context, "Face is Saving...")
                    addNewFace(
                        empId,
                        empName,
                        headImg,
                        faceResults[0].feature,
                        userCredentialPreference!!.userId
                    )
                }


            } else if (faceResults.count() > 1) {
                showMessage("একাধিক ফেইস শনাক্ত করা হয়েছে!")
                //Toast.makeText(this, "Multiple face detected!", Toast.LENGTH_SHORT).show()
            } else {
                showMessage("কোন ফেইস সনাক্ত করা যায়নি!")
                //Toast.makeText(this, "No face detected!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: java.lang.Exception) {
            //handle exception
            e.printStackTrace()
        }

    }


    private fun showMessage(msg: String) {

        val bar = Snackbar.make(binding!!.mainView, msg, Snackbar.LENGTH_INDEFINITE)
            .setAction("আবার চেষ্টা করুন") {
                captureImage()
            }
        bar.show()
    }

    private fun showMessageIdAlreadyRegister(msg: String) {

        val bar = Snackbar.make(binding!!.mainView, msg, Snackbar.LENGTH_INDEFINITE)
            .setAction("নতুন স্ক্যান করুন") {
                val intent = Intent(this, EmployeeListActivityV2::class.java)
                startActivity(intent)
                finish()
            }
        bar.show()
    }


    private fun setScanData(
        empId: String?,
        empName: String?,
        empDesignation: String?,
        wordNo: String?
    ) {
        binding!!.tvCardId.text = empId
        binding!!.tvCardOwner.text = empName
        binding!!.tvDesignation.text = empDesignation
        binding!!.tvWordNo.text = wordNo
    }


    private fun captureImage() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile()
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(
                            this,
                            "com.ttv.facedemo.fileprovider",
                            photoFile!!
                        )
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(baseContext, ex.message.toString())
                }

            } else {
                displayMessage(baseContext, "Null")
            }
        }

    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }


    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
            compressCaptureImage(bitmap)
        } else {
            displayMessage(baseContext, "Request cancelled or something went wrong.")
        }
    }

    private fun compressCaptureImage(actualBitmap: Bitmap?) {
        //Compressed Bitmap
        //val reduceBitmap = ImageResizer.reduceBitmapSize(actualBitmap, 2073600)
        binding!!.imageView.setImageBitmap(actualBitmap)
        photoFileCompress = getBitmapFile(actualBitmap)


    }

    fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    private fun getBitmapFile(reduceBitmap: Bitmap?): File {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        reduceBitmap!!.compress(Bitmap.CompressFormat.JPEG, 25, stream)
        stream.flush()
        stream.close()
        return file
    }


    private fun addNewFace(
        empId: String?,
        empName: String?,
        face: Bitmap,
        feature: ByteArray,
        supervisorId: Int
    ) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        face.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

        // image as file
        val requestBody =
            photoFileCompress!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val facePart =
            MultipartBody.Part.createFormData("face", photoFileCompress!!.name, requestBody)


        val addNewFaceCall =
            RetrofitClient.getInstance().api.addNewFace(empId, empName, supervisorId, facePart)
        addNewFaceCall.enqueue(object : Callback<RpNewFace?> {
            override fun onResponse(
                call: Call<RpNewFace?>,
                response: Response<RpNewFace?>
            ) {
                Log.d(TAG, "addNewFace: Code: " + response.code())
                if (response.code() == 201 && response.isSuccessful) {
                    val faceResponse = response.body()!!
                    Log.d(TAG, "onResponse: Response: " + faceResponse.empName)
                    Toast.makeText(context, " নতুন ফেইস নিবন্ধন সফল হয়েছে !", Toast.LENGTH_SHORT)
                        .show()

                    val user_id = mydb!!.insertUser(empName, empId, face, feature)
                    val _face = FaceEntity(user_id, empName, empId, face, feature)
                    userLists.add(_face)
                    val faceFeatureInfo = FaceFeatureInfo(user_id, feature)
                    FaceEngine.getInstance(context).registerFaceFeature(faceFeatureInfo)
                    AppProgressBar.hideMessageProgress()
                    val intent = Intent(context, EmployeeListActivityV2::class.java)
                    finish()
                    startActivity(intent)


                } else if (response.code() == 400) {
                    AppProgressBar.hideMessageProgress()
                    AppProgressBar.userAttentionPb(
                        context,
                        "এই কর্মীর ফেইসটি ইতিমধ্যে যুক্ত আছে । অনুগ্রহ করে ফেইস সিঙ্ক করুন।"
                    )

                } else {
                    AppProgressBar.hideMessageProgress()
                    try {
                        Toast.makeText(context, response.errorBody()!!.string(), Toast.LENGTH_SHORT)
                            .show()
                        Log.d(TAG, "addNewFace: Error: " + response.errorBody()!!.string())
                    } catch (e: IOException) {
                        // handle failure at error parse
                    }
                }
            }

            override fun onFailure(call: Call<RpNewFace?>, t: Throwable) {
                AppProgressBar.hideMessageProgress()
                Log.d(TAG, "addNewFace: Error")
                Log.e(TAG, "addNewFace: " + t.message)
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }


    companion object {
        private const val CAPTURE_IMAGE_REQUEST = 1
        private const val TAG = "AddEmployeeFace"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, EmployeeListActivityV2::class.java)
        startActivity(intent)
        finish()
    }
}