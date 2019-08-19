package coniferous.pipe.routing

import java.util

import coniferous.pipe.{BlockPipe, Pipe, RoutedItem}
import net.minecraft.block.BlockState
import net.minecraft.util.math.{BlockPos, Direction}
import net.minecraft.world.World

class TransportPipe(pos: BlockPos, world: World) extends Pipe {
  override def name: String = "transport"

  override def routeItem(connections: util.EnumSet[Direction], item: RoutedItem, from: Direction): Option[Direction] =
    item.routing.nodes match {
      case dir :: xs =>
        if (dir == from) None
        else if (!connections.contains(dir)) None
        else {
          item.routing.nodes = xs
          Some(dir)
        }
      case Nil => None
    }

  override def canConnect(connections: util.EnumSet[Direction], state: BlockState): Boolean =
    state.getBlock == BlockPipe && connections.size() < 2
}
