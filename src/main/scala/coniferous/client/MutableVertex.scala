package coniferous.client

import java.lang.{Float => JFloat}

import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.render.VertexFormatElement.{Format, Type}
import net.minecraft.client.render.{BufferBuilder, VertexFormat, VertexFormatElement, VertexFormats}
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.{Vector3f, Vector4f}
import net.minecraft.util.math.{MathHelper, Vec2f, Vec3d, Vec3i}

import scala.collection.JavaConverters._

@Environment(EnvType.CLIENT) object MutableVertex {
  private def normalAsByte(norm: Float, offset: Int) = {
    val as = (norm * 0x7f).toInt
    as << offset
  }
}

@Environment(EnvType.CLIENT) class MutableVertex() {
  normal_x = 0
  normal_y = 1
  normal_z = 0
  colour_r = 0xFF
  colour_g = 0xFF
  colour_b = 0xFF
  colour_a = 0xFF
  /** The position of this vertex. */
  var position_x: Float = .0f
  var position_y: Float = .0f
  var position_z: Float = .0f
  /** The normal of this vertex. Might not be normalised. Default value is [0, 1, 0]. */
  var normal_x: Float = .0f
  var normal_y: Float = .0f
  var normal_z: Float = .0f
  /** The colour of this vertex, where each one is a number in the range 0-255. Default value is 255. */
  var colour_r = 0
  var colour_g = 0
  var colour_b = 0
  var colour_a = 0
  /** The texture co-ord of this vertex. Should usually be between 0-1 */
  var tex_u: Float = .0f
  var tex_v: Float = .0f
  /** The light of this vertex. Should be in the range 0-15. */
  var light_block = 0
  var light_sky = 0

  def this(from: MutableVertex) {
    this()
    copyFrom(from)
  }

  def copyFrom(from: MutableVertex): MutableVertex = {
    position_x = from.position_x
    position_y = from.position_y
    position_z = from.position_z
    normal_x = from.normal_x
    normal_y = from.normal_y
    normal_z = from.normal_z
    colour_r = from.colour_r
    colour_g = from.colour_g
    colour_b = from.colour_b
    colour_a = from.colour_a
    tex_u = from.tex_u
    tex_v = from.tex_v
    light_block = from.light_block
    light_sky = from.light_sky
    this
  }

  def this(a: MutableVertex, b: MutableVertex, interp: Float) {
    this()
    position_x = MathHelper.lerp(a.position_x, b.position_x, interp)
    position_y = MathHelper.lerp(a.position_y, b.position_y, interp)
    position_z = MathHelper.lerp(a.position_z, b.position_z, interp)
    normal_x = MathHelper.lerp(a.normal_x, b.normal_x, interp)
    normal_y = MathHelper.lerp(a.normal_y, b.normal_y, interp)
    normal_z = MathHelper.lerp(a.normal_z, b.normal_z, interp)
    colour_r = MathHelper.lerp(a.colour_r, b.colour_r, interp).toShort
    colour_g = MathHelper.lerp(a.colour_g, b.colour_g, interp).toShort
    colour_b = MathHelper.lerp(a.colour_b, b.colour_b, interp).toShort
    colour_a = MathHelper.lerp(a.colour_a, b.colour_a, interp).toShort
    tex_u = MathHelper.lerp(a.tex_u, b.tex_u, interp)
    tex_v = MathHelper.lerp(a.tex_v, b.tex_v, interp)
    light_block = MathHelper.lerp(a.light_block, b.light_block, interp).toByte
    light_sky = MathHelper.lerp(a.light_sky, b.light_sky, interp).toByte
  }

  override def toString: String = "{ pos = [ " + position_x + ", " + position_y + ", " + position_z // + " ], norm = [ " + normal_x + ", " + normal_y + ", " + normal_z + " ], colour = [ " + colour_r + ", " + colour_g + ", " + colour_b + ", " + colour_a + " ], tex = [ " + tex_u + ", " + tex_v + " ], light_block = " + light_block + ", light_sky = " + light_sky + " }"

  def toBakedBlock(data: Array[Int], offset: Int): Unit = { // POSITION_3F
    data(offset + 0) = JFloat.floatToRawIntBits(position_x)
    data(offset + 1) = JFloat.floatToRawIntBits(position_y)
    data(offset + 2) = JFloat.floatToRawIntBits(position_z)
    // COLOR_4UB
    data(offset + 3) = colourRGBA
    // TEX_2F
    data(offset + 4) = JFloat.floatToRawIntBits(tex_u)
    data(offset + 5) = JFloat.floatToRawIntBits(tex_v)
    // TEX_2S
    data(offset + 6) = lightc
  }

  def toBakedItem(data: Array[Int], offset: Int): Unit = {
    data(offset + 0) = JFloat.floatToRawIntBits(position_x)
    data(offset + 1) = JFloat.floatToRawIntBits(position_y)
    data(offset + 2) = JFloat.floatToRawIntBits(position_z)
    data(offset + 3) = colourRGBA
    data(offset + 4) = JFloat.floatToRawIntBits(tex_u)
    data(offset + 5) = JFloat.floatToRawIntBits(tex_v)
    // NORMAL_3B
    data(offset + 6) = normalToPackedInt
  }

  def normalToPackedInt: Int = MutableVertex.normalAsByte(normal_x, 0) | MutableVertex.normalAsByte(normal_y, 8) | MutableVertex.normalAsByte(normal_z, 16)

  def colourRGBA: Int = {
    var rgba = 0
    rgba |= (colour_r & 0xFF) << 0
    rgba |= (colour_g & 0xFF) << 8
    rgba |= (colour_b & 0xFF) << 16
    rgba |= (colour_a & 0xFF) << 24
    rgba
  }

  def fromBakedBlock(data: Array[Int], offset: Int): Unit = {
    position_x = JFloat.intBitsToFloat(data(offset + 0))
    position_y = JFloat.intBitsToFloat(data(offset + 1))
    position_z = JFloat.intBitsToFloat(data(offset + 2))
    colouri(data(offset + 3))
    tex_u = JFloat.intBitsToFloat(data(offset + 4))
    tex_v = JFloat.intBitsToFloat(data(offset + 5))
    lighti(data(offset + 6))
    normalf(0, 1, 0)
  }

  def fromBakedItem(data: Array[Int], offset: Int): Unit = {
    position_x = JFloat.intBitsToFloat(data(offset + 0))
    position_y = JFloat.intBitsToFloat(data(offset + 1))
    position_z = JFloat.intBitsToFloat(data(offset + 2))
    colouri(data(offset + 3))
    tex_u = JFloat.intBitsToFloat(data(offset + 4))
    tex_v = JFloat.intBitsToFloat(data(offset + 5))
    normali(data(offset + 6))
    lightf(1, 1)
  }

  def fromBakedFormat(data: Array[Int], format: VertexFormat, offset: Int): Unit = {
    var o = offset
    for (elem <- format.getElements.asScala) {
      elem.getType match {
        case VertexFormatElement.Type.POSITION =>
          assert(elem.getFormat == Format.FLOAT)
          position_x = JFloat.intBitsToFloat(data({
            o += 1;
            o - 1
          }))
          position_y = JFloat.intBitsToFloat(data({
            o += 1;
            o - 1
          }))
          position_z = JFloat.intBitsToFloat(data({
            o += 1;
            o - 1
          }))

        case VertexFormatElement.Type.COLOR =>
          assert(elem.getFormat == Format.UBYTE)
          colouri(data({
            o += 1;
            o - 1
          }))

        case VertexFormatElement.Type.NORMAL =>
          assert(elem.getFormat == Format.BYTE)
          normali(data({
            o += 1;
            o - 1
          }))

        case VertexFormatElement.Type.UV =>
          if (elem.getIndex == 0) {
            tex_u = JFloat.intBitsToFloat(data({
              o += 1;
              o - 1
            }))
            tex_v = JFloat.intBitsToFloat(data({
              o += 1;
              o - 1
            }))
          }
          else if (elem.getIndex == 1) {
            lighti(data({
              o += 1;
              o - 1
            }))
          }
        case _ =>
      }
    }
  }

  def normali(combined: Int): MutableVertex = {
    normal_x = ((combined >> 0) & 0xFF) / 0x7f
    normal_y = ((combined >> 8) & 0xFF) / 0x7f
    normal_z = ((combined >> 16) & 0xFF) / 0x7f
    this
  }

  def colouri(rgba: Int): MutableVertex = colouri(rgba, rgba >> 8, rgba >> 16, rgba >>> 24)

  def lighti(combined: Int): MutableVertex = lighti(combined >> 4, combined >> 20)

  def putData(vertexIndex: Int, emitter: QuadEmitter): Unit = {
    emitter.pos(vertexIndex, position_x, position_y, position_z)
    emitter.spriteColor(vertexIndex, 0, colourBGRA)
    emitter.sprite(vertexIndex, 0, tex_u, tex_v)
    emitter.normal(vertexIndex, normal_x, normal_y, normal_z)
    emitter.lightmap(vertexIndex, lightc)
  }

  def colourBGRA: Int = {
    var rgba = 0
    rgba |= (colour_a & 0xFF) << 24
    rgba |= (colour_r & 0xFF) << 16
    rgba |= (colour_g & 0xFF) << 8
    rgba |= (colour_b & 0xFF) << 0
    rgba
  }

  def lightc: Int = light_block << 4 + light_sky << 20

  def render(bb: BufferBuilder): Unit = {
    val vf = bb.getVertexFormat
    if (vf == VertexFormats.POSITION_COLOR_UV_NORMAL) renderAsBlock(bb)
    else {
      for (vfe <- vf.getElements.asScala) {
        if (vfe.isPosition) renderPosition(bb)
        else if (vfe.getType == Type.NORMAL) renderNormal(bb)
        else if (vfe.getType == Type.COLOR) renderColour(bb)
        else if (vfe.getType == Type.UV) if (vfe.getIndex == 0) renderTex(bb)
        else if (vfe.getIndex == 1) renderLightMap(bb)
      }
      bb.next()
    }
  }

  def renderAsBlock(bb: BufferBuilder): Unit = {
    renderPosition(bb)
    renderColour(bb)
    renderTex(bb)
    renderLightMap(bb)
    bb.next()
  }

  def renderColour(bb: BufferBuilder): Unit = {
    bb.color(colour_r, colour_g, colour_b, colour_a)
  }

  def renderTex(bb: BufferBuilder): Unit = {
    bb.texture(tex_u, tex_v)
  }

  def renderLightMap(bb: BufferBuilder): Unit = {
    bb.texture(light_sky << 4, light_block << 4)
  }

  def renderPosition(bb: BufferBuilder): Unit = {
    bb.vertex(position_x, position_y, position_z)
  }

  def renderNormal(bb: BufferBuilder): Unit = {
    bb.normal(normal_x, normal_y, normal_z)
  }

  def positionv(vec: Vector3f): MutableVertex = positionf(vec.getX, vec.getY, vec.getZ)

  def positionf(x: Float, y: Float, z: Float): MutableVertex = {
    position_x = x
    position_y = y
    position_z = z
    this
  }

  def positionv(vec: Vec3d): MutableVertex = positiond(vec.x, vec.y, vec.z)

  def positiond(x: Double, y: Double, z: Double): MutableVertex = positionf(x.toFloat, y.toFloat, z.toFloat)

  def positionvf = new Vector3f(position_x, position_y, position_z)

  def positionvd = new Vec3d(position_x, position_y, position_z)

  def normalv(vec: Vector3f): MutableVertex = normalf(vec.getX, vec.getY, vec.getZ)

  def invertNormal: MutableVertex = normalf(-normal_x, -normal_y, -normal_z)

  /** Sets the current normal given the x, y, and z coordinates. These are NOT normalised or checked. */
  def normalf(x: Float, y: Float, z: Float): MutableVertex = {
    normal_x = x
    normal_y = y
    normal_z = z
    this
  }

  /** @return The current normal vector of this vertex. This might be normalised. */
  def normal = new Vector3f(normal_x, normal_y, normal_z)

  def colourv(vec: Vector4f): MutableVertex = colourf(vec.getX, vec.getY, vec.getZ, vec.getW)

  def colourf(r: Float, g: Float, b: Float, a: Float): MutableVertex = colouri((r * 0xFF).toInt, (g * 0xFF).toInt, (b * 0xFF).toInt, (a * 0xFF).toInt)

  def colouri(r: Int, g: Int, b: Int, a: Int): MutableVertex = {
    colour_r = (r & 0xFF).toShort
    colour_g = (g & 0xFF).toShort
    colour_b = (b & 0xFF).toShort
    colour_a = (a & 0xFF).toShort
    this
  }

  def colourv = new Vector4f(colour_r / 255f, colour_g / 255f, colour_b / 255f, colour_a / 255f)

  def colourABGR: Int = {
    var rgba = 0
    rgba |= (colour_r & 0xFF) << 24
    rgba |= (colour_g & 0xFF) << 16
    rgba |= (colour_b & 0xFF) << 8
    rgba |= (colour_a & 0xFF) << 0
    rgba
  }

  def multColourd(r: Double, g: Double, b: Double, a: Double): MutableVertex = multColouri((r * 255).toInt, (g * 255).toInt, (b * 255).toInt, (a * 255).toInt)

  def multColouri(r: Int, g: Int, b: Int, a: Int): MutableVertex = {
    colour_r = (colour_r * r / 255).toShort
    colour_g = (colour_g * g / 255).toShort
    colour_b = (colour_b * b / 255).toShort
    colour_a = (colour_a * a / 255).toShort
    this
  }

  /** Multiplies the colour by {@link MutableQuad#diffuseLight(float, float, float)} for the normal. */
  def multShade: MutableVertex = multColourd(MutableQuad.diffuseLight(normal_x, normal_y, normal_z))

  def multColourd(d: Double): MutableVertex = {
    val m = (d * 255).toInt
    multColouri(m)
  }

  def multColouri(by: Int): MutableVertex = multColouri(by, by, by, 255)

  def texFromSprite(sprite: Sprite): MutableVertex = {
    tex_u = sprite.getU(tex_u * 16)
    tex_v = sprite.getV(tex_v * 16)
    this
  }

  def texFromSpriteRaw(sprite: Sprite): MutableVertex = {
    tex_u = sprite.getU(tex_u)
    tex_v = sprite.getV(tex_v)
    this
  }

  def texv(vec: Vec2f): MutableVertex = texf(vec.x, vec.y)

  def texf(u: Float, v: Float): MutableVertex = {
    tex_u = u
    tex_v = v
    this
  }

  def tex = new Vec2f(tex_u, tex_v)

  def lightv(vec: Vec2f): MutableVertex = lightf(vec.x, vec.y)

  def lightf(block: Float, sky: Float): MutableVertex = lighti((block * 0xF).toInt, (sky * 0xF).toInt)

  def lighti(block: Int, sky: Int): MutableVertex = {
    light_block = block.toByte
    light_sky = sky.toByte
    this
  }

  def maxLighti(block: Int, sky: Int): MutableVertex = lighti(Math.max(block, light_block), Math.max(sky, light_sky))

  def lightvf = new Vec2f(light_block * 15f, light_sky * 15f)

  def lighti(): Array[Int] = Array[Int](light_block, light_sky)

  def translatef(x: Float, y: Float, z: Float): MutableVertex = {
    position_x += x
    position_y += y
    position_z += z
    this
  }

  def translatevi(vec: Vec3i): MutableVertex = translatei(vec.getX, vec.getY, vec.getZ)

  // public MutableVertex transform(Matrix4f matrix) {
  // positionv(MatrixUtil.transform(matrix, positionvf()));
  // normalv(MatrixUtil.transform(matrix, normal()));
  // return this;
  def translatei(x: Int, y: Int, z: Int): MutableVertex = {
    position_x += x
    position_y += y
    position_z += z
    this
  }

  def translatevd(vec: Vec3d): MutableVertex = translated(vec.x, vec.y, vec.z)

  def translated(x: Double, y: Double, z: Double): MutableVertex = {
    position_x += x.toFloat
    position_y += y.toFloat
    position_z += z.toFloat
    this
  }

  def scaled(scale: Double): MutableVertex = scalef(scale.toFloat)

  def scalef(scale: Float): MutableVertex = {
    position_x *= scale
    position_y *= scale
    position_z *= scale
    this
  }

  def scaled(x: Double, y: Double, z: Double): MutableVertex = scalef(x.toFloat, y.toFloat, z.toFloat)

  def scalef(x: Float, y: Float, z: Float): MutableVertex = {
    position_x *= x
    position_y *= y
    position_z *= z
    // TODO: scale normals?
    this
  }

  /** Rotates around the X axis by angle. */
  def rotateX(angle: Float): Unit = {
    val cos = MathHelper.cos(angle)
    val sin = MathHelper.sin(angle)
    rotateDirectlyX(cos, sin)
  }

  def rotateDirectlyX(cos: Float, sin: Float): Unit = {
    val y = position_y
    val z = position_z
    position_y = y * cos - z * sin
    position_z = y * sin + z * cos
  }

  /** Rotates around the Y axis by angle. */
  def rotateY(angle: Float): Unit = {
    val cos = MathHelper.cos(angle)
    val sin = MathHelper.sin(angle)
    rotateDirectlyY(cos, sin)
  }

  def rotateDirectlyY(cos: Float, sin: Float): Unit = {
    val x = position_x
    val z = position_z
    position_x = x * cos - z * sin
    position_z = x * sin + z * cos
  }

  /** Rotates around the Z axis by angle. */
  def rotateZ(angle: Float): Unit = {
    val cos = MathHelper.cos(angle)
    val sin = MathHelper.sin(angle)
    rotateDirectlyZ(cos, sin)
  }

  def rotateDirectlyZ(cos: Float, sin: Float): Unit = {
    val x = position_x
    val y = position_y
    position_x = x * cos + y * sin
    position_y = x * -sin + y * cos
  }

  /** Rotates this vertex around the X axis 90 degrees.
   *
   * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
   *              anti-clockwise. */
  def rotateX_90(scale: Float): MutableVertex = {
    val ym = scale
    val zm = -ym
    var t = position_y * ym
    position_y = position_z * zm
    position_z = t
    t = normal_y * ym
    normal_y = normal_z * zm
    normal_z = t
    this
  }

  /** Rotates this vertex around the Y axis 90 degrees.
   *
   * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
   *              anti-clockwise. */
  def rotateY_90(scale: Float): MutableVertex = {
    val xm = scale
    val zm = -xm
    var t = position_x * xm
    position_x = position_z * zm
    position_z = t
    t = normal_x * xm
    normal_x = normal_z * zm
    normal_z = t
    this
  }

  /** Rotates this vertex around the Z axis 90 degrees.
   *
   * @param scale The multiplier for scaling. Positive values will rotate clockwise, negative values rotate
   *              anti-clockwise. */
  def rotateZ_90(scale: Float): MutableVertex = {
    val xm = scale
    val ym = -xm
    var t = position_x * xm
    position_x = position_y * ym
    position_y = t
    t = normal_x * xm
    normal_x = normal_y * ym
    normal_y = t
    this
  }

  /** Rotates this vertex around the X axis by 180 degrees. */
  def rotateX_180: MutableVertex = {
    position_y = -position_y
    position_z = -position_z
    normal_y = -normal_y
    normal_z = -normal_z
    this
  }

  /** Rotates this vertex around the Y axis by 180 degrees. */
  def rotateY_180: MutableVertex = {
    position_x = -position_x
    position_z = -position_z
    normal_x = -normal_x
    normal_z = -normal_z
    this
  }

  /** Rotates this vertex around the Z axis by 180 degrees. */
  def rotateZ_180: MutableVertex = {
    position_x = -position_x
    position_y = -position_y
    normal_x = -normal_x
    normal_y = -normal_y
    this
  }
}
