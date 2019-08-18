package coniferous.block

import java.util
import java.util.Optional

import net.minecraft.state.property.AbstractProperty

import scala.collection.JavaConverters._

class EnumerationProperty[A <: Enumeration, V <: A#Value with Ordered[V]](name: String, a: A, cls: Class[V])
  extends AbstractProperty[V](name, cls) {
  override def getValues: util.Collection[V] = a.values.asJavaCollection.asInstanceOf[util.Collection[V]]

  override def getValue(s: String): Optional[V] = a.values.find(_.toString.equals(s)).map(Optional.of)
    .getOrElse(Optional.empty()).asInstanceOf[Optional[V]]

  override def getName(t: V): String = t.toString
}
