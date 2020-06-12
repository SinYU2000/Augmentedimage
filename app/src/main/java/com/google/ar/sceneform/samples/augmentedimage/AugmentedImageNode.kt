/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.augmentedimage

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import java.util.concurrent.CompletableFuture



/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
class AugmentedImageNode: AnchorNode {
    // The augmented image represented by this node.
    var image: AugmentedImage? = null
        private set

    var context: Context? = null

    private var maze_scale = 0.0f

    // Add a member variable to hold the maze model.
    private var mazeNode: Node? = null

    // Add a ModelRenderable called ballRenderable.
    private var ballRenderable: ModelRenderable? = null

    // Add a variable called mazeRenderable for use with loading
    // GreenMaze.sfb.
    private var mazeRenderable: CompletableFuture<ModelRenderable?>? = null

    // Replace the definition of the AugmentedImageNode function with the
    // following code, which loads GreenMaze.sfb into mazeRenderable.
    constructor(context: Context?) {
        this.context = context;
        mazeRenderable = ModelRenderable.builder()
                .setSource(context, Uri.parse("GreenMaze.sfb"))
                .build()

        // Upon construction, start loading the models for the corners of the frame.
        if (ulCorner == null) {
            ulCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
                    .build()
            urCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
                    .build()
            llCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
                    .build()
            lrCorner = ModelRenderable.builder()
                    .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
                    .build()
        }

        // Add this code to the end of this function.
        // Add this code to the end of this function.
        MaterialFactory.makeOpaqueWithColor(context, com.google.ar.sceneform.rendering.Color(Color.RED))
                .thenAccept { material: Material? -> ballRenderable = ShapeFactory.makeSphere(0.01f, Vector3(0.0F, 0.0F, 0.0F), material) }

    }

    // Replace the definition of the setImage function with the following
    // code, which checks if mazeRenderable has completed loading.

    // Replace the definition of the setImage function with the following
    // code, which checks if mazeRenderable has completed loading.
//    fun setImage(image: AugmentedImage) {
//        this.image = image
//
//        // Initialize mazeNode and set its parents and the Renderable.
//        // If any of the models are not loaded, process this function
//        // until they all are loaded.
//        if (!mazeRenderable!!.isDone) {
//            CompletableFuture.allOf(mazeRenderable)
//                    .thenAccept { aVoid: Void? -> setImage(image) }
//                    .exceptionally { throwable: Throwable? ->
//                        Log.e(AnchorNode.TAG, "Exception loading", throwable)
//                        null
//                    }
//            return
//        }
//
//        // Set the anchor based on the center of the image.
//        anchor = image.createAnchor(image.centerPose)
//        mazeNode = Node()
//        mazeNode!!.setParent(this)
//        mazeNode!!.renderable = mazeRenderable!!.getNow(null)
//    }
    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    fun setImage(image: AugmentedImage) {
        this.image = image

        // Initialize mazeNode and set its parents and the Renderable.
        // If any of the models are not loaded, process this function
        // until they all are loaded.
        if (!mazeRenderable!!.isDone) {
            CompletableFuture.allOf(mazeRenderable)
                    .thenAccept { aVoid: Void? -> setImage(image) }
                    .exceptionally { throwable: Throwable? ->
                        Log.e(TAG, "Exception loading", throwable)
                        null
                    }
            return
        }

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)
        mazeNode = Node()
        mazeNode!!.setParent(this)
        mazeNode!!.renderable = mazeRenderable!!.getNow(null)

        // If any of the models are not loaded, then recurse when all are loaded.
        if (!ulCorner!!.isDone || !urCorner!!.isDone || !llCorner!!.isDone || !lrCorner!!.isDone) {
            CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
                    .thenAccept { aVoid: Void? -> setImage(image) }
                    .exceptionally { throwable: Throwable? ->
                        Log.e(TAG, "Exception loading", throwable)
                        null
                    }
        }

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)

        // Make the 4 corner nodes.
        val localPosition = Vector3()
        var cornerNode: Node

        // Upper left corner.
        localPosition[-0.5f * image.extentX, 0.0f] = -0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = ulCorner!!.getNow(null)

        // Upper right corner.
        localPosition[0.5f * image.extentX, 0.0f] = -0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = urCorner!!.getNow(null)

        // Lower right corner.
        localPosition[0.5f * image.extentX, 0.0f] = 0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = lrCorner!!.getNow(null)

        // Lower left corner.
        localPosition[-0.5f * image.extentX, 0.0f] = 0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = llCorner!!.getNow(null)

        // Make sure the longest edge fits inside the image.
        // Make sure the longest edge fits inside the image.
        val maze_edge_size = 492.65f
        val max_image_edge = Math.max(image.extentX, image.extentZ)
        maze_scale = max_image_edge / maze_edge_size

        // Scale Y an extra 10 times to lower the maze wall.

        // Scale Y an extra 10 times to lower the maze wall.
        mazeNode!!.localScale = Vector3(maze_scale, maze_scale * 0.1f, maze_scale)


        // Add the ball at the end of the setImage function.
        // Add the ball at the end of the setImage function.
        val ballNode = Node()
        ballNode.setParent(this)
        ballNode.renderable = ballRenderable
        ballNode.localPosition = Vector3(0.0f, 0.1f, 0.0f)

    }

    companion object {
        private const val TAG = "AugmentedImageNode"

        // Models of the 4 corners.  We use completable futures here to simplify
        // the error handling and asynchronous loading.  The loading is started with the
        // first construction of an instance, and then used when the image is set.
        private var ulCorner: CompletableFuture<ModelRenderable?>? = null
        private var urCorner: CompletableFuture<ModelRenderable?>? = null
        private var lrCorner: CompletableFuture<ModelRenderable?>? = null
        private var llCorner: CompletableFuture<ModelRenderable?>? = null
    }

//    init {
//        // Upon construction, start loading the models for the corners of the frame.
//        if (ulCorner == null) {
//            ulCorner = ModelRenderable.builder()
//                    .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
//                    .build()
//            urCorner = ModelRenderable.builder()
//                    .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
//                    .build()
//            llCorner = ModelRenderable.builder()
//                    .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
//                    .build()
//            lrCorner = ModelRenderable.builder()
//                    .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
//                    .build()
//        }
//    }
}