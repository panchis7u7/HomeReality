package com.voxel.homereality.Features

import android.content.Context
import android.widget.TextView
import com.voxel.homereality.AR.CameraFacingNode
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

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

    /** All the vertical vertices used to represent the box. **/
    private var verticalVertices: MutableList<Node> = mutableListOf()

    /**All the vertices of the upper frane of the box.**/
    private var upperFrameVertices: MutableList<Node> = mutableListOf()

    /**All the vertices of the lower frame of the box.**/
    private var lowerFrameVertices: MutableList<Node> = mutableListOf()

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

        /** Node points.**/
        val PT_1: Int = 0
        val PT_2: Int = 1
        val PT_3: Int = 2
        val PT_4: Int = 3
        val PT_5: Int = 4
        val PT_6: Int = 5
        val PT_7: Int = 6
        val PT_8: Int = 7
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

                        var type: String = ""

                        if (anchorNodeList.size==2){
                            type = "width "
                        }
                        /** Display  a box at the middle of the line. **/
                        addTextBox(node, getDistanceMeters(firstPointPose!!, secondPointPose!!)
                                .toFloat(), type = type)
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Renders the 3D measurement box. **/

    fun drawSquare() {
        /** Two points reprent the length of the box. **/
        val pt1 = anchorNodeList[PT_1].worldPosition
        val pt2 = anchorNodeList[PT_2].worldPosition

        // 3 pint of the line of the paraaler line to the
        // line created by PT_1 and PT_2
        val tracker = anchorNodeList[PT_3].worldPosition
        val p2ToPt1 = Vector3.subtract(pt2, pt1)
        val p2ToTracker = Vector3.subtract(pt2, tracker)
        val r = rejections(p2ToTracker, p2ToPt1)
        val midPoint = midPointVector(pt1, pt2)

        // offset of the box
        val toAdd = Vector3(0f, 0f, Math.abs(r.length()) / 2 + 0f)

        // set the box length and width
        realWorldMeasurements.boxLength = r.length()
        realWorldMeasurements.boxWidth = p2ToPt1.length()

        // real distance
        val dist1 = getDistanceMeters(
                anchorNodeList[PT_1].anchor!!.pose,
                anchorNodeList[PT_2].anchor!!.pose
        ).toFloat()
        val dist2 = r.length()


        val difference = Vector3.subtract(midPoint, tracker)
        if (difference != Vector3.zero()) {
            val directionFromTopToBottom = r.normalized()
            val rotation = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())


            // add area
            MaterialFactory.makeTransparentWithColor(
                    context,
                    boxRenderData.areaRenderColor
            )
                    .thenAccept { material ->

                        material.setFloat("reflectance", 0F)
                        material.setFloat("roughness", 1F)
                        material.setFloat("metallic", 1F)

                        val modelRenderable = ShapeFactory.makeCube(Vector3(
                                        realWorldMeasurements.boxWidth,
                                        LINE_DEFAULT,
                                        realWorldMeasurements.boxLength), toAdd, material)
                                .apply {
                                    isShadowReceiver = false
                                    isShadowCaster = false
                                }

                        val node = TransformableNode(arFragment.transformationSystem)
                        node.scaleController.isEnabled = false
                        node.getTranslationController().setEnabled(false);

                        node.setParent(anchorNodeList[PT_3])
                        node.renderable = modelRenderable
                        node.worldPosition = midPoint
                        node.worldRotation = rotation

                        val node2 = Node()
                        node2.setParent(anchorNodeList[PT_3])
                        node2.worldPosition = midPoint
                        node2.worldRotation = rotation

                        addTextBox(node2, 0f, position = Vector3.add(toAdd, Vector3(
                                LINE_DEFAULT, LINE_DEFAULT * 32, LINE_DEFAULT
                            )
                        ), measurement = "cm", startUnit = "H=", view = boxInfoCardLayout.heightCardLayout)

                        addTextBox(node2, dist1 * dist2, position = Vector3.add(
                                        toAdd, Vector3(LINE_DEFAULT, 0f, LINE_DEFAULT)
                        ), measurement = "cm^2", startUnit = "S=", view = boxInfoCardLayout.heightCardLayout)

                        centerNode = node
                        centerLocation = midPoint
                        heightNode = node2

                    }

            /** Double existing vertex from 4 to 8. **/
            multipleNodeArrayList(vertices)

            /** Remove line. **/
            while (anchorNodeList[0].children.size > 0) {
                anchorNodeList[0].removeChild(anchorNodeList[0].children[0])
            }

            while (anchorNodeList[1].children.size > 0) {
                anchorNodeList[1].removeChild(anchorNodeList[1].children[0])
            }

            /** Draw surrounded lines. **/
            drawFrame(position = midPoint, rotation = rotation, dist1 = dist1, dist2 = dist2)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns the rejection of a given vetor with another. **/

    private fun rejections(firstVector: Vector3, secondNode: Vector3): Vector3 {
        var projection = secondNode.normalized().scaled(Vector3.dot(firstVector, secondNode.normalized()))
        return Vector3.subtract(firstVector, projection)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns a vector located at the center of 2 nodes. **/

    private fun midPointVector(point1: Vector3, point2: Vector3): Vector3 {
        var avgX = (point1.x + point2.x) / 2
        var avgY = (point1.y + point2.y) / 2
        var avgZ = (point1.z + point2.z) / 2
        return Vector3(avgX, avgY, avgZ)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Copies all the nodes in the array and adds them. **/

    fun multipleNodeArrayList(arrayListNode: MutableList<Node>) {
        /** Create a copy of existing nodes. **/
        var tempArrayList = arrayListNode

        for (i in 1..arrayListNode.size) {

            var tempVertex = Node()
            tempVertex.worldPosition = vertices[i].worldPosition
            tempVertex.worldRotation = vertices[i].worldRotation
            tempVertex.renderable = vertices[i].renderable
            tempVertex.setParent(vertices[i].parent)

            tempArrayList.add(tempVertex)

        }

        /** Add the verticies to the list. **/
        for (i in 1..tempArrayList.size) {

            arrayListNode.add(tempArrayList[i])
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Draws a surrounding frame comprised if 4 lines around the box body. **/

    private fun drawFrame(position: Vector3, rotation: Quaternion, dist1: Float, dist2: Float) {
        /** Add horizontal lines ------------------- **/
        /** Line 1. **/
        renderVerticalLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(realWorldMeasurements.boxWidth / 2, 0f, 0f),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )

        /** Line 2. **/
        renderVerticalLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(-realWorldMeasurements.boxWidth / 2, 0f, 0f),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )


        /** Line 3. **/
        renderVerticalLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(
                        realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength)
                ),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )

        /** Line 4. **/
        renderVerticalLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(
                        -realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength)
                ),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )

        /** Add lower frame-------------------- **/
        /** Add Line. **/
        renderLowerFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(realWorldMeasurements.boxWidth, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3.zero(),
                parentAnchorNode = anchorNodeList[1],
                position = position,
                rotation = rotation
        )

        /** Add Line. **/
        renderLowerFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(realWorldMeasurements.boxWidth, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(0f, 0f, Math.abs(realWorldMeasurements.boxLength)),
                parentAnchorNode = anchorNodeList[1],
                position = position,
                rotation = rotation
        )

        /** Add Line. **/
        renderLowerFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, realWorldMeasurements.boxLength),
                offset = Vector3(
                        -realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength / 2)
                ),
                parentAnchorNode = anchorNodeList[1],
                position = position,
                rotation = rotation
        )

        /** Add Line. **/
        renderLowerFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, realWorldMeasurements.boxLength),
                offset = Vector3(
                        realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength / 2)
                ),
                parentAnchorNode = anchorNodeList[1],
                position = position,
                rotation = rotation
        )

        /** Add upper frame ------------------- **/
        /** Add Line. **/
        renderUpperFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(realWorldMeasurements.boxWidth, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3.zero(),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation,
                dist = dist1,
                type = "width ="
        )

        /** Add Line. **/
        renderUpperFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(realWorldMeasurements.boxWidth, LINE_DEFAULT, LINE_DEFAULT),
                offset = Vector3(0f, 0f, Math.abs(realWorldMeasurements.boxLength)),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )

        /** Add Line. **/
        renderUpperFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, realWorldMeasurements.boxLength),
                offset = Vector3(
                        -realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength / 2)
                ),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation,
                dist = dist2,
                type = "length ="

        )

        /** Add Line. **/
        renderUpperFrameLine(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, LINE_DEFAULT, realWorldMeasurements.boxLength),
                offset = Vector3(
                        realWorldMeasurements.boxWidth / 2,
                        0f,
                        Math.abs(realWorldMeasurements.boxLength / 2)
                ),
                parentAnchorNode = anchorNodeList[PT_3],
                position = position,
                rotation = rotation
        )

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Renders the vertical lines of the measurement box. **/

    private fun renderVerticalLine(
            context: Context, color: Color,
            modeSize: Vector3,
            offset: Vector3,
            parentAnchorNode: AnchorNode,
            position: Vector3,
            rotation: Quaternion
    ) {

        var node = renderLine(
                context, color,
                modeSize, offset,
                parentAnchorNode,
                position, rotation
        )
        verticalVertices.add(node)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Renders the lower frame of the measurement box. **/

    private fun renderLowerFrameLine(
            context: Context, color: Color,
            modeSize: Vector3,
            offset: Vector3,
            parentAnchorNode: AnchorNode,
            position: Vector3,
            rotation: Quaternion, dist: Float? = null, type: String = ""
    ) {

        var node = renderLine(
                context, color,
                modeSize, offset,
                parentAnchorNode,
                position, rotation
        )

        if (dist != null) {
            addTextBox(
                    node, dist, position = Vector3.add(offset, Vector3(0f, 0.01f, 0f)),
                    measurement = "cm", type = type
            )
        }

        lowerFrameVertices.add(node)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Renders the upper frame of the measurement box. **/

    private fun renderUpperFrameLine(
            context: Context, color: Color,
            modeSize: Vector3,
            offset: Vector3,
            parentAnchorNode: AnchorNode,
            position: Vector3,
            rotation: Quaternion, dist: Float? = null, type: String = ""
    ) {

        var node = renderLine(
                context, color,
                modeSize, offset,
                parentAnchorNode,
                position, rotation
        )

        if (dist != null) {

            addTextBox(node, dist, position = Vector3.add(offset, Vector3(0f, 0.01f, 0f)),
                    measurement = "cm", type = type
            )
        }
        upperFrameVertices.add(node)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Renders a line. **/

    private fun renderLine(
            context: Context, color: Color, modeSize: Vector3,
            offset: Vector3, parentAnchorNode: AnchorNode,
            position: Vector3, rotation: Quaternion
    ): Node {

        var modelRenderable: ModelRenderable? = getLineModelRendable(context, color, modeSize, offset)

        var node = Node().apply {
            setParent(parentAnchorNode)
            renderable = modelRenderable
            worldPosition = position
            worldRotation = rotation
        }
        return node
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Return th renderable of a line. **/

    private fun getLineModelRendable(
            context: Context, color: Color, modeSize: Vector3, offset: Vector3
    ): ModelRenderable? {
        var modelRenderable: ModelRenderable? = null

        // add line
        MaterialFactory.makeOpaqueWithColor(context, color)
                .thenAccept { material ->

                    modelRenderable = ShapeFactory.makeCube(
                            modeSize,
                            offset,
                            material
                    ).apply {
                        isShadowCaster = false
                        isShadowReceiver = false
                    }
                }
        return modelRenderable
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Update the height of the box frame. **/

    private fun updateVerticalFrameHeight(height: Float) {
        /** Line 1. **/
        verticalVertices[0].renderable = getLineModelRendable(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, 2 * LINE_DEFAULT * height, LINE_DEFAULT),
                offset = Vector3(realWorldMeasurements.boxWidth / 2, 0f + height / 200, 0f)
        )

        /** Line 2. **/
        verticalVertices[1].renderable = getLineModelRendable(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, 2 * LINE_DEFAULT * height, LINE_DEFAULT),
                offset = Vector3(-realWorldMeasurements.boxWidth / 2, 0f + height / 200, 0f)
        )

        /** Line 3. **/
        verticalVertices[2].renderable = getLineModelRendable(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, 2 * LINE_DEFAULT * height, LINE_DEFAULT),
                offset = Vector3(
                        realWorldMeasurements.boxWidth / 2,
                        0f + height / 200,
                        Math.abs(realWorldMeasurements.boxLength)
                )
        )

        /** Line 4. **/
        verticalVertices[3].renderable = getLineModelRendable(
                context = context,
                color = boxRenderData.lineRenderColor,
                modeSize = Vector3(LINE_DEFAULT, 2 * LINE_DEFAULT * height, LINE_DEFAULT),
                offset = Vector3(
                        -realWorldMeasurements.boxWidth / 2,
                        0f + height / 200,
                        Math.abs(realWorldMeasurements.boxLength)
                )
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Add anchor node to the box. **/

    fun addAnchorNode(anchorNode: AnchorNode) {
        val transformableNode =
                TransformableNode(arFragment.transformationSystem).apply {
                    renderable = null
                    setParent(anchorNode)
                    scaleController.isEnabled = false
                    getTranslationController().setEnabled(false)
                }

        if (anchorNodeList.size != 2) {
            transformableNode.renderable = cubeRenderable
        }

        // add to list of the box'es nodes
        anchorNodeList.add(anchorNode)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Add a vertex to the list of the boxes vertices. **/

    fun addVertex(node: Node) {
        vertices.add(node)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Update the height of the box body. **/

    private fun updateBoxHeight(height: Float) {

        var toAdd =
                Vector3(0f, 0f + height * LINE_DEFAULT, Math.abs(realWorldMeasurements.boxLength) / 2)
        // add area

        MaterialFactory.makeTransparentWithColor(context, boxRenderData.areaRenderColor)
                .thenAccept { material ->
                    material.setFloat("reflectance", 0F)
                    material.setFloat("roughness", 1F)
                    material.setFloat("metallic", 1F)

                    val modelRenderable = ShapeFactory.makeCube(
                            Vector3(
                                    realWorldMeasurements.boxWidth,
                                    2 * height * LINE_DEFAULT,
                                    realWorldMeasurements.boxLength
                            ), toAdd, material)
                            .apply {
                                isShadowReceiver = false
                                isShadowCaster = false
                            }

                    centerNode!!.renderable = modelRenderable
                }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Updates the real world height of the measurement box. **/

    fun setBoxHeight(updatedHeight: Float) {

        var height = updatedHeight

        if (height <= 1f) {
            height = 1f
        }

        var diffrence = height - realWorldMeasurements.boxHeight

        realWorldMeasurements.boxHeight = height

        updateBoxHeight(height)
        updateVerticalFrameHeight(height)

        /** Move upper frame. **/
        for (i in 0..3) {

            upperFrameVertices[i].localPosition = Vector3(
                    upperFrameVertices[i].localPosition.x,
                    upperFrameVertices[i].localPosition.y + 2 * LINE_DEFAULT * diffrence,
                    upperFrameVertices[i].localPosition.z
            )
            heightNode!!.localPosition = Vector3(
                    heightNode!!.localPosition.x,
                    heightNode!!.localPosition.y + LINE_DEFAULT / 2 * diffrence,
                    heightNode!!.localPosition.z
            )
        }

        var it: ViewRenderable = heightNode!!.children[0].renderable as ViewRenderable
        (it.view as TextView).text = "H= ${String.format("%.1f", height)} cm"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Get the current measurement stage. **/

    fun getMeasurementStage(): MeasurementStage {
        when (vertices.size) {
            PT_1 -> return MeasurementStage.NONE
            PT_2 -> return MeasurementStage.ORIGIN
            PT_3 -> return MeasurementStage.WIDTH
            PT_4, PT_5 -> return MeasurementStage.LENGTH
            else -> return MeasurementStage.HEIGHT
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns the measurements information of the box. **/

    fun getBoxMeasurements(): BoxMeasurements {
        return realWorldMeasurements
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Deletes renderable and all the boxes verticies and anchor nodes. **/

    fun clear() {
        vertices.clear()
        verticalVertices.clear()
        anchorNodeList.clear()
        upperFrameVertices.clear()
        lowerFrameVertices.clear()
        centerLocation = null
        centerNode = null
        heightNode = null
        realWorldMeasurements.boxWidth = 0f
        realWorldMeasurements.boxHeight = 0f
        realWorldMeasurements.boxLength = 0f
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /** Return an explicit anchor node. **/

    fun getAnchorNode(index: Int): AnchorNode {
        return anchorNodeList[index]
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** Returns how many anchor nodes belong to the box. **/

    fun getAnchorListSize(): Int {
        return anchorNodeList.size
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
}