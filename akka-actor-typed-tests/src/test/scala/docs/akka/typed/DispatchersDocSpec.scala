/*
 * Copyright (C) 2018 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.akka.typed

import akka.actor.testkit.typed.scaladsl.{ ActorTestKit, TestProbe }
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.SpawnProtocol.Spawn
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, SpawnProtocol, TypedAkkaSpecWithShutdown, Props, DispatcherSelector }
import akka.dispatch.Dispatcher
import org.scalatest.concurrent.ScalaFutures
import DispatchersDocSpec._
import com.typesafe.config.{ Config, ConfigFactory }

object DispatchersDocSpec {

  val config = ConfigFactory.parseString(
    """
       //#config
      your-dispatcher {
        type = Dispatcher
        executor = "thread-pool-executor"
        thread-pool-executor {
          fixed-pool-size = 32
        }
        throughput = 1
      }
       //#config
    """.stripMargin)

  case class WhichDispatcher(replyTo: ActorRef[Dispatcher])

  val giveMeYourDispatcher = Behaviors.receive[WhichDispatcher] { (ctx, msg) ⇒
    msg.replyTo ! ctx.executionContext.asInstanceOf[Dispatcher]
    Behaviors.same
  }

  val yourBehavior: Behavior[String] = Behaviors.same

  val example = Behaviors.receive[Any] { (ctx, msg) ⇒

    //#spawn-dispatcher
    import akka.actor.typed.DispatcherSelector

    ctx.spawn(yourBehavior, "DefaultDispatcher")
    ctx.spawn(yourBehavior, "ExplicitDefaultDispatcher", DispatcherSelector.default())
    ctx.spawn(yourBehavior, "BlockingDispatcher", DispatcherSelector.blocking())
    ctx.spawn(yourBehavior, "DispatcherFromConfig", DispatcherSelector.fromConfig("your-dispatcher"))
    //#spawn-dispatcher

    Behaviors.same
  }

}

class DispatchersDocSpec extends ActorTestKit with TypedAkkaSpecWithShutdown with ScalaFutures {

  override def config: Config = DispatchersDocSpec.config

  "Actor Dispatchers" should {
    "support default and blocking dispatcher" in {
      val probe = TestProbe[Dispatcher]()
      val actor: ActorRef[SpawnProtocol] = spawn(SpawnProtocol.behavior)

      val withDefault = (actor ? Spawn(giveMeYourDispatcher, "default", Props.empty)).futureValue
      withDefault ! WhichDispatcher(probe.ref)
      probe.expectMessageType[Dispatcher].id shouldEqual "akka.actor.default-dispatcher"

      val withBlocking = (actor ? Spawn(giveMeYourDispatcher, "default", DispatcherSelector.blocking())).futureValue
      withBlocking ! WhichDispatcher(probe.ref)
      probe.expectMessageType[Dispatcher].id shouldEqual "akka.actor.default-blocking-io-dispatcher"

      val withCustom = (actor ? Spawn(giveMeYourDispatcher, "default", DispatcherSelector.fromConfig("your-dispatcher"))).futureValue
      withCustom ! WhichDispatcher(probe.ref)
      probe.expectMessageType[Dispatcher].id shouldEqual "your-dispatcher"
    }
  }
}
