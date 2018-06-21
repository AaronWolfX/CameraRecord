package com.gmail.aaron.camerarecord

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.StateCallback
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.widget.Toast
import com.gmail.aaron.camerarecord.util.PermissionsUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*
import kotlin.math.log


/**
 * Camera2使用
 * 1.创建CameraManager对象
 * 2.CameraManager拿到摄像头列表cameraIdList
 * 3.获取cameraId
 * 4.通过cameraId拿到CameraDevice对象
 */

class MainActivity : AppCompatActivity() {

    lateinit var cameraManager: CameraManager
    lateinit var mCamera: CameraDevice
    lateinit var mBackgroundHandler:Handler
    lateinit var holder:SurfaceHolder
    lateinit var captureSession: CameraCaptureSession
    lateinit var mSurface: SurfaceTexture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionsUtil.getInstance().checkPermissions(this, object : PermissionsUtil.PermissionsListener {
            override fun agreePermission() {
                initTextureView()
            }

            override fun disagreePermission() {
                toast("拒绝权限")
            }
        }, Manifest.permission.CAMERA)
    }

    fun initTextureView(){

        textureView.surfaceTextureListener = textureListener
//        initCamera()
    }


    var textureListener:TextureView.SurfaceTextureListener = object :TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.e("aaron","onSurfaceTextureSizeChanged")

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            Log.e("aaron","onSurfaceTextureUpdated")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Log.e("aaron","onSurfaceTextureDestroyed")
            return surface==null
        }


        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.e("aaron","onSurfaceTextureAvailable")
            mSurface  = surface!!
            initCamera()
        }

    }

    fun startBackgroundThread(){
        Log.e("aaron","启动thread")
        var backroundThread:HandlerThread = HandlerThread("background")
        backroundThread.start()
        mBackgroundHandler = Handler(backroundThread.looper)
    }

    @SuppressLint("MissingPermission")
    fun initCamera() {
        startBackgroundThread()
        cameraManager = getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.forEach {
            Log.e("aaron", "cameraId:" + it)
        }

        cameraManager.openCamera("0", stateCallback, null)
    }

    var stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice?) {
            mCamera = camera!!
            //这里拿到摄像头对象
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice?) {
            camera?.close()
            Log.e("aaron", "open camera onDisconnected")
        }

        override fun onError(camera: CameraDevice?, error: Int) {
            camera?.close()
            Log.e("aaron", "open camera onError")
        }

    }

    fun createCameraPreviewSession() {
        Log.e("aaron","open success")
        /**
        //设置了一个具有输出Surface的CaptureRequest.Builder。
        mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
        //创建一个CameraCaptureSession来进行相机预览。
        mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()),
         */

        var mPreviewRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        //获取Surface显示预览数据
        val mSurface = Surface(mSurface)
        mPreviewRequestBuilder.addTarget(mSurface)
        mCamera.createCaptureSession(Arrays.asList(mSurface),object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession?) {
                Log.e("aaron","onConfigureFailed")
            }

            override fun onConfigured(session: CameraCaptureSession?) {
                Log.e("aaron","onConfigured")
                captureSession = session!!
                // 自动对焦应
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                val request = mPreviewRequestBuilder.build()
                captureSession.setRepeatingRequest(request,null,null)
            }

        },mBackgroundHandler)
        Log.e("aaron","createCameraPreviewSession")
    }

}

