package com.sample.edgedetection

//import com.sample.edgedetection.scan.ScanPresenter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.sample.edgedetection.crop.CropActivity
import com.sample.edgedetection.processor.TAG
import com.sample.edgedetection.processor.processPicture
import com.sample.edgedetection.scan.ScanActivity
import com.sample.edgedetection.scan.ScanPresenter
import org.opencv.android.Utils
import org.opencv.core.CvException
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File




class FiveImageActivity : AppCompatActivity() {

    private lateinit var mPresenter: ScanPresenter;

    var fiveImageBundle : Bundle = Bundle();
//    private lateinit var context :Context

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_five_image)
//        context =  ScanActivity()
//        Log.e("testset" , intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE)!!.toString())
//         fiveImageBundle = intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE)!!

        //imageButtons
        val imageView1 =  findViewById<ImageButton>(R.id.image1)
        val imageView2 =  findViewById<ImageButton>(R.id.image2)
        val imageView3 =  findViewById<ImageButton>(R.id.image3)
        val imageView4 =   findViewById<ImageButton>(R.id.image4)
        val imageView5 =  findViewById<ImageButton>(R.id.image5)

//        val deleteButton1 = findViewById<ImageButton>(R.id.deleteBtn1)

        //imagePath
        val imgFile1 = File(intent.getStringExtra("image1")  as String )
        val imgFile2 = File(intent.getStringExtra("image2")  as String )
        val imgFile3 = File(intent.getStringExtra("image3")  as String )
        val imgFile4 = File(intent.getStringExtra("image4")  as String )
        val imgFile5 = File(intent.getStringExtra("image5")  as String )
        val  myBitmap1:Bitmap
            myBitmap1 = BitmapFactory.decodeFile(intent.getStringExtra("image1") as String);
            imageView1.setImageBitmap(myBitmap1)
            val myBitmap2 = BitmapFactory.decodeFile(intent.getStringExtra("image2") as String);
            imageView2.setImageBitmap(myBitmap2)
            val myBitmap3 = BitmapFactory.decodeFile(intent.getStringExtra("image3") as String);
            imageView3.setImageBitmap(myBitmap3)
            val myBitmap4 = BitmapFactory.decodeFile(intent.getStringExtra("image4") as String);
            imageView4.setImageBitmap(myBitmap4)
            val myBitmap5 = BitmapFactory.decodeFile(intent.getStringExtra("image5") as String);
            imageView5.setImageBitmap(myBitmap5)

        imageView1.setOnClickListener{
            val mat1 = Mat();
            Log.i("test", "this is working")
            Utils.bitmapToMat(myBitmap1,mat1)
            detectEdge(mat1)
//            ScanPresenter( context, ScanActivity(), fiveImageBundle).detectEdge(mat1)
        }

        imageView2.setOnClickListener{
            val mat2 = Mat();
            Utils.bitmapToMat(myBitmap2,mat2)
            detectEdge(mat2)
        }

        imageView3.setOnClickListener{
            val mat3 = Mat();
            Utils.bitmapToMat(myBitmap3,mat3)
            detectEdge(mat3)
        }

        imageView4.setOnClickListener{
            val mat4 = Mat();
            Utils.bitmapToMat(myBitmap4,mat4)
            detectEdge(mat4)
        }
        imageView5.setOnClickListener{
            val mat5 = Mat();
            Utils.bitmapToMat(myBitmap5,mat5)
            detectEdge(mat5)
        }


    }
    fun detectEdge(pic: Mat) {
        SourceManager.corners = processPicture(pic)
        Imgproc.cvtColor(pic, pic, Imgproc.COLOR_RGB2BGRA)
        SourceManager.pic = pic

        val cropIntent = Intent(this, CropActivity::class.java);
        cropIntent.putExtra(EdgeDetectionHandler.INITIAL_BUNDLE, fiveImageBundle)
        (this as Activity).startActivityForResult(cropIntent, REQUEST_CODE);
        Log.i("test","detecting edge")

//        images.clear()
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
