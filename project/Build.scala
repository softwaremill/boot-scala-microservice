import sbt._
import sbt.Keys._
import net.virtualvoid.sbt.graph.Plugin._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import sbt.ScalaVersion
import sbtassembly.Plugin._
import AssemblyKeys._

object BuildSettings {

  val buildSettings = Defaults.defaultSettings ++ Seq(

    organization := "com.softwaremill",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.10.3",

    scalacOptions += "-unchecked",
    classpathTypes ~= (_ + "orbit"),
    libraryDependencies ++= Dependencies.testingDependencies,
    libraryDependencies ++= Dependencies.logging,
    libraryDependencies ++= Seq(Dependencies.guava, Dependencies.googleJsr305),

    parallelExecution := false,

    resolvers += "JCenter" at "http://jcenter.bintray.com"
  )
}

object Dependencies {

  val slf4jVersion = "1.7.6"
  val logBackVersion = "1.1.1"
  val smlCommonVersion = "75"
  val scalatraVersion = "2.2.2"
  val rogueVersion = "2.2.0"
  val scalaLoggingVersion = "1.1.0"
  val jettyVersion = "9.2.0.v20140526"

  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion
  val logBackClassic = "ch.qos.logback" % "logback-classic" % logBackVersion
//  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % slf4jVersion
  val scalaLogging = "com.typesafe" %% "scalalogging-slf4j" % scalaLoggingVersion

  val logging = Seq(slf4jApi, logBackClassic, scalaLogging)

  val typesafeConfig = "com.typesafe" % "config" % "1.2.0"

  val guava = "com.google.guava" % "guava" % "16.0.1"
  val googleJsr305 = "com.google.code.findbugs" % "jsr305" % "2.0.3"

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalatraScalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  val scalatraJson = "org.scalatra" %% "scalatra-json" % scalatraVersion
  val json4s = "org.json4s" %% "json4s-jackson" % "3.2.7"
  val scalatraAuth = "org.scalatra" %% "scalatra-auth" % scalatraVersion  exclude("commons-logging", "commons-logging")

  val jodaTime = "joda-time" % "joda-time" % "2.3"
  val jodaConvert = "org.joda" % "joda-convert" % "1.6"

  val commonsValidator = "commons-validator" % "commons-validator" % "1.4.0" exclude("commons-logging", "commons-logging")
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.2.1"

  val jetty = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
  val jettyTest = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "test"

  val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"

  val jodaDependencies = Seq(jodaTime, jodaConvert)
  val scalatraStack = Seq(scalatra, scalatraScalatest, scalatraJson, json4s, scalatraAuth, commonsLang)

  val testingDependencies = Seq(mockito, scalatest)

  val javaxMail = "javax.mail" % "mail" % "1.4.7"

  val seleniumVer = "2.42.2"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVer % "test"
  val seleniumFirefox = "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVer % "test"
  val fest = "org.easytesting" % "fest-assert" % "1.4" % "test"
  val awaitility = "com.jayway.awaitility" % "awaitility-scala" % "1.3.5" % "test"
  val fakeMongo = "com.github.fakemongo" % "fongo" % "1.5.1" % "test"
  //lift-mongodb-record depends on older mongo-java-driver, but for fakeMongo we need newer
  val mongoJava = "org.mongodb" % "mongo-java-driver" % "2.12.2" % "test"

  val microDeps = "com.ofg" % "micro-deps" % "0.5.5" exclude("org.jboss.spec.javax.annotation", "jboss-annotations-api_1.2_spec") exclude("org.jboss.spec.javax.servlet", "jboss-servlet-api_3.1_spec") exclude("commons-collections", "commons-collections")

  // If the scope is provided;test, as in scalatra examples then gen-idea generates the incorrect scope (test).
  // As provided implies test, so is enough here.
  val servletApiProvided = "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "provided" artifacts (Artifact("javax.servlet", "jar", "jar"))

  val bson = "org.mongodb" % "bson" % "2.7.1" % "provided"

  val rogueField = "com.foursquare" %% "rogue-field" % rogueVersion intransitive()
  val rogueCore = "com.foursquare" %% "rogue-core" % rogueVersion intransitive()
  val rogueLift = "com.foursquare" %% "rogue-lift" % rogueVersion intransitive()
  val rogueIndex = "com.foursquare" %% "rogue-index" % rogueVersion intransitive()
  val liftMongoRecord = "net.liftweb" %% "lift-mongodb-record" % "2.5.1"

  val rogue = Seq(rogueCore, rogueField, rogueLift, rogueIndex, liftMongoRecord)
}

object BootzookaBuild extends Build {

  import Dependencies._
  import BuildSettings._
  import com.earldouglas.xsbtwebplugin.WebPlugin.webSettings

  private def haltOnCmdResultError(result: Int) {
    if(result != 0) {
      throw new Exception("Build failed.")
    }
  }

  lazy val parent: Project = Project(
    "bootzooka-root",
    file("."),
    settings = buildSettings
  ) aggregate(common, domain, dao, service, rest, dist)

  lazy val common: Project = Project(
    "bootzooka-common",
    file("bootzooka-common"),
    settings = buildSettings ++ Seq(libraryDependencies ++= jodaDependencies)
  )

  lazy val domain: Project = Project(
    "bootzooka-domain",
    file("bootzooka-domain"),
    settings = buildSettings ++ Seq(libraryDependencies += bson)
  ) dependsOn (common)

  lazy val dao: Project = Project(
    "bootzooka-dao",
    file("bootzooka-dao"),
    settings = buildSettings ++ Seq(libraryDependencies ++= (rogue ++ Seq(mongoJava, fakeMongo)))
  ) dependsOn(domain, common)

  lazy val service: Project = Project(
    "bootzooka-service",
    file("bootzooka-service"),
    settings = buildSettings ++ Seq(libraryDependencies ++= Seq(commonsValidator, javaxMail, typesafeConfig))
  ) dependsOn(domain, dao, common)

  lazy val rest: Project = Project(
    "bootzooka-rest",
    file("bootzooka-rest"),
    settings = buildSettings ++ graphSettings ++ webSettings ++ Seq(
      libraryDependencies ++= scalatraStack ++ jodaDependencies ++ Seq(servletApiProvided),
      artifactName := { (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        "bootzooka." + artifact.extension // produces nice war name -> http://stackoverflow.com/questions/8288859/how-do-you-remove-the-scala-version-postfix-from-artifacts-builtpublished-wi
      },
      // We need to include the whole webapp, hence replacing the resource directory
      webappResources in Compile <<= baseDirectory { bd =>
        val restResources = bd.getParentFile / rest.base.getName / "src" / "main" / "webapp"
        List(restResources)
      },
      packageWar in DefaultConf <<= (packageWar in DefaultConf),
      libraryDependencies ++= Seq(jettyContainer, servletApiProvided, microDeps)
    )
  ) dependsOn(service, domain, common)

  lazy val dist = Project(
    "bootzooka-dist",
    file("bootzooka-dist"),
    settings = buildSettings ++ assemblySettings ++ Seq(
      libraryDependencies ++= Seq(jetty),
      mainClass in assembly := Some("com.softwaremill.bootzooka.Bootzooka"),
      // We need to include the whole webapp, hence replacing the resource directory
      unmanagedResourceDirectories in Compile <<= baseDirectory { bd => {
        List(bd.getParentFile / rest.base.getName / "src" / "main", bd.getParentFile / "dist")
      } }
    )
  ) dependsOn (rest)
}
