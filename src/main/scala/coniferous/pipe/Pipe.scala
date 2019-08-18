package coniferous.pipe

import net.minecraft.block.BlockState
import net.minecraft.util.math.Direction

trait Pipe {
  def name: String

  def routeItem(item: RoutedItem, from: Direction): Option[Direction]

  def canConnect(state: BlockState): Boolean
}

trait SidedPipe extends Pipe {
  def mainSide: Direction
}
