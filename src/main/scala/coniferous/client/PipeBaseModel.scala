package coniferous.client

import coniferous.pipe.PipeEntity.PipeRenderData
import coniferous.pipe.{PipeEntity, SidedPipe}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.{Direction, Vec3d}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object PipeBaseModel { // Models
  val colourOffset = 0.01
  val faceOffset = new Array[Vec3d](6)
  val uvs = new UvFaceData()
  val uvsRot = Array(Array(2, 0, 3, 3), Array(0, 2, 1, 1), Array(2, 0, 0, 2), Array(0, 2, 2, 0), Array(3, 3, 0, 2), Array(1, 1, 2, 0))
  val types = Array(UvFaceData.from16(4, 0, 12, 4), UvFaceData.from16(4, 12,
    12, 16), UvFaceData.from16(0, 4, 4, 12), UvFaceData.from16(12, 4,
    16, 12))
  var center = new Vec3d(0.5f, 0.5f, 0.5f)
  var radius = new Vec3d(0.25f, 0.25f, 0.25f)
  private var QUADS: Array[Array[Array[MutableQuad]]] = _
  private var QUADS_COLOURED: Array[Array[Array[MutableQuad]]] = _

  QUADS = Array.ofDim(2)
  QUADS_COLOURED = Array.ofDim(2)

  // Model Usage
  def generateCutout(key: PipeEntity.PipeRenderData): IndexedSeq[BakedQuad] = {
    val quads = new ArrayBuffer[MutableQuad]()
    for (face <- Direction.values) {
      val connected = key.isConnected(face)
      val sprite = if (connected) getSprite(key, face)
      else getCenterSprite(key.kind)
      val quadsIndex = if (connected) 1
      else 0
      val quadArray = QUADS(quadsIndex)(face.ordinal)
      addQuads(quadArray, quads, sprite)
    }
    quads.map(_.toBakedBlock)
  }

  private def getSprite(key: PipeRenderData, face: Direction): Sprite = {
    val pipe = key.pipe
    pipe match {
      case sided: SidedPipe =>
        val mainDir = sided.mainSide
        if (mainDir eq face) return getPipeSprite(s"${key.kind.toString}_main")
      case _ =>
    }
    getCenterSprite(key.kind)
  }

  for (face <- Direction.values) {
    faceOffset(face.ordinal) = new Vec3d(face.getOpposite.getVector).multiply(colourOffset)
  }
  // not connected
  QUADS(0) = Array.ofDim(6, 2)
  QUADS_COLOURED(0) = Array.ofDim(6, 2)

  private def getPipeSprite(id: String) = MinecraftClient.getInstance.getSpriteAtlas.getSprite(s"coniferous:pipe_$id")

  def getCenterSprite(kind: PipeEntity.PipeType.Value): Sprite = getPipeSprite(kind.toString.toLowerCase())

  private def addQuads(from: Array[MutableQuad], to: mutable.Buffer[MutableQuad], sprite: Sprite): Unit = {
    for (f <- from) {
      if (f != null) {
        val copy = new MutableQuad(f)
        copy.setSprite(sprite)
        copy.texFromSprite(sprite)
        to += copy
      }
    }
  }

  uvs.minV = 4 / 16f
  uvs.minU = uvs.minV
  uvs.maxV = 12 / 16f
  uvs.maxU = uvs.maxV
  for (face <- Direction.values) {
    val quad = ModelUtil.createFace(face, center, radius, uvs)
    quad.setDiffuse(quad.normalvf)
    QUADS(0)(face.ordinal)(0) = quad
    dupDarker(QUADS(0)(face.ordinal))
    val colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs)
    for (q <- colQuads) {
      q.translatevd(faceOffset(face.ordinal))
    }
    QUADS_COLOURED(0)(face.ordinal) = colQuads
  }

  private def dupDarker(quads: Array[MutableQuad]): Unit = {
    val halfLength = quads.length / 2
    val mult = 0.75f
    for (i <- 0 until halfLength) {
      val n = i + halfLength
      val from = quads(i)
      if (from != null) {
        val to = from.copyAndInvertNormal
        to.setCalculatedDiffuse()
        to.multColourd(mult)
        quads(n) = to
      }
    }
  }

  private def dupInverted(quads: Array[MutableQuad]): Unit = {
    val halfLength = quads.length / 2
    for (i <- 0 until halfLength) {
      val n = i + halfLength
      val from = quads(i)
      if (from != null) quads(n) = from.copyAndInvertNormal
    }
  }

  // connected
  QUADS(1) = Array.ofDim(6, 8)
  QUADS_COLOURED(1) = Array.ofDim(6, 8)
  for (side <- Direction.values) {
    center = new Vec3d(0.5 + side.getOffsetX * 0.375f, 0.5 + side.getOffsetY * 0.375f,
      0.5 + side.getOffsetZ * 0.375f)
    radius = new Vec3d(if (side.getAxis eq Axis.X) 0.125f
    else 0.25f, if (side.getAxis eq Axis.Y) 0.125f
    else 0.25f, if (side.getAxis eq Axis.Z) 0.125f
    else 0.25f)
    var i = 0
    for (face <- Direction.values) {
      if (face.getAxis eq side.getAxis) {
        val quad = ModelUtil.createFace(face, center, radius, types(i))
        quad.rotateTextureUp(uvsRot(side.ordinal)(i))
        val col = new MutableQuad(quad)
        quad.setDiffuse(quad.normalvf)
        QUADS(1)(side.ordinal)(i) = quad
        col.translatevd(faceOffset(face.ordinal))
        QUADS_COLOURED(1)(side.ordinal)(i) = col
        i += 1
      }
      dupDarker(QUADS(1)(side.ordinal))
      dupInverted(QUADS_COLOURED(1)(side.ordinal))
    }
  }
}

