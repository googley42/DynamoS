package com.coding42

import java.util

import com.amazonaws.services.dynamodbv2.model.{AttributeValue, GetItemResult}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

package object dynamos {

  implicit class ToDynamoKey[A : DynamosWriter](map: Map[String, A]) {
    def toDynamoKey: util.Map[String, AttributeValue] = map.mapValues(_.toDynamoDb).asJava
  }

  implicit class ToDynamoIdOps[A](a: A)(implicit writer: DynamosWriter[A]) {
    def toDynamoDb: AttributeValue = writer.write(a)
  }

  object Dynamos {
    def fromDynamo[A](i: GetItemResult)(implicit reader: DynamosReader[A]): Option[A] = fromDynamo(i.getItem)

    def fromDynamo[A](i: java.util.Map[String, AttributeValue])(implicit reader: DynamosReader[A]): Option[A] = {
      Option(i).map { map =>
        reader.read(new AttributeValue().withM(map))
      }
    }

    def fromDynamo[A : DynamosReader](i: java.util.Collection[java.util.Map[String, AttributeValue]]): Iterable[A] = {
      i.asScala.flatMap(fromDynamo(_): Option[A]) // TODO is flatmap fine here?
    }
  }
}
