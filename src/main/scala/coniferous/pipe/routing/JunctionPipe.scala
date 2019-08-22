package coniferous.pipe.routing

import java.util

import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import coniferous.pipe.{BlockPipe, Pipe}
import net.minecraft.block.BlockState
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.World

case class JunctionPipe(pos: BlockPos, world: World) extends Pipe {
  override def name: String = "junction"

  override def canConnect(connections: util.EnumSet[Direction], state: BlockState, face: Direction): Boolean = state.getBlock.isInstanceOf[BlockPipe]

  override def toTag[T](ops: DynamicOps[T]): datafixers.Dynamic[T] = new datafixers.Dynamic[T](ops, ops.createInt(0))

  override def fromTag(tag: datafixers.Dynamic[_]): Unit = {}
}