package coniferous.pipe.routing

import java.util

import com.google.common.collect.ImmutableMap
import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import coniferous.pipe.{BlockPipe, SidedPipe}
import net.minecraft.block.BlockState
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.World

case class ProviderPipe(pos: BlockPos, world: World) extends SidedPipe {
  private var mSide: Direction = Direction.DOWN

  override def mainSide: Direction = mSide

  override def name: String = "provider"

  override def canConnect(connections: util.EnumSet[Direction], state: BlockState, face: Direction): Boolean = state.getBlock.isInstanceOf[BlockPipe]

  override def toTag[T](ops: DynamicOps[T]): datafixers.Dynamic[T] = new datafixers.Dynamic[T](ops, ops.createMap(
    ImmutableMap.of(ops.createString("MainSide"), ops.createInt(mSide.getId))))

  override def fromTag(tag: datafixers.Dynamic[_]): Unit = {
    mSide = Direction.byId(tag.get("MainSide").asInt(0))
  }

  override def canMainSide(dir: Direction): Boolean = PipeUtils.getInventory(pos.offset(dir), world).nonEmpty
}
