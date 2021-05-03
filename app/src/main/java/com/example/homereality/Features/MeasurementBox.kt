package com.example.homereality.Features

import android.content.Context
import android.widget.TextView
import com.example.homereality.AR.CameraFacingNode
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
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
    private var distanceCardRenderable: ViewRenderable? = null

    /**Holds the real world shape.**/
    private val realWorldMeasurements = BoxMeasurements(0f,0f,0f)

    companion object{
        private val TOTAL_VERTICES: Int = 8
        private val MIN_HEIGHT_INDICATOR: Int = 10
        private val LINE_DEFAULT: Float = 0.005f
        private val TAG = MeasurementBox::class.simpleName
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /** Inits the UI config of the box. **/

    fun setUI(){
        /**Makes the sphere nodes.**/
        MaterialFactory.makeTransparentWithColor(context, boxRenderData.pointRenderColor)
                .thenAccept { material ->
                    cubeRenderable = ShapeFactory.makeSphere(
                            boxRenderData.pointRenderRadius, Vector3.zero(), material
                    )
                    cubeRenderable?.isShadowReceiver = false
                    cubeRenderable?.isShadowCaster = false
                }

        /**Distance card views.**/
        ViewRenderable.builder()
                .setView(context, boxInfoCardLayout.cardLayout)
                .build()
                .thenAccept {
                    distanceCardRenderable = it
                    distanceCardRenderable!!.isShadowCaster = false
                    distanceCardRenderable!!.isShadowReceiver = false
                }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /**Draws a line between two anchor nodes.**/

    fun drawLine(anchorNodeFirst: AnchorNode, anchorNodeSecond: AnchorNode){
        /** World position of the nodes. **/
        val firstPointWorldPos: Vector3 = anchorNodeFirst.worldPosition
        val secondPointWorldPos: Vector3 = anchorNodeSecond.worldPosition

        /** The pose of the nodes. **/
        val firstPointPose: Pose? = anchorNodeFirst.anchor?.pose
        val secondPointPose: Pose? = anchorNodeSecond.anchor?.pose

        val difference: Vector3 = Vector3.subtract(firstPointWorldPos, secondPointWorldPos)
        if(difference != Vector3.zero()){

            val directionFromTopToBottom = difference.normalized()
            val rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())

            /** Add the line. **/
            MaterialFactory.makeOpaqueWithColor(context, boxRenderData.pointRenderColor)
                    .thenAccept { material ->

                        /** Init the line model. **/
                        val modelRenderable = ShapeFactory.makeCube(
                                Vector3(LINE_DEFAULT, LINE_DEFAULT, difference.length()),
                                Vector3.zero(),
                                material
                        )

                        /**  Create a node holding the renderable. **/
                        val node = Node()
                        node.setParent(anchorNodeSecond)
                        node.renderable = modelRenderable

                        // set the position of the node
                        node.worldPosition = Vector3.add(firstPointWorldPos, secondPointWorldPos).scaled(.5f)
                        node.worldRotation = rotationFromAToB

                        var type=""

                        if (anchorNodeList.size==2){
                            type = "width "
                        }
                        // display  a box at the middle of the line
                        addTextBox(node, getDistanceMeters(firstPointPose!!, secondPointPose!!)
                                .toFloat(),type = type)
                    }

        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Diplay a floating text box above a given node. **/

    private fun addTextBox(
            node: Node, dist: Float, position: Vector3 = Vector3(0f, 0.02f, 0f),
            rotation: Quaternion = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f),
            measurement: String = "cm", startUnit: String = "",
            view: Int = boxInfoCardLayout.cardLayout, type: String = ""
    ) {


        ViewRenderable.builder()
                .setView(context, view)
                .build()
                .thenAccept { it ->
                    (it.view as TextView).text =
                            "${type}${startUnit}${String.format(" % .1f", dist * 100)} ${measurement}"

                    it.isShadowCaster = false
                    it.isShadowReceiver = false

                    CameraFacingNode().apply {
                        setParent(node)
                        localRotation = rotation
                        localPosition = position
                        renderable = it
                    }
                }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Return the distance in meters between two world positions. **/

    private fun getDistanceMeters(pose1: Pose, pose2: Pose): Double {

        val distanceX = pose1.tx() - pose2.tx()
        val distanceY = pose1.ty() - pose2.ty()
        val distanceZ = pose1.tz() - pose2.tz()
        return Math.sqrt(
                (distanceX * distanceX +
                        distanceY * distanceY +
                        distanceZ * distanceZ).toDouble()
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
}