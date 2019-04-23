import ReleaseTransformations._

name := "tagless-metrics"

version := "0.1"

val ScalaVersion = "2.12.8"

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val commonSettings = Seq(
  scalaVersion := ScalaVersion,
  organization := "org.novelfs",
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.0"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pgpSecretRing := file("local.privkey.asc"),
  pgpPublicRing := file("local.pubkey.asc"),
  pgpPassphrase := sys.env.get("PGP_PASS").map(_.toArray),
  pomIncludeRepository := { _ => false },
  publishMavenStyle := true,
  scalacOptions ++= Seq(
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
    "-language:higherKinds",             // Allow higher-kinded types
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
    "-Xfuture",                          // Turn on future language features.
    "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
    "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
    "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
    "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",            // Option.apply used implicit view.
    "-Xlint:package-object-classes",     // Class or object defined in package object.
    "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
    "-Xlint:unsound-match",              // Pattern match may not be typesafe.
    "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification",             // Enable partial unification in type constructor inference
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
  )
)

lazy val core =
  (project in file("core"))
    .settings(commonSettings: _*)
    .settings(
      name := "tagless-metrics-core",
    )

lazy val kamon =
  (project in file("kamon"))
    .dependsOn(core)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "io.kamon"       %% "kamon-core"        % "1.1.6",
        "org.typelevel"  %% "cats-effect"       % "1.2.0",
        "io.kamon"       %% "kamon-testkit"     % "1.1.1"  % Test,
        "org.scalacheck" %% "scalacheck"        % "1.14.0" % Test,
        "org.scalactic"  %% "scalactic"         % "3.0.5"  % Test,
        "org.scalatest"  %% "scalatest"         % "3.0.5"  % Test
      ),
      name := "tagless-metrics-kamon",
    )

lazy val combined =
  (project in file("."))
    .settings(commonSettings: _*)
    .settings(noPublishSettings)
    .aggregate(
      core, 
      kamon
    )

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

licenses := Seq("Apache-2.0" -> url("https://opensource.org/licenses/Apache-2.0"))

homepage := Some(url("https://github.com/TheInnerLight/tagless-metrics"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/TheInnerLight/tagless-metrics"),
    "scm:git@github.com:TheInnerLight/tagless-metrics.git"
  )
)

releaseCrossBuild := true
crossScalaVersions := Seq("2.11.12", "2.12.6")

useGpg := false

developers := List(
  Developer(
    id    = "TheInnerLight",
    name  = "Phil Curzon",
    email = "phil@novelfs.org",
    url   = url("https://github.com/TheInnerLight")
  )
)



parallelExecution in Test := false

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeRelease")
)


