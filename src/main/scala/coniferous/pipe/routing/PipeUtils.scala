package coniferous.pipe.routing

import net.minecraft.block.entity.HopperBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object PipeUtils {
  // May as well, hopper logic is good enough tbh
  def getInventory(pos: BlockPos, world: World): Option[Inventory] = Option(HopperBlockEntity.getInventoryAt(world, pos))

}
