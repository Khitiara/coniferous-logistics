package coniferous.pipe

import java.util

import com.mojang.datafixers.Dynamic
import com.mojang.datafixers.types.DynamicOps
import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

trait Pipe {
  def name: String

  def routeItem(connections: util.EnumSet[Direction], item: RoutedItem, from: Direction): Option[Direction] =
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

  def canConnect(connections: util.EnumSet[Direction], state: BlockState, face: Direction): Boolean

  def toTag[T](ops: DynamicOps[T]): Dynamic[T]

  def fromTag(tag: Dynamic[_]): Unit
}

trait SidedPipe extends Pipe {
  def mainSide: Direction

  def canMainSide(dir: Direction): Boolean
}
