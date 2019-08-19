package coniferous.pipe

import coniferous.Coniferous
import coniferous.block.{BaseBlock, HasBE, MachineGUIBlockActivationSkeleton}
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.{Block, BlockEntityProvider, BlockRenderLayer, BlockState, Material}
import net.minecraft.entity.EntityContext
import net.minecraft.state.StateFactory
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.util.math.Direction.Axis
import net.minecraft.util.shape.{VoxelShape, VoxelShapes}
import net.minecraft.world.BlockView

import scala.collection.JavaConverters._
import scala.collection.immutable.BitSet
import scala.reflect.{ClassTag, classTag}

object BlockPipe extends BaseBlock("pipe", None,
  FabricBlockSettings.of(Material.METAL).breakByHand(true).build()) with BlockEntityProvider
  with MachineGUIBlockActivationSkeleton[PipeEntity] with HasBE[PipeEntity] {

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

  override def appendProperties(builder: StateFactory.Builder[Block, BlockState]): Unit = {
    builder.add(Coniferous.Properties.KIND_PROPERTY)
    super.appendProperties(builder)
  }

  override protected def beCTag: ClassTag[PipeEntity] = classTag

  override protected def beType: BlockEntityType[PipeEntity] = PipeEntity.tpe

  override def getRenderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override def getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, entityPos: EntityContext): VoxelShape = {
    val be = view.getBlockEntity(pos)
    be match {
      case pipe: PipeEntity =>
        if (pipe.connections.isEmpty) return CENTER_SHAPE
        return SHAPES(pipe.connections.asScala.map(_.getId).toSet.foldLeft(BitSet.empty)((s, i) => s + i).toBitMask(0)
          .toInt)
      case _ =>
    }
    CENTER_SHAPE
  }
}
