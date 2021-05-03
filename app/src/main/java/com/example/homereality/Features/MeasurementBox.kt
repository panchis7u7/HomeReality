package com.example.homereality.Features

import android.content.Context
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

////////////////////////////////////////////////////////////////////////////////////////////////////
/**Details for box rendering.*/

data class BoxRenderData(val pointRenderRadius: Float,
                         var pointRenderColor: Color,
                         var lineRenderColor: Color,
                         var areaRenderColor: Color)

////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////
/**Holds the layout of the measurements from the floating cards.*/

data class BoxInfoCardLayout(val cardLayout: Int,
                             val heightCardLayout: Int)

////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////
/**Holds the measurments of a 3D measurement box.*/

data class BoxMeasurements(var boxWidth: Float,
                           var boxLength: Float,
                           var boxHeight: Float)

////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////
/**Current stage of measurement.
* NONE -> No node was placed.
* ORIGIN -> A single node was placed.
* WIDTH -> Two nodes were placed. Represents a line.
* LENGTH -> Four nodes were placed.
* HEIGHT -> Eight nodes were placed.*/

enum class MeasurementStage{
    NONE, ORIGIN, WIDTH, LENGTH, HEIGHT
}

////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////
/**Class to measure real word 3D box in ARCore.*/

class MeasurementBox (var boxRenderData: BoxRenderData,
                      var boxInfoCardLayout: BoxInfoCardLayout,
                      var context: Context,
                      var arFragment: ArFragment){
    /**Boxes Anchor nodes.**/
    var anchorNodeList: MutableList<AnchorNode> = mutableListOf()

    /**All the vertices used to represent the box.**/
    private var vertices: MutableList<Node> = mutableListOf()

    /**All the vertices of the upper frane of the box.**/
    private var upperFrameVerticecs: MutableList<Node> = mutableListOf()

    /**All the vertices of the lower frame of the box.**/
    private var lowerFrameVerticecs: MutableList<Node> = mutableListOf()

    /**Node appearing at the center of the boxes base.**/
    var centerNode: Node? = null

    /**Vector representing the location of the base of the box.**/
    var centerLocation: Vector3? = null

    /**Node representing the height of the box.**/
    private var heightNode: Node? = null

    /**Renderable of the body of the 3D box.**/
    private var cubeRenderable: ModelRenderable? = null

    /**Renderable of the cards that will display the real world measurements.**/
    private var distanceCardRenderable: ModelRenderable? = null

    /**Holds the real world shape.**/
    private val realWorldMeasurements = BoxMeasurements(0f,0f,0f)

    companion object{
        private val TOTAL_VERTICES: Int = 8
        private val MIN_HEIGHT_INDICATOR: Int = 10
        private val LINE_DEFAULT: Float = 0.005f
        private val TAG = MeasurementBox::class.simpleName
    }

    /** Inits the UI config of the box. **/
    fun setUI(){

    }

}