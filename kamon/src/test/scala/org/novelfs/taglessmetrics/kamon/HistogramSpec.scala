package org.novelfs.taglessmetrics.kamon

import cats.implicits._
import kamon.testkit.MetricInspection
import org.scalatest.{FlatSpec, Matchers}
import org.novelfs.taglessmetrics.kamon.instances.all._
import _root_.kamon.Kamon
import cats.effect.IO
import org.novelfs.taglessmetrics.HistogramMetric

class HistogramSpec extends FlatSpec with Matchers with MetricInspection {
  "histogram" should "record values" in {
    val hist = Histogram("test-histogram")
    val test =
      for {
        _ <- HistogramMetric[IO, Histogram].record(100)(hist)
        _ <- HistogramMetric[IO, Histogram].recordTimes(998)(150)(hist)
        _ <- HistogramMetric[IO, Histogram].record(200)(hist)
      } yield ()

    test.unsafeRunSync()

    val kamonHist = Kamon.histogram("test-histogram")

    val distribution = kamonHist.distribution()
    distribution.min shouldBe 100
    distribution.max shouldBe 200
    distribution.count shouldBe 1000
    distribution.buckets.length shouldBe 3
    distribution.buckets.map(b => (b.value, b.frequency)) should contain.allOf(
      100 -> 1,
      150 -> 998,
      200 -> 1
    )
  }
}
