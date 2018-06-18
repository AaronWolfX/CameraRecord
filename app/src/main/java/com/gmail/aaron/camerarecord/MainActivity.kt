package com.gmail.aaron.camerarecord

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraDevice.StateCallback
import android.hardware.camera2.CameraManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Camera2使用
 * 1.创建CameraManager对象
 * 2.CameraManager拿到摄像头列表cameraIdList
 * 3.获取cameraId
 * 4.通过cameraId拿到CameraDevice对象
 */

class MainActivity : AppCompatActivity() {

    lateinit var cameraManager: CameraManager
    lateinit var mCamera:CameraDevice
    var stateCallback = object: CameraDevice.StateCallback(){
        override fun onOpened(camera: CameraDevice?) {
            mCamera = camera!!
            //这里拿到摄像头对象
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            camera?.close()
            Log.e("aaron","open camera onDisconnected")
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            camera?.close()
            Log.e("aaron","open camera onError")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createCameraPreviewSession()
    }

    @SuppressLint("MissingPermission")
    fun initCamera() {
        cameraManager = getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.forEach {
            Log.e("aaron", "cameraId:" + it)
        }

        cameraManager.openCamera("0", stateCallback, null)
    }

    fun createCameraPreviewSession(){

    }

}

