package coniferous.block

import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.BlockView

trait HasBE[A <: BlockEntity] extends BlockEntityProvider {
  override def createBlockEntity(var1: BlockView): BlockEntity = beType.instantiate()

  protected def beType: BlockEntityType[A]
}
