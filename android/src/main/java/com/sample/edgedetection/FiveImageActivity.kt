package com.sample.edgedetection

//import com.sample.edgedetection.scan.ScanPresenter
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.sample.edgedetection.processor.TAG
import org.opencv.android.Utils
import org.opencv.core.CvException
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FiveImageActivity : AppCompatActivity() {




    public var images = kotlin.collections.ArrayList<Mat>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, images.toString())
        val imageView1 =  findViewById<ImageView>(R.id.imageView)
        val imageView2 =  findViewById<ImageView>(R.id.imageView2)
        val imageView3 =  findViewById<ImageView>(R.id.imageView3)
        val imageView4 =   findViewById<ImageView>(R.id.imageView4)
        val imageView5 =  findViewById<ImageView>(R.id.imageView5)
//        imageView1.setImageBitmap(convertMatToBitMap(mPresenter.images[0]))
//        imageView2.setImageBitmap(convertMatToBitMap(mPresenter.images[1]))
//        imageView3.setImageBitmap(convertMatToBitMap(mPresenter.images[2]))
//        imageView4.setImageBitmap(convertMatToBitMap(mPresenter.images[3]))
//        imageView5.setImageBitmap(convertMatToBitMap(mPresenter.images[4]))
    }



}

private fun convertMatToBitMap(input: Mat): Bitmap? {
    var bmp: Bitmap? = null
    val rgb = Mat()
    Imgproc.cvtColor(input, rgb, Imgproc.COLOR_BGR2RGB)
    try {
        bmp = Bitmap.createBitmap(rgb.cols(), rgb.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgb, bmp)
    } catch (e: CvException) {
        Log.i(TAG, e.message.toString())
    }
    return bmp
}
