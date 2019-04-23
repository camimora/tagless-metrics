package org.novelfs.taglessmetrics.kamon

import cats.implicits._
import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}
import org.novelfs.taglessmetrics.IncrementMetric
import org.novelfs.taglessmetrics.kamon.instances.all._
import _root_.kamon.Kamon
import _root_.kamon.testkit.MetricInspection
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class CounterSpec extends FlatSpec with Matchers with MetricInspection with GeneratorDrivenPropertyChecks {
  val positiveInt = Gen.posNum[Int]

  "increment" should "increment the metric the number of times it is called" in {
    forAll(positiveInt) { expectedCounterValue: Int =>
      val testCounter = Counter("test-counter")
      val incrMetric = IncrementMetric[IO, Counter].increment(testCounter)
      List.fill(expectedCounterValue)(incrMetric).sequence_.unsafeRunSync()
      val actualCounterValue = Kamon.counter("test-counter").value()
      actualCounterValue shouldBe expectedCounterValue
    }
  }

  "incrementTimes" should "increment the metric by n" in {
    forAll(positiveInt) { expectedCounterValue: Int =>
      val testCounter = Counter("test-counter-2")
      IncrementMetric[IO, Counter].incrementTimes(expectedCounterValue.toLong)(testCounter).unsafeRunSync()
      val actualCounterValue = Kamon.counter("test-counter-2").value()
      actualCounterValue shouldBe expectedCounterValue
    }
  }
}
