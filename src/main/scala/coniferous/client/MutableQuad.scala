package coniferous.client

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.client.render.{BufferBuilder, VertexFormat}
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.math.{Direction, Vec2f, Vec3d, Vec3i}

object MutableQuad {
  val EMPTY_ARRAY = new Array[MutableQuad](0)

  def diffuseLight(normal: Vector3f): Float = diffuseLight(normal.getX, normal.getY, normal.getZ)

  def diffuseLight(x: Float, y: Float, z: Float): Float = {
    val up = y >= 0
    val xx = x * x
    val yy = y * y
    val zz = z * z
    val t = xx + yy + zz
    var light = (xx * 0.6f + zz * 0.8f) / t
    var yyt = yy / t
    if (!up) yyt *= 0.5f
    light += yyt
    light
  }
}

class MutableQuad() {
  final val vertex_0 = new MutableVertex()
  final val vertex_1 = new MutableVertex()
  final val vertex_2 = new MutableVertex()
  final val vertex_3 = new MutableVertex()
  private var colourIndex = -1
  private var face: Direction = _
  private var sprite: Sprite = _

  def this(tintIndex: Int, face: Direction) {
    this()
    this.colourIndex = tintIndex
    this.face = face
  }

  def setTint(tint: Int): MutableQuad = {
    colourIndex = tint
    this
  }

  def getColourIndex: Int = colourIndex

  def setFace(face: Nothing): MutableQuad = {
    this.face = face
    this
  }

  def getFace: Direction = face

  def getSprite: Sprite = this.sprite

  def setSprite(sprite: Sprite): Unit = {
    this.sprite = sprite
  }

  def toBakedBlock: BakedQuad = {
    val data = new Array[Int](28)
    vertex_0.toBakedBlock(data, 0)
    vertex_1.toBakedBlock(data, 7)
    vertex_2.toBakedBlock(data, 14)
    vertex_3.toBakedBlock(data, 21)
    new BakedQuad(data, colourIndex, face, sprite)
  }

  def toBakedItem: BakedQuad = {
    val data = new Array[Int](28)
    vertex_0.toBakedItem(data, 0)
    vertex_1.toBakedItem(data, 7)
    vertex_2.toBakedItem(data, 14)
    vertex_3.toBakedItem(data, 21)
    new BakedQuad(data, colourIndex, face, sprite)
  }

  def fromBakedBlock(quad: BakedQuad): MutableQuad = {
    colourIndex = quad.getColorIndex
    face = quad.getFace
    sprite = quad.getSprite
    val data = quad.getVertexData
    val stride = data.length / 4
    vertex_0.fromBakedBlock(data, 0)
    vertex_1.fromBakedBlock(data, stride)
    vertex_2.fromBakedBlock(data, stride * 2)
    vertex_3.fromBakedBlock(data, stride * 3)
    this
  }

  def fromBakedItem(quad: BakedQuad): MutableQuad = {
    colourIndex = quad.getColorIndex
    face = quad.getFace
    sprite = quad.getSprite
    val data = quad.getVertexData
    val stride = data.length / 4
    vertex_0.fromBakedItem(data, 0)
    vertex_1.fromBakedItem(data, stride)
    vertex_2.fromBakedItem(data, stride * 2)
    vertex_3.fromBakedItem(data, stride * 3)
    this
  }

  def fromBakedFormat(quad: BakedQuad, format: VertexFormat): MutableQuad = {
    colourIndex = quad.getColorIndex
    face = quad.getFace
    sprite = quad.getSprite
    val data = quad.getVertexData
    val stride = data.length / 4
    vertex_0.fromBakedFormat(data, format, 0)
    vertex_1.fromBakedFormat(data, format, stride)
    vertex_2.fromBakedFormat(data, format, stride * 2)
    vertex_3.fromBakedFormat(data, format, stride * 3)
    this
  }

  def render(bb: BufferBuilder): Unit = {
    vertex_0.render(bb)
    vertex_1.render(bb)
    vertex_2.render(bb)
    vertex_3.render(bb)
  }

  def putData(emitter: QuadEmitter): Unit = {
    vertex_0.putData(0, emitter)
    vertex_1.putData(1, emitter)
    vertex_2.putData(2, emitter)
    vertex_3.putData(3, emitter)
  }

  def setCalculatedNormal(): Unit = {
    normalvf(getCalculatedNormal)
  }

  def getCalculatedNormal: Vector3f = {
    val a = new Vector3f(vertex_1.positionvf)
    a.subtract(vertex_0.positionvf)
    val b = new Vector3f(vertex_2.positionvf)
    b.subtract(vertex_0.positionvf)
    a.cross(b)
    a
  }

  def normalvf(vec: Vector3f): MutableQuad = normalf(vec.getX, vec.getY, vec.getZ)

  /** Sets the normal for all vertices to the specified float coordinates. */
  // ############################
  //
  // Delegate vertex functions
  // Basically a lot of functions that
  // change every vertex in the same way
  /* Position */
  // Note that you cannot set all of the position elements at once, so this is left empty
  /* Normal */ def normalf(x: Float, y: Float, z: Float): MutableQuad = {
    vertex_0.normalf(x, y, z)
    vertex_1.normalf(x, y, z)
    vertex_2.normalf(x, y, z)
    vertex_3.normalf(x, y, z)
    this
  }

  def setDiffuse(normal: Vector3f): Unit = {
    val diffuse = MutableQuad.diffuseLight(normal)
    colourf(diffuse, diffuse, diffuse, 1)
  }

  def colourf(r: Float, g: Float, b: Float, a: Float): MutableQuad = {
    vertex_0.colourf(r, g, b, a)
    vertex_1.colourf(r, g, b, a)
    vertex_2.colourf(r, g, b, a)
    vertex_3.colourf(r, g, b, a)
    this
  }

  def setCalculatedDiffuse(): Unit = {
    val diffuse = getCalculatedDiffuse
    colourf(diffuse, diffuse, diffuse, 1)
  }

  def getCalculatedDiffuse: Float = MutableQuad.diffuseLight(getCalculatedNormal)

  /** Inverts a copy of this quad's normal so that it will render in the opposite direction. You will need to recall
   * diffusion calculations if you had previously calculated the diffuse. */
  def copyAndInvertNormal: MutableQuad = {
    val copy = new MutableQuad(this)
    copy.vertex_0.copyFrom(vertex_3).invertNormal
    copy.vertex_1.copyFrom(vertex_2).invertNormal
    copy.vertex_2.copyFrom(vertex_1).invertNormal
    copy.vertex_3.copyFrom(vertex_0).invertNormal
    copy
  }

  def this(from: MutableQuad) {
    this()
    copyFrom(from)
  }

  def copyFrom(from: MutableQuad): MutableQuad = {
    colourIndex = from.colourIndex
    face = from.face
    sprite = from.sprite
    vertex_0.copyFrom(from.vertex_0)
    vertex_1.copyFrom(from.vertex_1)
    vertex_2.copyFrom(from.vertex_2)
    vertex_3.copyFrom(from.vertex_3)
    this
  }

  def rotateTextureUp(times: Int): MutableQuad = times & 3 match {
    case 0 =>
      this

    case 1 =>
      val t = vertex_0.tex
      vertex_0.texv(vertex_1.tex)
      vertex_1.texv(vertex_2.tex)
      vertex_2.texv(vertex_3.tex)
      vertex_3.texv(t)
      this

    case 2 =>
      val t0 = vertex_0.tex
      val t1 = vertex_1.tex
      vertex_0.texv(vertex_2.tex)
      vertex_1.texv(vertex_3.tex)
      vertex_2.texv(t0)
      vertex_3.texv(t1)
      this

    case 3 =>
      val t = vertex_3.tex
      vertex_3.texv(vertex_2.tex)
      vertex_2.texv(vertex_1.tex)
      vertex_1.texv(vertex_0.tex)
      vertex_0.texv(t)
      this

    case _ =>
      throw new IllegalStateException("'times & 3' was not 0, 1, 2 or 3!")

  }

  def normalvd(vec: Vec3d): MutableQuad = normald(vec.x, vec.y, vec.z)

  def normald(x: Double, y: Double, z: Double): MutableQuad = normalf(x.toFloat, y.toFloat, z.toFloat)

  def normalvf = new Vector3f(vertex_0.normal_x, vertex_0.normal_y, vertex_0.normal_z)

  def normalvd = new Vec3d(vertex_0.normal_x, vertex_0.normal_y, vertex_0.normal_z)

  /* Colour */ def colouri(r: Int, g: Int, b: Int, a: Int): MutableQuad = {
    vertex_0.colouri(r, g, b, a)
    vertex_1.colouri(r, g, b, a)
    vertex_2.colouri(r, g, b, a)
    vertex_3.colouri(r, g, b, a)
    this
  }

  def colouri(rgba: Int): MutableQuad = {
    vertex_0.colouri(rgba)
    vertex_1.colouri(rgba)
    vertex_2.colouri(rgba)
    vertex_3.colouri(rgba)
    this
  }

  // public MutableQuad colourvl(VecLong vec) {
  // return colouri((int) vec.a, (int) vec.b, (int) vec.c, (int) vec.d);
  // public MutableQuad colourvf(Tuple4f vec) {
  // return colourf(vec.x, vec.y, vec.z, vec.w);
  def multColourd(r: Double, g: Double, b: Double, a: Double): MutableQuad = {
    vertex_0.multColourd(r, g, b, a)
    vertex_1.multColourd(r, g, b, a)
    vertex_2.multColourd(r, g, b, a)
    vertex_3.multColourd(r, g, b, a)
    this
  }

  def multColourd(by: Double): MutableQuad = {
    val m = (by * 255).toInt
    multColouri(m)
  }

  def multColouri(by: Int): MutableQuad = {
    vertex_0.multColouri(by)
    vertex_1.multColouri(by)
    vertex_2.multColouri(by)
    vertex_3.multColouri(by)
    this
  }

  def multColouri(r1: Int, g1: Int, b1: Int, a1: Int): MutableQuad = {
    val r = r1 & 0xFF
    val g = g1 & 0xFF
    val b = b1 & 0xFF
    val a = a1 & 0xFF
    vertex_0.multColouri(r, g, b, a)
    vertex_1.multColouri(r, g, b, a)
    vertex_2.multColouri(r, g, b, a)
    vertex_3.multColouri(r, g, b, a)
    this
  }

  def multShade: MutableQuad = {
    vertex_0.multShade
    vertex_1.multShade
    vertex_2.multShade
    vertex_3.multShade
    this
  }

  /* Texture co-ords */ def texFromSprite(sprite: Sprite): MutableQuad = {
    vertex_0.texFromSprite(sprite)
    vertex_1.texFromSprite(sprite)
    vertex_2.texFromSprite(sprite)
    vertex_3.texFromSprite(sprite)
    this
  }

  def texFromSpriteRaw(sprite: Sprite): MutableQuad = {
    vertex_0.texFromSpriteRaw(sprite)
    vertex_1.texFromSpriteRaw(sprite)
    vertex_2.texFromSpriteRaw(sprite)
    vertex_3.texFromSpriteRaw(sprite)
    this
  }

  def lighti(combined: Int): MutableQuad = {
    vertex_0.lighti(combined)
    vertex_1.lighti(combined)
    vertex_2.lighti(combined)
    vertex_3.lighti(combined)
    this
  }

  def lightvf(vec: Vec2f): MutableQuad = lightf(vec.x, vec.y)

  def lightf(block: Float, sky: Float): MutableQuad = lighti((block * 15).toInt, (sky * 15).toInt)

  /* Lightmap texture co-ords */ def lighti(block: Int, sky: Int): MutableQuad = {
    vertex_0.lighti(block, sky)
    vertex_1.lighti(block, sky)
    vertex_2.lighti(block, sky)
    vertex_3.lighti(block, sky)
    this
  }

  /** Sets the current light value of every vertex to be the maximum of the given in value, and the current value */
  def maxLighti(block: Int, sky: Int): MutableQuad = {
    vertex_0.maxLighti(block, sky)
    vertex_1.maxLighti(block, sky)
    vertex_2.maxLighti(block, sky)
    vertex_3.maxLighti(block, sky)
    this
  }

  def translatevi(vec: Vec3i): MutableQuad = translatei(vec.getX, vec.getY, vec.getZ)

  /* Transforms */
  // public MutableQuad transform(Matrix4f transformation) {
  // vertex_0.transform(transformation);
  // vertex_1.transform(transformation);
  // vertex_2.transform(transformation);
  // vertex_3.transform(transformation);
  // return this;
  def translatei(x: Int, y: Int, z: Int): MutableQuad = translatef(x, y, z)

  def translatef(x: Float, y: Float, z: Float): MutableQuad = {
    vertex_0.translatef(x, y, z)
    vertex_1.translatef(x, y, z)
    vertex_2.translatef(x, y, z)
    vertex_3.translatef(x, y, z)
    this
  }

  def translatevf(vec: Vector3f): MutableQuad = translatef(vec.getX, vec.getY, vec.getZ)

  def translatevd(vec: Vec3d): MutableQuad = translated(vec.x, vec.y, vec.z)

  def translated(x: Double, y: Double, z: Double): MutableQuad = translatef(x.toFloat, y.toFloat, z.toFloat)

  def scaled(scale: Double): MutableQuad = scalef(scale.toFloat)

  def scalef(scale: Float): MutableQuad = {
    vertex_0.scalef(scale)
    vertex_1.scalef(scale)
    vertex_2.scalef(scale)
    vertex_3.scalef(scale)
    this
  }

  def scaled(x: Double, y: Double, z: Double): MutableQuad = scalef(x.toFloat, y.toFloat, z.toFloat)

  def scalef(x: Float, y: Float, z: Float): MutableQuad = {
    vertex_0.scalef(x, y, z)
    vertex_1.scalef(x, y, z)
    vertex_2.scalef(x, y, z)
    vertex_3.scalef(x, y, z)
    this
  }

  def rotateX(angle: Float): Unit = {
    vertex_0.rotateX(angle)
    vertex_1.rotateX(angle)
    vertex_2.rotateX(angle)
    vertex_3.rotateX(angle)
  }

  def rotateY(angle: Float): Unit = {
    vertex_0.rotateY(angle)
    vertex_1.rotateY(angle)
    vertex_2.rotateY(angle)
    vertex_3.rotateY(angle)
  }

  def rotateZ(angle: Float): Unit = {
    vertex_0.rotateZ(angle)
    vertex_1.rotateZ(angle)
    vertex_2.rotateZ(angle)
    vertex_3.rotateZ(angle)
  }

  def rotateDirectlyX(cos: Float, sin: Float): Unit = {
    vertex_0.rotateDirectlyX(cos, sin)
    vertex_1.rotateDirectlyX(cos, sin)
    vertex_2.rotateDirectlyX(cos, sin)
    vertex_3.rotateDirectlyX(cos, sin)
  }

  def rotateDirectlyY(cos: Float, sin: Float): Unit = {
    vertex_0.rotateDirectlyY(cos, sin)
    vertex_1.rotateDirectlyY(cos, sin)
    vertex_2.rotateDirectlyY(cos, sin)
    vertex_3.rotateDirectlyY(cos, sin)
  }

  def rotateDirectlyZ(cos: Float, sin: Float): Unit = {
    vertex_0.rotateDirectlyZ(cos, sin)
    vertex_1.rotateDirectlyZ(cos, sin)
    vertex_2.rotateDirectlyZ(cos, sin)
    vertex_3.rotateDirectlyZ(cos, sin)
  }

  def rotate(from: Direction, to: Direction, ox: Float, oy: Float, oz: Float): MutableQuad = {
    if (from eq to) { // don't bother rotating: there is nothing to rotate!
      return this
    }
    translatef(-ox, -oy, -oz)
    // @formatter:off
    from.getAxis match {
      case Direction.Axis.X =>
        val mult = from.getOffsetX
        to.getAxis match {
          case Direction.Axis.X =>
            rotateY_180
          case Direction.Axis.Y =>
            rotateZ_90(mult * to.getOffsetY)
          case Direction.Axis.Z =>
            rotateY_90(mult * to.getOffsetZ)
        }
      case Direction.Axis.Y =>
        val mult = from.getOffsetY
        to.getAxis match {
          case Direction.Axis.X =>
            rotateZ_90(-mult * to.getOffsetX)
          case Direction.Axis.Y =>
            rotateZ_180
          case Direction.Axis.Z =>
            rotateX_90(mult * to.getOffsetZ)
        }
      case Direction.Axis.Z =>
        val mult = -from.getOffsetZ
        to.getAxis match {
          case Direction.Axis.X =>
            rotateY_90(mult * to.getOffsetX)
          case Direction.Axis.Y =>
            rotateX_90(mult * to.getOffsetY)
          case Direction.Axis.Z =>
            rotateY_180
        }
    }
    // @formatter:on
    translatef(ox, oy, oz)
    this
  }

  def rotateX_90(scale: Float): MutableQuad = {
    vertex_0.rotateX_90(scale)
    vertex_1.rotateX_90(scale)
    vertex_2.rotateX_90(scale)
    vertex_3.rotateX_90(scale)
    this
  }

  def rotateY_90(scale: Float): MutableQuad = {
    vertex_0.rotateY_90(scale)
    vertex_1.rotateY_90(scale)
    vertex_2.rotateY_90(scale)
    vertex_3.rotateY_90(scale)
    this
  }

  def rotateZ_90(scale: Float): MutableQuad = {
    vertex_0.rotateZ_90(scale)
    vertex_1.rotateZ_90(scale)
    vertex_2.rotateZ_90(scale)
    vertex_3.rotateZ_90(scale)
    this
  }

  def rotateY_180: MutableQuad = {
    vertex_0.rotateY_180
    vertex_1.rotateY_180
    vertex_2.rotateY_180
    vertex_3.rotateY_180
    this
  }

  def rotateZ_180: MutableQuad = {
    vertex_0.rotateZ_180
    vertex_1.rotateZ_180
    vertex_2.rotateZ_180
    vertex_3.rotateZ_180
    this
  }

  def rotateX_180: MutableQuad = {
    vertex_0.rotateX_180
    vertex_1.rotateX_180
    vertex_2.rotateX_180
    vertex_3.rotateX_180
    this
  }

  override def toString: String = "MutableQuad [vertices=" + vToS + ", tintIndex=" + colourIndex + ", face=" + face + "]"

  private def vToS = "[ " + vertex_0 + ", " + vertex_1 + ", " + vertex_2 + ", " + vertex_3 + " ]"
}
