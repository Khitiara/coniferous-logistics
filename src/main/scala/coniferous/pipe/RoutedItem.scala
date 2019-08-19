package coniferous.pipe

import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

case class RoutedItem(stack: ItemStack, from: Direction, routing: RoutingInformation)

case class RoutingInformation(var nodes: List[Direction])