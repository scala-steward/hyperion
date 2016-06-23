package hyperion

import akka.actor.ActorDSL.actor
import akka.testkit.TestProbe
import scala.concurrent.duration.DurationInt

class LauncherActorSpec extends BaseAkkaSpec {
  "Starting the Launcher Actor" should {
    "start the necessary child actors" in {
      // Arrange

      // Act
      val launcher = actor("create-children")(new LauncherActor() {
        override protected def http() = TestProbe().ref
      })

      // Assert
      val createdActors = TestProbe().expectActor("/user/*", 4 seconds).filterNot(_ == launcher)
      createdActors should have size 5
      val createdPaths = createdActors.map(_.path.toString).map(_.replace("akka://default/user/", ""))
      createdPaths should contain allOf ("receiver", "incoming-http-actor", "recent-history", "collecting-actor", "meter-agent")
    }
  }
}