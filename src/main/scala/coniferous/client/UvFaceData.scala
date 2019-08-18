package coniferous.client

import net.minecraft.client.texture.Sprite

object UvFaceData {
  val DEFAULT = new UvFaceData(0, 0, 1, 1)

  def from16(minU: Double, minV: Double, maxU: Double, maxV: Double) = new UvFaceData(minU / 16.0, minV / 16.0, maxU / 16.0, maxV / 16.0)

  def from16(minU: Int, minV: Int, maxU: Int, maxV: Int) = UvFaceData(minU / 16f, minV / 16f, maxU / 16f, maxV / 16f)
}

case class UvFaceData(var minU: Float, var maxU: Float, var minV: Float, var maxV: Float) {

  def this(from: UvFaceData) {
    this(from.minU, from.maxU, from.minV, from.maxV)
  }

  def this(minU: Double, minV: Double, maxU: Double, maxV: Double) {
    this(minU.toFloat, minV.toFloat, maxU.toFloat, maxV.toFloat)
  }

  def this() {
    this(0, 0, 1, 1)
  }

  def inParent(parent: UvFaceData): UvFaceData = parent.andSub(this)

  def andSub(sub: UvFaceData): UvFaceData = {
    val size_u = maxU - minU
    val size_v = maxV - minV
    val min_u = minU + sub.minU * size_u
    val min_v = minV + sub.minV * size_v
    val max_u = minU + sub.maxU * size_u
    val max_v = minV + sub.maxV * size_v
    new UvFaceData(min_u, min_v, max_u, max_v)
  }

  override def toString: String = "[ " + minU * 16 + ", " + minV * 16 + ", " + maxU * 16 + ", " + maxV * 16 + " ]"

  def inSprite(sprite: Sprite): Unit = {
    minU = sprite.getU(minU * 16)
    minV = sprite.getV(minV * 16)
    maxU = sprite.getU(maxU * 16)
    maxV = sprite.getV(maxV * 16)
  }

  // public void inSprite(ISprite sprite) {
  // minU = (float) sprite.getInterpU(minU);
  // minV = (float) sprite.getInterpV(minV);
  // maxU = (float) sprite.getInterpU(maxU);
  // maxV = (float) sprite.getInterpV(maxV);
  // }
}
