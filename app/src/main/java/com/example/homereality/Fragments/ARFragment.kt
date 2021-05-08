package com.example.homereality.Fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.ar.core.Plane
import com.example.homereality.ARSceneActivity
import com.example.homereality.R
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment

class ARFragment : ArFragment(), Scene.OnUpdateListener {
    /** Reference to the AR Scene activity. **/
    var cameraActivity: ARSceneActivity? = null
    var animationLayout: ConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)

        return view
    }

    override fun onUpdate(p0: FrameTime?) {
        super.onUpdate(p0)
        if(animationLayout!!.visibility != View.VISIBLE)
            return

        val frame = arSceneView!!.arFrame ?: return
        if(frame.camera.trackingState != TrackingState.TRACKING)
            return

        for(plane in frame.getUpdatedTrackables(Plane::class.java)){
            if(plane.trackingState == TrackingState.TRACKING)
                hideLoadMessage()
        }

    }

    private fun hideLoadMessage(){
        if(animationLayout!!.visibility == View.INVISIBLE)
            return
        animationLayout!!.visibility = View.INVISIBLE
    }

    private fun showLoadingMessage(){
        if(animationLayout!!.visibility != View.INVISIBLE)
            return
        animationLayout!!.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if(arSceneView == null)
            return

        if(arSceneView!!.session != null)
            showLoadingMessage()
    }
}