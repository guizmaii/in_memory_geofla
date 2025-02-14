import sbt.Keys.libraryDependencies

ThisBuild / organization := "com.colisweb"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / scalafmtOnCompile := true
ThisBuild / scalafmtCheck := true
ThisBuild / scalafmtSbtCheck := true
ThisBuild / pushRemoteCacheTo := Some(
  MavenCache("local-cache", baseDirectory.value / sys.env.getOrElse("CACHE_PATH", "sbt-cache"))
)

val testKitLibs = Seq(
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "org.scalactic"  %% "scalactic"  % "3.0.5",
  "org.scalatest"  %% "scalatest"  % "3.0.5"
).map(_ % Test)

lazy val root = Project(id = "geoflram", base = file("."))
  .settings(moduleName := "geoflram")
  .settings(noPublishSettings: _*)
  .aggregate(core, jruby)
  .dependsOn(core, jruby)

lazy val core =
  project
    .settings(moduleName := "geoflram")
    .settings(
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
      libraryDependencies ++= Seq(
        "org.locationtech.jts" % "jts-core" % "1.16.0"
      ) ++ testKitLibs
    )

lazy val jruby =
  project
    .settings(moduleName := "geoflram-jruby")
    .settings(libraryDependencies ++= testKitLibs)
    .dependsOn(core)

lazy val benchmarks =
  project
    .enablePlugins(JmhPlugin)
    .settings(noPublishSettings: _*)
    .dependsOn(core)

/**
  * Copied from Cats
  */
def noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

//// Aliases

/**
  * Copied from kantan.csv
  */
addCommandAlias("runBenchs", "benchmarks/jmh:run -i 10 -wi 10 -f 2 -t 1")

/**
  * Example of JMH tasks that generate flamegraphs.
  *
  * http://malaw.ski/2017/12/10/automatic-flamegraph-generation-from-jmh-benchmarks-using-sbt-jmh-extras-plain-java-too/
  */
addCommandAlias(
  "flame93",
  "benchmarks/jmh:run -f1 -wi 10 -i 20 PackerBenchmarkWithRealData -prof jmh.extras.Async:flameGraphOpts=--minwidth,2;verbose=true")
