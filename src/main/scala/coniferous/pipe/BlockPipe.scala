package coniferous.pipe

import coniferous.block.{BaseBlock, HasBE}
import coniferous.shade.io.github.kvverti.msu.WorldAccess._
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block._
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.{EntityContext, LivingEntity}
import net.minecraft.item.ItemStack
import net.minecraft.util.BooleanBiFunction
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.world.{BlockView, World}

import scala.collection.JavaConverters._
import scala.collection.immutable.BitSet

class BlockPipe(val kind: PipeEntity.PipeType.Value) extends BaseBlock(s"pipe_${kind.toString}",
  FabricBlockSettings.of(Material.METAL).breakByHand(true).build()) with BlockEntityProvider
  /*with MachineGUIBlockActivationSkeleton[PipeEntity]*/ with HasBE[PipeEntity] {

  val CENTER_SHAPE: VoxelShape = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)
  private val FACE_SHAPES = Array.ofDim[VoxelShape](6)
  private val FACE_CENTER_SHAPES = Array.ofDim[VoxelShape](6)
  private val SHAPES = Array.ofDim[VoxelShape](1 << 6)

  for (dir <- Direction.values) {
    val x = 0.5 + dir.getOffsetX * 0.375
    val y = 0.5 + dir.getOffsetY * 0.375
    val z = 0.5 + dir.getOffsetZ * 0.375
    val rx = if (dir.getAxis == Axis.X) 0.125 else 0.25
    val ry = if (dir.getAxis == Axis.Y) 0.125 else 0.25
    val rz = if (dir.getAxis == Axis.Z) 0.125 else 0.25
    val faceShape = VoxelShapes.cuboid(x - rx, y - ry, z - rz, x + rx, y + ry, z + rz)
    FACE_SHAPES(dir.ordinal) = faceShape
    FACE_CENTER_SHAPES(dir.ordinal) = VoxelShapes.union(faceShape, CENTER_SHAPE)
  }

  for (c <- 0 to 0x3f) {
    var shape = CENTER_SHAPE
    for (dir <- Direction.values) {
      if ((c & (1 << dir.ordinal)) != 0) shape = VoxelShapes.combine(shape, FACE_SHAPES(dir.ordinal), BooleanBiFunction.OR)
    }
    SHAPES(c) = shape.simplify
  }

  //  override protected def beCTag: ClassTag[PipeEntity] = classTag

  override protected def beType: BlockEntityType[PipeEntity] = PipeEntity.tpe

  override def getRenderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override def getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, entityPos: EntityContext): VoxelShape =
    view.typedBlockEntity[PipeEntity](pos).map { pipe =>
      if (pipe.connections.isEmpty) CENTER_SHAPE
      else SHAPES(pipe.connections.asScala.map(_.getId).foldLeft(BitSet.empty)((s, i) => s + i).toBitMask(0)
        .toInt)
    }.getOrElse(CENTER_SHAPE)


  override def neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, neighbor: BlockPos, thing: Boolean): Unit = {
    world.typedBlockEntity[PipeEntity](pos).foreach(entity => {
      entity.initialize()
    })
  }

  override def onPlaced(world: World, pos: BlockPos, state: BlockState, entity: LivingEntity, stack: ItemStack): Unit = {
    super.onPlaced(world, pos, state, entity, stack)
    world.typedBlockEntity[PipeEntity](pos).foreach(be => be.initialize())
  }
}

object BlockPipe {
  val kinds: Map[PipeEntity.PipeType.Value, BlockPipe] = PipeEntity.PipeType.values.map(kind => kind -> new BlockPipe(kind)).toMap

  def init(): Unit = {}
}
