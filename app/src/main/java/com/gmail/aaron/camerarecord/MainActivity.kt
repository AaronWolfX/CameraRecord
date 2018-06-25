package com.gmail.aaron.camerarecord

import android.Manifest
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.gmail.aaron.camerarecord.util.PermissionsUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import android.media.MediaRecorder
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import com.gmail.aaron.camerarecord.util.AvcEncoder
import java.io.File
import java.util.concurrent.ArrayBlockingQueue


class MainActivity : AppCompatActivity(), View.OnClickListener, Camera.PreviewCallback {

    lateinit var camera: Camera
    var mediaRecorder: MediaRecorder = MediaRecorder()
    lateinit var optimalPreviewSize: Camera.Size
    //待解码视频缓冲队列，静态成员！
    private val yuvqueuesize = 10
    val YUVQueue = ArrayBlockingQueue<ByteArray>(yuvqueuesize)
    lateinit var avcEncoder: AvcEncoder


    fun getYUVQueueSize(): Int {
        return YUVQueue.size
    }

    fun YUVQueuePoll(): ByteArray? {
        return YUVQueue.poll()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionsUtil.getInstance().checkPermissions(this, object : PermissionsUtil.PermissionsListener {
            override fun agreePermission() {
                textureView.surfaceTextureListener = textureListener
            }

            override fun disagreePermission() {
                toast("拒绝权限")
            }
        }, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        btnStart.setOnClickListener(this)
        btnEnd.setOnClickListener(this)
    }

    /**
     * 监听textureView 的初始化状态等
     */
    var textureListener: TextureView.SurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.e("aaron", "onSurfaceTextureSizeChanged")

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            Log.e("aaron", "onSurfaceTextureUpdated")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            Log.e("aaron", "onSurfaceTextureDestroyed")
            return surface == null
        }


        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            Log.e("aaron", "onSurfaceTextureAvailable")
            initCamera()
            avcEncoder = AvcEncoder(width, height, 30, 8500 * 1000)
            avcEncoder.StartEncoderThread(this@MainActivity)
        }
    }

    fun initCamera() {
        //获取一个Camera实例，默认为后置摄像头
        camera = Camera.open()
        //设置默认参数
        val parameters = camera.parameters
        parameters.pictureFormat = ImageFormat.JPEG
        optimalPreviewSize = getOptimalPreviewSize(camera.parameters.supportedPreviewSizes, textureView.width.toDouble(), textureView.height.toDouble())
        Log.e("aaron", "size  width:" + optimalPreviewSize.width + "  height:" + optimalPreviewSize.height)
        parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height)
        camera.parameters = parameters
        getCameraSize()

        //修改当前方向
        setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK)

        //设置surfaceHolder
        camera.setPreviewTexture(textureView.surfaceTexture)

        //开始预览
        camera.startPreview()

        camera.setPreviewCallback(this)

        //对焦
        autoFocus()
    }

    /**
     * 设置方向
     */
    fun setCameraDisplayOrientation(cameraId: Int) {
        var info: Camera.CameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        var rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result = 0
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

    /**
     * 摄像头支持的分辨率
     */
    fun getCameraSize() {
        val parameters = camera.parameters
        for (supportedPreviewSize in parameters.supportedPreviewSizes) {
            Log.e("aaronSize", "height:" + supportedPreviewSize.height + "  width:" + supportedPreviewSize.width)
        }
    }

    fun autoFocus() {
        //camera自动对焦
        camera.autoFocus(object : Camera.AutoFocusCallback {
            override fun onAutoFocus(success: Boolean, camera: Camera?) {
                toast("自动对焦成功")
            }

        })
    }

    /**
     * 拿到屏幕最佳分辨率
     */
    fun getOptimalPreviewSize(sizes: MutableList<Camera.Size>, h: Double, w: Double): Camera.Size {
        var ASPECT_TOLERANCE = 0.1
        var targetRatio: Double = (w / h)
        var largestArea: Double = w * h

        var optimalSize: Camera.Size = sizes[1]
        var minDiff = Double.MAX_VALUE
        var getSize = false

        var targetHeight = h

        //Try to find an size match aspect ratio and size
        for (size in sizes) {
            var ratio: Double = (size.width.toDouble() / size.height.toDouble())
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue
            }
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size
                getSize = true
                minDiff = Math.abs(size.height - targetHeight)
            }

//            if (size.height * size.width > largestArea) {
//                optimalSize = size
//                getSize = true
//                largestArea = size.height.toDouble() * size.width.toDouble()
//            }

        }

        //Cannot find the one match the aspect ratio,
        if (!getSize) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight)
                }
            }
        }
        return optimalSize
    }


    fun startRecord() {
        camera.unlock()
        mediaRecorder.setCamera(camera)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)

        mediaRecorder.setVideoSize(optimalPreviewSize!!.width, optimalPreviewSize!!.height)
        //帧频率
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024)
        Log.e("aaronRecorder", "width:${optimalPreviewSize!!.width}  height:${optimalPreviewSize!!.height}")
        var path = "${Environment.getExternalStorageDirectory()}${File.separator}aaron"
        var file: File = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        mediaRecorder.setOutputFile("$path${File.separator + System.currentTimeMillis()}.mp4")
        mediaRecorder.prepare()
        mediaRecorder.start()
    }


    fun stopRecord() {
        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
        camera.reconnect()
        camera.lock()
        camera.startPreview()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnStart -> startRecord()
            R.id.btnEnd -> stopRecord()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        autoFocus()
        return super.onTouchEvent(event)
    }

    /**
     * 摄像头回调   将数据返回
     */
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        putYUVData(data,data!!.size)
    }

    fun putYUVData(data: ByteArray?, size: Int) {
        if (YUVQueue.size>=10){
            YUVQueue.poll()
        }
        YUVQueue.add(data)
    }
}