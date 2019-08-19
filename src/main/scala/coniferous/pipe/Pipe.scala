package coniferous.pipe

import java.util

import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

trait Pipe {
  def name: String

  def routeItem(connections: util.EnumSet[Direction], item: RoutedItem, from: Direction): Option[Direction]

  def canConnect(connections: util.EnumSet[Direction], state: BlockState): Boolean
}

trait SidedPipe extends Pipe {
  def mainSide: Direction
}
