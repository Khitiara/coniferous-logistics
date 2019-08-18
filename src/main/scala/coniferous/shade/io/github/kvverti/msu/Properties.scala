package coniferous.shade.io.github.kvverti.msu

import com.google.common.collect.ImmutableMap
import net.minecraft.state.PropertyContainer
import net.minecraft.state.property.Property

import JavaStreams._

object Properties {

  implicit class IntPropertyWrapper(val self: Property[Integer]) extends AnyVal {
    def getIntValue(name: String): Option[Int] = self.getValue(name).toOption.map(Int.unbox)
  }

  implicit class BooleanPropertyWrapper(val self: Property[java.lang.Boolean]) extends AnyVal {
    def getBoolValue(name: String): Option[Boolean] = self.getValue(name).toOption.map(Boolean.unbox)
  }

  implicit class PropertyContainerFixer[C <: PropertyContainer[C]](val self: C) extends AnyVal {
    // this isn't actually the real type of the map either, but it's close enough
    // to be useful

    import scala.language.existentials

    type Entries = ImmutableMap[Property[A], A] forSome {type A <: Comparable[A]}

    def entries: Entries = self.getEntries.asInstanceOf[Entries]

    def getInt(prop: Property[Integer]): Int = Int.unbox(self.get(prop))

    def getBool(prop: Property[java.lang.Boolean]): Boolean = Boolean.unbox(self.get(prop))

    def withVal[A <: Comparable[A]](prop: Property[A], value: A): C = self.`with`(prop, value)

    def withVal(prop: Property[Integer], value: Int): C = self.`with`(prop, Int.box(value))

    def withVal(prop: Property[java.lang.Boolean], value: Boolean): C = self.`with`(prop, Boolean.box(value))
  }

}
