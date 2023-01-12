package com.sample.edgedetection.crop

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import com.sample.edgedetection.EdgeDetectionHandler
import com.sample.edgedetection.MainActivity
import com.sample.edgedetection.SourceManager
import com.sample.edgedetection.processor.Corners
import com.sample.edgedetection.processor.TAG
import com.sample.edgedetection.processor.cropPicture
import com.sample.edgedetection.processor.enhancePicture
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream


const val IMAGES_DIR = "smart_scanner"

class CropPresenter(
    private val context: Context,
    private val iCropView: ICropView.Proxy,
    private val initialBundle: Bundle
) {
    private val picture: Mat? = SourceManager.pic

    private val corners: Corners? = SourceManager.corners
    private var croppedPicture: Mat? = null
    private var enhancedPicture: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var rotateBitmap: Bitmap? = null
    private var rotateBitmapDegree: Int = -90

    fun onViewsReady(paperWidth: Int, paperHeight: Int) {
        iCropView.getPaperRect().onCorners2Crop(corners, picture?.size(), paperWidth, paperHeight)
        val bitmap = Bitmap.createBitmap(
            picture?.width() ?: 1080, picture?.height()
                ?: 1920, Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(picture, bitmap, true)
        iCropView.getPaper().setImageBitmap(bitmap)
    }

    //gets saved image and crops when image available, then crop activity handle it
    @SuppressLint("CheckResult")
    fun crop() {
        if (picture == null) {
            Log.i(TAG, "picture null?")
            return
        }

        if (croppedBitmap != null) {
            Log.i(TAG, "already cropped")
            return
        }

        Observable.create<Mat> {
            it.onNext(cropPicture(picture, iCropView.getPaperRect().getCorners2Crop()))
        }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pc ->
                Log.i(TAG, "cropped picture: $pc")
                croppedPicture = pc
                croppedBitmap =
                    Bitmap.createBitmap(pc.width(), pc.height(), Bitmap.Config.ARGB_8888)
                Utils.matToBitmap(pc, croppedBitmap)
                iCropView.getCroppedPaper().setImageBitmap(croppedBitmap)

//                iCropView.getPaper().visibility = View.GONE
//                iCropView.getPaperRect().visibility = View.GONE
            }

    }

    @SuppressLint("CheckResult")
    fun enhance() {
        if (croppedBitmap == null) {
            Log.i(TAG, "picture null?")
            return
        }

        val imgToEnhance: Bitmap? = when {
            enhancedPicture != null -> {
                enhancedPicture
            }
            rotateBitmap != null -> {
                rotateBitmap
            }
            else -> {
                croppedBitmap
            }
        }

        Observable.create<Bitmap> {
            it.onNext(enhancePicture(imgToEnhance))
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pc ->

                enhancedPicture = pc
                rotateBitmap = enhancedPicture

                iCropView.getCroppedPaper().setImageBitmap(pc)
            }
    }

    fun reset() {
        if (croppedBitmap == null) {
            Log.i(TAG, "picture null?")
            return
        }

        //croppedBitmap = croppedBitmap?.rotateInt(rotateBitmapDegree)

        rotateBitmap = croppedBitmap
        enhancedPicture = croppedBitmap

        iCropView.getCroppedPaper().setImageBitmap(croppedBitmap)
    }

    fun rotate() {
        if (croppedBitmap == null && enhancedPicture == null) {
            Log.i(TAG, "picture null?")
            return
        }

        if (enhancedPicture != null && rotateBitmap == null) {
            Log.i(TAG, "enhancedPicture ***** TRUE")
            rotateBitmap = enhancedPicture
        }

        if (rotateBitmap == null) {
            Log.i(TAG, "rotateBitmap ***** TRUE")
            rotateBitmap = croppedBitmap
        }

        Log.i(TAG, "ROTATEBITMAPDEGREE --> $rotateBitmapDegree")

        rotateBitmap = rotateBitmap?.rotateInt(rotateBitmapDegree)

        //rotateBitmap = rotateBitmap?.rotateFloat(rotateBitmapDegree.toFloat())

        iCropView.getCroppedPaper().setImageBitmap(rotateBitmap)

        enhancedPicture = rotateBitmap
        croppedBitmap = croppedBitmap?.rotateInt(rotateBitmapDegree)
    }


    fun save() {
        val file1 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO1) as String);
        val file2 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO2) as String);
        val file3 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO3) as String);
        val file4 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO4) as String);
        val file5 = File(initialBundle.getString(EdgeDetectionHandler.SAVE_TO5) as String);
        val rotatePic = rotateBitmap
//        if (null != rotatePic) {
//            Log.i("rotated picture", "rotated picture worked")
//
//            val outStream1 = FileOutputStream(file1)
//            val outStream2 = FileOutputStream(file2)
//            val outStream3 = FileOutputStream(file3)
//            val outStream4 = FileOutputStream(file4)
//            val outStream5 = FileOutputStream(file5)
//            rotatePic.compress(Bitmap.CompressFormat.JPEG, 100, outStream1)
//            rotatePic.compress(Bitmap.CompressFormat.JPEG, 100, outStream2)
//            rotatePic.compress(Bitmap.CompressFormat.JPEG, 100, outStream3)
//            rotatePic.compress(Bitmap.CompressFormat.JPEG, 100, outStream4)
//            rotatePic.compress(Bitmap.CompressFormat.JPEG, 100, outStream5)
//            outStream1.flush()
//            outStream2.flush()
//            outStream3.flush()
//            outStream4.flush()
//            outStream5.flush()
//            outStream1.close()
//            outStream2.close()
//            outStream3.close()
//            outStream4.close()
//            outStream5.close()
//            rotatePic.recycle()
//            Log.i(TAG, "RotateBitmap Saved")
//        } else {
//            //first save enhanced picture, if picture is not enhanced, save cropped picture, otherwise nothing to do
//            val pic = enhancedPicture
//
//            if (null != pic) {
//                Log.i("enhanced picture", "enhanced picture worked")
//                val outStream1 = FileOutputStream(file1)
//                val outStream2 = FileOutputStream(file2)
//                val outStream3 = FileOutputStream(file3)
//                val outStream4 = FileOutputStream(file4)
//                val outStream5 = FileOutputStream(file5)
//                pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream1)
//                pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream2)
//                pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream3)
//                pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream4)
//                pic.compress(Bitmap.CompressFormat.JPEG, 100, outStream5)
//                outStream1.flush()
//                outStream2.flush()
//                outStream3.flush()
//                outStream4.flush()
//                outStream5.flush()
//                outStream1.close()
//                outStream2.close()
//                outStream3.close()
//                outStream4.close()
//                outStream5.close()
//                pic.recycle()
//            } else {
//
//                val cropPic = croppedBitmap
//                if (null != cropPic) {
//                    Log.i("cropped picture", "cropped picture worked")
//
//                    val outStream1 = FileOutputStream(file1)
//                    val outStream2 = FileOutputStream(file2)
//                    val outStream3 = FileOutputStream(file3)
//                    val outStream4 = FileOutputStream(file4)
//                    val outStream5 = FileOutputStream(file5)
//                    cropPic.compress(Bitmap.CompressFormat.JPEG, 100, outStream1)
//                    cropPic.compress(Bitmap.CompressFormat.JPEG, 100, outStream2)
//                    cropPic.compress(Bitmap.CompressFormat.JPEG, 100, outStream3)
//                    cropPic.compress(Bitmap.CompressFormat.JPEG, 100, outStream4)
//                    cropPic.compress(Bitmap.CompressFormat.JPEG, 100, outStream5)
//                    outStream1.flush()
//                    outStream2.flush()
//                    outStream3.flush()
//                    outStream4.flush()
//                    outStream5.flush()
//                    outStream1.close()
//                    outStream2.close()
//                    outStream3.close()
//                    outStream4.close()
//                    outStream5.close()
//                    cropPic.recycle()
//
//                    Log.i(TAG, "CroppedBitmap Saved")
//                }
//            }
//        }
    }

    fun Bitmap.rotateFloat(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    // Extension function to rotate a bitmap
    private fun Bitmap.rotateInt(degree: Int): Bitmap {
        // Initialize a new matrix
        val matrix = Matrix()

        // Rotate the bitmap
        matrix.postRotate(degree.toFloat())

        // Resize the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            this,
            width,
            height,
            true
        )

        // Create and return the rotated bitmap
        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }
}
