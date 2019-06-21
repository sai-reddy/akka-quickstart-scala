package com.example

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec, WordSpecLike}

import scala.concurrent.duration._

class DeviceSpec(_system: ActorSystem)
  extends TestKit(_system)
  with WordSpecLike
  with BeforeAndAfterAll
{

  def this() = this(ActorSystem("DeviceSpec"))

  override def afterAll: Unit = {
    shutdown(system)
  }


  "Device actor" must {

    //#device-read-test
    "reply with empty reading if no temperature is known" in {
      val probe = TestProbe()
      val deviceActor = system.actorOf(Device.props("group", "device"))

      deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
      val response = probe.expectMsgType[Device.RespondTemperature]
      assert(response.requestId === 42L)
      assert(response.value === None)
    }
    //#device-read-test

    //#device-write-read-test
    "reply with latest temperature reading" in {
      val probe = TestProbe()
      val deviceActor = system.actorOf(Device.props("group", "device"))

      deviceActor.tell(Device.RecordTemperature(requestId = 1, 24.0), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(requestId = 1))

      deviceActor.tell(Device.ReadTemperature(requestId = 2), probe.ref)
      val response1 = probe.expectMsgType[Device.RespondTemperature]
      assert(response1.requestId === 2L)
      assert(response1.value === Some(24.0))

      deviceActor.tell(Device.RecordTemperature(requestId = 3, 55.0), probe.ref)
      probe.expectMsg(Device.TemperatureRecorded(requestId = 3))

      deviceActor.tell(Device.ReadTemperature(requestId = 4), probe.ref)
      val response2 = probe.expectMsgType[Device.RespondTemperature]
      assert(response2.requestId === 4L)
      assert(response2.value === Some(55.0))
    }
    //#device-write-read-test

  }

}