package coniferous.client

import java.util

import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction.{Axis, AxisDirection}
import net.minecraft.util.math.{Box, Direction, Vec3d}
import net.minecraft.util.shape.VoxelShape

import scala.collection.JavaConverters._

object ModelUtil {


  def createInverseFace(face: Direction, center: Vec3d, radius: Vec3d, uvs: UvFaceData): MutableQuad = createFace(face, center, radius, uvs).copyAndInvertNormal

  def createDoubleFace(face: Direction, center: Vec3d, radius: Vec3d, uvs: UvFaceData): Array[MutableQuad] = {
    val norm = createFace(face, center, radius, uvs)
    Array[MutableQuad](norm, norm.copyAndInvertNormal)
  }

  def createModel(shape: VoxelShape, sprite: Sprite): util.List[MutableQuad] = {
    val list = new util.ArrayList[MutableQuad]
    val uvs = new UvFaceData
    for (box <- shape.getBoundingBoxes.asScala) {
      val center = box.getCenter
      val radius = new Vec3d(box.maxX - box.minX, box.maxY - box.minY, box.maxZ - box.minZ).multiply(0.5)
      for (dir <- Direction.values) {
        mapBoxToUvs(box, dir, uvs)
        uvs.inSprite(sprite)
        list.add(createFace(dir, center, radius, uvs))
      }
    }
    list
  }

  def createFace(face: Direction, center: Vec3d, radius: Vec3d, uvs: UvFaceData): MutableQuad = {
    val points = getPointsForFace(face, center, radius)
    createFace(face, points, uvs).normalf(face.getOffsetX, face.getOffsetY, face.getOffsetZ)
  }

  def createFace[T <: Vec3d](face: Direction, points: Array[T], uvs: UvFaceData): MutableQuad =
    createFace(face, points(0), points(1), points(2), points(3), uvs)

  def createFace(face: Direction, a: Vec3d, b: Vec3d, c: Vec3d, d: Vec3d, uvs1: UvFaceData): MutableQuad = {
    val quad = new MutableQuad(-1, face)
    var uvs = if (uvs1 == null) UvFaceData.DEFAULT else uvs1
    if (face == null || shouldInvertForRender(face)) {
      quad.vertex_0.positionv(a).texf(uvs.minU, uvs.minV)
      quad.vertex_1.positionv(b).texf(uvs.minU, uvs.maxV)
      quad.vertex_2.positionv(c).texf(uvs.maxU, uvs.maxV)
      quad.vertex_3.positionv(d).texf(uvs.maxU, uvs.minV)
    }
    else {
      quad.vertex_3.positionv(a).texf(uvs.minU, uvs.minV)
      quad.vertex_2.positionv(b).texf(uvs.minU, uvs.maxV)
      quad.vertex_1.positionv(c).texf(uvs.maxU, uvs.maxV)
      quad.vertex_0.positionv(d).texf(uvs.maxU, uvs.minV)
    }
    quad
  }

  def shouldInvertForRender(face: Direction): Boolean = {
    val flip = face.getDirection == AxisDirection.NEGATIVE
    if (face.getAxis == Axis.Z) !flip
    else flip
  }

  def getPointsForFace(face: Direction, center: Vec3d, radius: Vec3d): Array[Vec3d] = {
    val faceAdd = new Vec3d(face.getOffsetX * radius.x, face.getOffsetY * radius.y, face.getOffsetZ * radius.z)
    val centerOfFace = center.add(faceAdd)
    var faceRadius: Vec3d = null
    if (face.getDirection == AxisDirection.POSITIVE) faceRadius = radius.subtract(faceAdd)
    else faceRadius = radius.add(faceAdd)
    getPoints(centerOfFace, faceRadius)
  }

  def getPoints(centerFace: Vec3d, faceRadius: Vec3d): Array[Vec3d] = Array(centerFace.add(addOrNegate(faceRadius,
    u = false, v = false)), centerFace.add(addOrNegate(faceRadius, u = false, v = true)), centerFace.add(addOrNegate(
    faceRadius, u = true, v = true)), centerFace.add(addOrNegate(faceRadius, u = true, v = false)))

  def addOrNegate(coord: Vec3d, u: Boolean, v: Boolean): Vec3d = {
    val zisv = (coord.x != 0) && (coord.y == 0)
    val x = coord.x * (if (u) 1 else -1)
    val y = coord.y * (if (v) -1 else 1)
    val z = coord.z * (if (zisv) {
      if (v) -1 else 1
    } else if (u) 1 else -1)
    new Vec3d(x, y, z)
  }

  def mapBoxToUvs(box: Box, side: Direction, uvs: UvFaceData): Unit = { // TODO: Fix these!
    side match {
      case Direction.WEST => /* -X */
        uvs.minU = box.minZ.asInstanceOf[Float]
        uvs.maxU = box.maxZ.asInstanceOf[Float]
        uvs.minV = 1 - box.maxY.asInstanceOf[Float]
        uvs.maxV = 1 - box.minY.asInstanceOf[Float]

      case Direction.EAST => /* +X */
        uvs.minU = 1 - box.minZ.asInstanceOf[Float]
        uvs.maxU = 1 - box.maxZ.asInstanceOf[Float]
        uvs.minV = 1 - box.maxY.asInstanceOf[Float]
        uvs.maxV = 1 - box.minY.asInstanceOf[Float]

      case Direction.DOWN => /* -Y */
        uvs.minU = box.minX.asInstanceOf[Float]
        uvs.maxU = box.maxX.asInstanceOf[Float]
        uvs.minV = 1 - box.maxZ.asInstanceOf[Float]
        uvs.maxV = 1 - box.minZ.asInstanceOf[Float]

      case Direction.UP => /* +Y */
        uvs.minU = box.minX.asInstanceOf[Float]
        uvs.maxU = box.maxX.asInstanceOf[Float]
        uvs.minV = box.maxZ.asInstanceOf[Float]
        uvs.maxV = box.minZ.asInstanceOf[Float]

      case Direction.NORTH => /* -Z */
        uvs.minU = 1 - box.minX.asInstanceOf[Float]
        uvs.maxU = 1 - box.maxX.asInstanceOf[Float]
        uvs.minV = 1 - box.maxY.asInstanceOf[Float]
        uvs.maxV = 1 - box.minY.asInstanceOf[Float]

      case Direction.SOUTH => /* +Z */
        uvs.minU = box.minX.asInstanceOf[Float]
        uvs.maxU = box.maxX.asInstanceOf[Float]
        uvs.minV = 1 - box.maxY.asInstanceOf[Float]
        uvs.maxV = 1 - box.minY.asInstanceOf[Float]

      case _ =>
        throw new IllegalStateException("Unknown Direction " + side)

    }
  }

  def faceForRender(face: Direction): Direction = if (shouldInvertForRender(face)) face.getOpposite else face
}
