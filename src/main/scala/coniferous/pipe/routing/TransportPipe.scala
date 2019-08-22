package coniferous.pipe.routing

import java.util

import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import coniferous.pipe.{BlockPipe, Pipe, RoutedItem}
import net.minecraft.block.BlockState
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.World

import scala.collection.JavaConverters._

case class TransportPipe(pos: BlockPos, world: World) extends Pipe {
  override def name: String = "transport"

  override def canConnect(connections: util.EnumSet[Direction], state: BlockState, face: Direction): Boolean =
    connections.contains(face) || state.getBlock.isInstanceOf[BlockPipe] && connections.size() < 2

  override def routeItem(connections: util.EnumSet[Direction], item: RoutedItem, from: Direction): Option[Direction] =
    connections.asScala.find(_ != from)

  override def toTag[T](ops: DynamicOps[T]): datafixers.Dynamic[T] = new datafixers.Dynamic[T](ops, ops.createInt(0))

  override def fromTag(tag: datafixers.Dynamic[_]): Unit = {}
}
