package com.example.phoneauthentication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private val recognizer=TextRecognition.getClient()
    private val readImage=569

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth= FirebaseAuth.getInstance()

        takeImage.setOnClickListener {
            result.text=null
            checkPermission()
        }

        camraApp.setOnClickListener {
            result.text.clear()
            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
               if(intent.resolveActivity(this.packageManager)!=null){
                startActivityForResult(intent,9)
               }
            else{
                   Toast.makeText(this, "Unable to open Camera", Toast.LENGTH_SHORT).show()
               }
        }

    }

    private fun fetchFromGallery(){

        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(intent,0)

    }


   private fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=
                    PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),readImage)
                    return
            }
                fetchFromGallery()
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){

            readImage->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        fetchFromGallery()
                }
                else{
                    Toast.makeText(this, "PERMISSION IS NOT GRANTED", Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }


    private var selectImageUri: Uri?=null
    private var imageBitmap:Bitmap?=null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "photo selected")
            selectImageUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectImageUri)
            imageDisplay.setImageBitmap(bitmap)

                prepareImage()
        }
        else if (requestCode== 9 && resultCode == Activity.RESULT_OK && data != null){

           imageBitmap =data.extras!!.get("data") as Bitmap
            imageDisplay.setImageBitmap(imageBitmap)
            Toast.makeText(this, "Its doing its work", Toast.LENGTH_SHORT).show()
                bitImageCamera()
        }

        else{
            Toast.makeText(this, "There is some error Please try again", Toast.LENGTH_SHORT).show()
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser==null){
            startActivity(Intent(this,VerifyPhone::class.java))
        }
    }


   private lateinit var image:InputImage
    private fun prepareImage(){
                 try {
                            image = InputImage.fromFilePath(this,selectImageUri!!)

                }catch (e:IOException){
                        e.printStackTrace()
                     return
                 }
        recognitionPart(image)
    }

    private fun recognitionPart(image: InputImage){

        recognizer.process(image)
            .addOnSuccessListener {visionText->
                    fetch(visionText)
            }
            .addOnFailureListener{

            }

    }
    private fun fetch(result1:Text){

        if(result1.textBlocks.size==0){
            result.text =null
            return
        }
        for (block in result1.textBlocks){
            val blockText=block.text
            result.append(blockText+"\n")
        }

    }

    private fun bitImageCamera(){
        val imageCam=InputImage.fromBitmap(imageBitmap!!,0)
        recognitionPart(imageCam)
    }






    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_manu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
                R.id.LogoutBt-> {
                    mAuth.signOut()
                    startActivity(Intent(this,VerifyPhone::class.java))
                }
        }

        return super.onOptionsItemSelected(item)

    }

}