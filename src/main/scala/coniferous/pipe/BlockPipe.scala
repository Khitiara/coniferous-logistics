package coniferous.pipe

import coniferous.block.{BaseBlock, EnumerationProperty, HasBE, MachineGUIBlockActivationSkeleton}
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.block.{Block, BlockEntityProvider, BlockState, Material}
import net.minecraft.state.StateFactory
import net.minecraft.world.BlockView

import scala.reflect.{ClassTag, classTag}

object BlockPipe extends BaseBlock("pipe",
  FabricBlockSettings.of(Material.METAL).breakByHand(true).build()) with BlockEntityProvider
  with MachineGUIBlockActivationSkeleton[PipeEntity] with HasBE[PipeEntity] {

  val KIND_PROPERTY: EnumerationProperty[PipeEntity.PipeType.type, PipeEntity.PipeType.Value] = new EnumerationProperty("kind", PipeEntity.PipeType, classOf[PipeEntity.PipeType.Value])

  override def appendProperties(builder: StateFactory.Builder[Block, BlockState]): Unit = {
    builder.add(KIND_PROPERTY)
    super.appendProperties(builder)
  }

  override protected def beCTag: ClassTag[PipeEntity] = classTag

  override protected def beType: BlockEntityType[PipeEntity] = PipeEntity.tpe
}
