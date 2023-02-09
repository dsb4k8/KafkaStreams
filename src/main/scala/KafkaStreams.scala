package content

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

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
//  [Source, Flow, Sink]

  def main(args: Array[String]): Unit = {

  }

}
