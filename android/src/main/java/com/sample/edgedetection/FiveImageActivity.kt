package com.sample.edgedetection

//import com.sample.edgedetection.scan.ScanPresenter
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.sample.edgedetection.processor.TAG
import com.sample.edgedetection.scan.ScanPresenter
import org.opencv.android.Utils
import org.opencv.core.CvException
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File




class FiveImageActivity : AppCompatActivity() {




    private lateinit var mPresenter: ScanPresenter;


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_five_image)


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
        if (imgFile1.exists()) {
            myBitmap1 = BitmapFactory.decodeFile(intent.getStringExtra("image1") as String);
            imageView1.setImageBitmap(myBitmap1)
        }
        if (imgFile2.exists()) {
            val myBitmap2 = BitmapFactory.decodeFile(intent.getStringExtra("image2") as String);
            imageView2.setImageBitmap(myBitmap2)
        }
        if (imgFile3.exists()) {
            val myBitmap3 = BitmapFactory.decodeFile(intent.getStringExtra("image3") as String);
            imageView3.setImageBitmap(myBitmap3)
        }
        if (imgFile4.exists()) {
            val myBitmap4 = BitmapFactory.decodeFile(intent.getStringExtra("image4") as String);
            imageView4.setImageBitmap(myBitmap4)
        }
        if (imgFile5.exists()) {
            val myBitmap5 = BitmapFactory.decodeFile(intent.getStringExtra("image5") as String);
            imageView5.setImageBitmap(myBitmap5)
        }
        imageView1.setOnClickListener{
            Log.i("test", "this is working")
//           mPresenter.detectEdge()
        }


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
