package com.voxel.homereality.AR

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3

class CameraFacingNode: Node() {
    override fun onUpdate(p0: FrameTime?) {
        super.onUpdate(p0)
        scene?.let {
            /** Current camera position. **/
            val cameraPosition = it.camera.worldPosition

            /** Current node position. **/
            val nodePosition = this@CameraFacingNode.worldPosition

            /** Direction from camera to node. **/
            val cameraToNode = Vector3.subtract(cameraPosition, nodePosition)

            /** Update direction of the node. **/
            this@CameraFacingNode.worldRotation =
                    Quaternion.lookRotation(cameraToNode, Vector3.up())
        }
    }
}