organization := "com.cluda"

name := "signals"

version := "0.1.0"

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

test in assembly := {}

assemblyJarName in assembly := "signals.jar"

assemblyOutputPath in assembly := file("docker/signals.jar")

mainClass in assembly := Some("com.cluda.tradersbit.signals.Boot")

resolvers += "sonatype-oss-snapshot" at "https://oss.sonatype.org/content/repositories/snapshots" // for xchange snapshots

libraryDependencies ++= {
  val akkaV       = "2.3.14"
  val akkaStreamV = "1.0"
  val scalaTestV  = "2.2.4"
  val xchangeV = "3.0.1-SNAPSHOT"
  Seq(
    "com.typesafe.akka"     %%    "akka-actor"                              %     akkaV,
    "com.typesafe.akka"     %%    "akka-slf4j"                              %     akkaV,
    "com.typesafe.akka"     %%    "akka-stream-experimental"                %     akkaStreamV,
    "com.typesafe.akka"     %%    "akka-http-core-experimental"             %     akkaStreamV,
    "com.typesafe.akka"     %%    "akka-http-experimental"                  %     akkaStreamV,
    "com.typesafe.akka"     %%    "akka-http-spray-json-experimental"       %     akkaStreamV,
    "com.typesafe.akka"     %%    "akka-http-testkit-experimental"          %     akkaStreamV,
    "org.scalatest"         %%    "scalatest"                               %     scalaTestV      %     "test",
    ("com.xeiam.xchange"     %    "xchange-core"                            %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    ("com.xeiam.xchange"     %    "xchange-bitfinex"                        %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    ("com.xeiam.xchange"     %    "xchange-bitstamp"                        %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    "com.typesafe.slick"    %%    "slick"                                   %     "3.0.0",
    "org.postgresql"         %    "postgresql"                              %     "9.4-1201-jdbc41",
    "com.amazonaws"          %    "aws-java-sdk-sns"                        %     "1.10.+",
    "commons-codec"          %    "commons-codec"                           %     "1.10",
    "org.bitbucket.b_c"      %    "jose4j"                                  %     "0.4.3",
    "ch.qos.logback"         %    "logback-classic"                         %     "1.1.3"

  )
}