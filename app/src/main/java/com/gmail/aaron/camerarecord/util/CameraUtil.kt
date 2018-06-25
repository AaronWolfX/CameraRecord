package com.gmail.aaron.camerarecord.util

import android.hardware.Camera
import android.util.Size

//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//    ┃　　　┃   神兽保佑
//    ┃　　　┃   代码无BUG！
//    ┃　　　┗━━━┓
//    ┃　　　　　　　┣┓
//    ┃　　　　　　　┏┛
//    ┗┓┓┏━┳┓┏┛
//      ┃┫┫　┃┫┫
// 
/**
 * Created by aaronsmith on 2018/6/25.
 */
object CameraUtil {


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
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight)
                }
            }
        }
        return optimalSize
    }


    fun getOptimalPreviewSizeCamera2(sizes: Array<Size>, h: Double, w: Double): Size {
        var ASPECT_TOLERANCE = 0.1
        var targetRatio: Double = (w / h)
        var largestArea: Double = w * h

//        var optimalSize: Camera.Size = sizes[1]
        var optimalSize = sizes[1]
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
                    optimalSize = size
                    minDiff = Math.abs(size.height - targetHeight)
                }
            }
        }
        return optimalSize
    }


}