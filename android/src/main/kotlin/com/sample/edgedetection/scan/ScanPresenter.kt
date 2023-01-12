package com.sample.edgedetection.scan

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Display
import android.view.SurfaceHolder
import android.widget.Toast
import com.sample.edgedetection.*
import com.sample.edgedetection.crop.CropActivity
import com.sample.edgedetection.processor.Corners
import com.sample.edgedetection.processor.processPicture
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_scan.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min
import android.util.Size as SizeB

class ScanPresenter(
    private val context: Context,
    private val iView: ScanActivity,
    private val initialBundle: Bundle
) :
    SurfaceHolder.Callback, Camera.PictureCallback, Camera.PreviewCallback {
    private val TAG: String = "ScanPresenter"
    private var mCamera: Camera? = null
    private val mSurfaceHolder: SurfaceHolder = iView.getSurfaceView().holder
    private val executor: ExecutorService
    private val proxySchedule: Scheduler
    private var busy: Boolean = false
    private var mCameraLensFacing: String? = null
    private var flashEnabled: Boolean = false;
    private var mLastClickTime = 0L
    private var shutted: Boolean = true

    var images = ArrayList<Bitmap>()

    init {
        mSurfaceHolder.addCallback(this)
        executor = Executors.newSingleThreadExecutor()
        proxySchedule = Schedulers.from(executor)
    }

    //???????
    private fun isOpenRecently(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 300) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    //start camera
    fun start() {
        mCamera?.startPreview() ?: Log.i(TAG, "camera null")

    }

    //stop camera
    fun stop() {
        mCamera?.stopPreview() ?: Log.i(TAG, "camera null")
    }

    val canShut: Boolean get() = shutted

    // to shut photo
    fun shut() {
        if (isOpenRecently()) {
            return
        }

        mCamera?.autoFocus { b, _ ->
//            for production enableShutterSound should be true
            mCamera?.enableShutterSound(false)
            mCamera?.takePicture(null, null, this,this )
        }
        busy = false
        shutted = true

    }

    // turns on flash
    fun toggleFlash() {
        try {
            flashEnabled = !flashEnabled;
            val parameters = mCamera?.parameters;
            parameters?.flashMode =
                if (flashEnabled) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            mCamera?.setParameters(parameters);
            mCamera?.startPreview();
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //??
    fun updateCamera() {
        if (null == mCamera) {
            return
        }
        mCamera?.stopPreview()
        try {
            mCamera?.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        mCamera?.setPreviewCallback(this)
        mCamera?.startPreview()
    }

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private fun getCameraCharacteristics(id: String): CameraCharacteristics? {
        return cameraManager.getCameraCharacteristics(id)
    }

    private fun getBackFacingCameraId(): String? {
        for (camID in cameraManager.cameraIdList) {
            val lensFacing =
                getCameraCharacteristics(camID)?.get(CameraCharacteristics.LENS_FACING)!!
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                mCameraLensFacing = camID
                break
            }
        }
        return mCameraLensFacing
    }

    // initializing the camera
    fun initCamera() {

        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        } catch (e: RuntimeException) {
            e.stackTrace
            Toast.makeText(context, "cannot open camera, please grant camera", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val cameraCharacteristics =
            cameraManager.getCameraCharacteristics(getBackFacingCameraId()!!)

        val param = mCamera?.parameters
        val availble_res = getOptimalResolution()

        //val size = getMaxResolution()

        val size = iView.getCurrentDisplay()?.let {
            getPreviewOutputSize(
                it, cameraCharacteristics, SurfaceHolder::class.java
            )
        }
        // Log.d(TAG, "View finder size: ${viewFinder.width} x ${viewFinder.height}")
        Log.d(TAG, "Selected preview size: ${size?.width}${size?.height}")
        // viewFinder.setAspectRatio(previewSize.width, previewSize.height)


        size?.width?.toString()?.let { Log.i(TAG, it) }
        param?.setPreviewSize(size?.width ?: 1920, size?.height ?: 1080)
        val display = iView.getCurrentDisplay()
        val point = Point()

        display?.getRealSize(point)

        val displayWidth = minOf(point.x, point.y)
        val displayHeight = maxOf(point.x, point.y)
        val displayRatio = displayWidth.div(displayHeight.toFloat())
        val previewRatio = size?.height?.toFloat()?.div(size.width.toFloat()) ?: displayRatio
        if (displayRatio > previewRatio) {
            val surfaceParams = iView.getSurfaceView().layoutParams
            surfaceParams.height = (displayHeight / displayRatio * previewRatio).toInt()
            iView.getSurfaceView().layoutParams = surfaceParams
        }

        val supportPicSize = mCamera?.parameters?.supportedPictureSizes
        supportPicSize?.sortByDescending { it.width.times(it.height) }
        var pictureSize = supportPicSize?.find {
            it.height.toFloat().div(it.width.toFloat()) - previewRatio < 0.01
        }

        if (null == pictureSize) {
            pictureSize = supportPicSize?.get(0)
        }

        if (null == pictureSize) {
            Log.e(TAG, "can not get picture size")
        } else {
            param?.setPictureSize(pictureSize.width, pictureSize.height)
        }
        val pm = context.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS) && mCamera!!.parameters.supportedFocusModes.contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            )
        ) {
            param?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            Log.d(TAG, "enabling autofocus")
        } else {
            Log.d(TAG, "autofocus not available")
        }
        param?.flashMode = Camera.Parameters.FLASH_MODE_OFF

        mCamera?.parameters = param
        mCamera?.setDisplayOrientation(90)
        mCamera?.enableShutterSound(false)

    }
    // this func must take image path and detect the edge of image and save the image again it`s path
    fun detectEdge(pic: Mat) {
        SourceManager.corners = processPicture(pic)
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA)
        SourceManager.pic = pic

        val cropIntent = Intent(context, CropActivity::class.java);
        cropIntent.putExtra(EdgeDetectionHandler.INITIAL_BUNDLE, this.initialBundle)
        (context as Activity).startActivityForResult(cropIntent, REQUEST_CODE);


        images.clear()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        initCamera()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        updateCamera()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        synchronized(this) {
            mCamera?.stopPreview()
            mCamera?.setPreviewCallback(null)
            mCamera?.release()
            mCamera = null
        }
    }


    // when picture taken this func fires
    @SuppressLint("CheckResult")
    override fun onPictureTaken(p0: ByteArray?, p1: Camera?) {
        Observable.just(p0)
            .subscribeOn(proxySchedule)
            .subscribe {
                val pictureSize = p1?.parameters?.pictureSize
                val mat = Mat(
                    Size(
                        pictureSize?.width?.toDouble() ?: 1920.toDouble(),
                        pictureSize?.height?.toDouble() ?: 1080.toDouble()
                    ), CvType.CV_8U
                )
                mat.put(0, 0, p0)
                val pic = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
                Core.rotate(pic, pic, Core.ROTATE_90_CLOCKWISE)
                mat.release()
                images.add(convertMatToBitMap(pic))
                shutted = true;
                busy = false
                stop()
                start()
                if(images.size==5){
                    stop()
                    val file1 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO1) as String);
                    val file2 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO2) as String);
                    val file3 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO3) as String);
                    val file4 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO4) as String);
                    val file5 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO5) as String);
                    val outStream1 = FileOutputStream(file1)
                    val outStream2 = FileOutputStream(file2)
                    val outStream3 = FileOutputStream(file3)
                    val outStream4 = FileOutputStream(file4)
                    val outStream5 = FileOutputStream(file5)
                    images[0].compress(Bitmap.CompressFormat.JPEG, 100, outStream1)
                    images[1].compress(Bitmap.CompressFormat.JPEG, 100, outStream2)
                    images[2].compress(Bitmap.CompressFormat.JPEG, 100, outStream3)
                    images[3].compress(Bitmap.CompressFormat.JPEG, 100, outStream4)
                    images[4].compress(Bitmap.CompressFormat.JPEG, 100, outStream5)
                    outStream1.flush()
                    outStream2.flush()
                    outStream3.flush()
                    outStream4.flush()
                    outStream5.flush()
                    outStream1.close()
                    outStream2.close()
                    outStream3.close()
                    outStream4.close()
                    outStream5.close()
                    images[0].recycle()
                    images[1].recycle()
                    images[2].recycle()
                    images[3].recycle()
                    images[4].recycle()
                    val bundle = Bundle();
                    val fiveImageIntent = Intent(context, FiveImageActivity::class.java)
                    fiveImageIntent.putExtra("fiveImageBundle", bundle)

                    fiveImageIntent.putExtra("image1", initialBundle.getString(EdgeDetectionHandler.SAVE_TO1) as String)
                    fiveImageIntent.putExtra("image2", initialBundle.getString(EdgeDetectionHandler.SAVE_TO2) as String)
                    fiveImageIntent.putExtra("image3", initialBundle.getString(EdgeDetectionHandler.SAVE_TO3) as String)
                    fiveImageIntent.putExtra("image4", initialBundle.getString(EdgeDetectionHandler.SAVE_TO4) as String)
                    fiveImageIntent.putExtra("image5", initialBundle.getString(EdgeDetectionHandler.SAVE_TO5) as String)
                    (context as Activity).startActivity(fiveImageIntent);

//                                            detectEdge(pic)
//                    val file = File(initialBundle.getString(EdgeDetectionHandler.IMAGE_LIST) as String);

                }
//                images.clear()
            }
    }
    private fun convertMatToBitMap(input: Mat): Bitmap {
        var bmp: Bitmap
        val rgb = Mat()
        Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB)
//        try {
            bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(rgb, bmp)
//        } catch (e: CvException) {
//            Log.i(com.sample.edgedetection.processor.TAG, e.message.toString())
//        }
        return bmp
    }

    @SuppressLint("CheckResult")
    override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
        if (busy) {
            return
        }
        Log.i(TAG, "on process start")
        busy = true
        try {
            Observable.just(p0)
                .observeOn(proxySchedule)
                .doOnError {}
                .subscribe({
                    Log.i(TAG, "start prepare paper")
                    val parameters = p1?.parameters
                    val width = parameters?.previewSize?.width
                    val height = parameters?.previewSize?.height
                    val yuv = YuvImage(
                        p0, parameters?.previewFormat ?: 0, width ?: 1080, height
                            ?: 1920, null
                    )
                    val out = ByteArrayOutputStream()
                    yuv.compressToJpeg(Rect(0, 0, width ?: 1080, height ?: 1920), 100, out)
                    val bytes = out.toByteArray()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val img = Mat()
                    Utils.bitmapToMat(bitmap, img)
                    bitmap.recycle()
                    Core.rotate(img, img, Core.ROTATE_90_CLOCKWISE)
                    try {
                        out.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    Observable.create<Corners> {
                        val corner = processPicture(img)
                        busy = false
                        if (null != corner && corner.corners.size == 4) {
                            it.onNext(corner)
                        } else {
                            it.onError(Throwable("paper not detected"))
                        }
                    }.observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            iView.getPaperRect().onCornersDetected(it)

                        }, {
                            iView.getPaperRect().onCornersNotDetected()
                        })
                }, { throwable -> Log.e(TAG, throwable.message!!) })
        } catch (e: Exception) {
            print(e.message)
        }

    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */

    class SmartSize(width: Int, height: Int) {
        var size = SizeB(width, height)
        var long = max(size.width, size.height)
        var short = min(size.width, size.height)
        override fun toString() = "SmartSize(${long}x${short})"
    }

    /** Standard High Definition size for pictures and video */
    private val SIZE_1080P: SmartSize = SmartSize(1920, 1080)

    /** Returns a [SmartSize] object for the given [Display] */
    private fun getDisplaySmartSize(display: Display): SmartSize {
        val outPoint = Point()
        display.getRealSize(outPoint)
        return SmartSize(outPoint.x, outPoint.y)
    }

    /**
     * Returns the largest available PREVIEW size. For more information, see:
     * https://d.android.com/reference/android/hardware/camera2/CameraDevice and
     * https://developer.android.com/reference/android/hardware/camera2/params/StreamConfigurationMap
     */
    private fun <T> getPreviewOutputSize(
        display: Display,
        characteristics: CameraCharacteristics,
        targetClass: Class<T>,
        format: Int? = null
    ): SizeB {

        // Find which is smaller: screen or 1080p
        val screenSize = getDisplaySmartSize(display)
        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
        val maxSize = if (hdScreen) SIZE_1080P else screenSize

        // If image format is provided, use it to determine supported sizes; else use target class
        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!
        if (format == null)
            assert(StreamConfigurationMap.isOutputSupportedFor(targetClass))
        else
            assert(config.isOutputSupportedFor(format))
        val allSizes = if (format == null)
            config.getOutputSizes(targetClass) else config.getOutputSizes(format)

        // Get available sizes and sort them by area from largest to smallest
        val validSizes = allSizes
            .sortedWith(compareBy { it.height * it.width })
            .map { SmartSize(it.width, it.height) }.reversed()

        // Then, get the largest output size that is smaller or equal than our max size
        return validSizes.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
    }

    private fun getOptimalResolution(): Camera.Size? {


        val resolutions = mCamera?.parameters?.supportedPreviewSizes
        if (resolutions != null) {
            for (item in resolutions) {
                println("${item.width}, ${item.height}")
            }
        }
        return null

    }

}


