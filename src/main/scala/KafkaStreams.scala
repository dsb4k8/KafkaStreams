import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}
object KafkaStreams {

  object Domain {
    type UserID = String
    type Profile = String
    type Product = String
    type OrderId = String
    type Status = String

    case class Order(orderId: OrderId, userID: UserID, product: Product, amount: Double)
    case class Discount(profile: Profile, amount: Double)
    case class Payment(orderId: OrderId, status: Status)
  }

  //Kafka Topics
  object Topics {
    val OrdersByUser = "orders-by-user"
    val DiscountProfilesByUser = "discount-profiles-by-user"
    val Discounts = "discounts"
    val Orders = "orders"
    val Payments = "payments"
    val PaidOrders = "paid-orders"
  }
/* Kafka Components[
    Source: Emitter of Elements
    Flow: transforms elements along the way (e.g. map)
    Sink: Injester of elements]

   we will use io.circe library to turn out Domain Objects into JSON Strings,
   and then turn those Json Strings into bytes/serialized representation
 */

  /* A Serde (Serialize/Deserialize) is a datastructure that takes
  Must be able to serialize. Therefore,  data structure will be employed*/

  import Domain._
  import Topics._
  implicit def serde[A >: Null: Decoder : Encoder]: Serde[A]  = {
    //2 fns => serialize order and deserialize order
    val serializer = (a: A) => a.asJson.noSpaces.getBytes()
    val deserializer = (bytes: Array[Byte]) => {
      val string  = new String(bytes)
      decode[A](string).toOption
    }

    Serdes.fromFn[A](serializer, deserializer) //We now have an order of Serde that kafka can use implicitly convert to and from an array of bytes

  }

  //Topology API
  var builder = new StreamsBuilder()

  //kstream
  val userOrderStream: KStream[UserID, Order] = builder.stream[UserID, Order](OrdersByUser)

  //ktable - Distributed between the nodes in a kafka cluster.
  //broker managed k topic.
  // Can be queried with dsl
  val userProfilesTable: KTable[UserID, Profile] = builder.table[UserID, Profile](DiscountProfilesByUser)

  //GlobalKTable - Copied to all the nodes in a cluster
  //Should store few values ... or else you might crash
  // :) very performant - A join on this GKTable with a normal K table will be very fast because everything is in memory
  //the alternative approach is inefficient -
  //      "Shuffling" is bad:  joining normal KTables together and sending the data back in forth between the nodes

  val discountProfilesGTable: GlobalKTable[Profile, Discount] = builder.globalTable[Profile, Discount](Discounts)


//  filter userOrderStream by Orders over $1000
  //kstream transformation
  val expensiveOrders = userOrderStream.filter((userID, order) => order.amount > 1000)

  val listOfProducts = userOrderStream.mapValues(order =>order.product)

  val productsStream = userOrderStream.flatMapValues(_.product)

  builder.build()

  def main(args: Array[String]): Unit = {

  }

}
