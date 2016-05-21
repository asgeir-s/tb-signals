organization := "com.cluda"

name := "signals"

version := "0.2.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

test in assembly := {}

assemblyJarName in assembly := "signals.jar"

assemblyOutputPath in assembly := file("docker/signals.jar")

mainClass in assembly := Some("com.cluda.tradersbit.signals.Boot")

resolvers += "sonatype-oss-snapshot" at "https://oss.sonatype.org/content/repositories/snapshots" // for xchange snapshots

libraryDependencies ++= {
  val akkaV       = "2.4.6"
  val xchangeV = "4.0.1-SNAPSHOT"
  Seq(
    "com.typesafe.akka"     %%    "akka-actor"                              %     akkaV,
    "com.typesafe.akka"     %%    "akka-slf4j"                              %     akkaV,
    "com.typesafe.akka"     %%    "akka-stream"                             %     akkaV,
    "com.typesafe.akka"     %%    "akka-http-core"                          %     akkaV,
    "com.typesafe.akka"     %%    "akka-http-experimental"                  %     akkaV,
    "com.typesafe.akka"     %%    "akka-http-spray-json-experimental"       %     akkaV,
    "com.typesafe.akka"     %%    "akka-http-testkit"                       %     akkaV,
    "org.scalatest"         %%    "scalatest"                               %     "2.2.6"      %     "test",
    ("org.knowm.xchange"     %    "xchange-core"                            %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    ("org.knowm.xchange"     %    "xchange-bitfinex"                        %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    ("org.knowm.xchange"     %    "xchange-bitstamp"                        %     xchangeV).exclude("com.pusher", "pusher-java-client"),
    "com.typesafe.slick"    %%    "slick"                                   %     "3.1.1",
    "org.postgresql"         %    "postgresql"                              %     "9.4.1208",
    "com.amazonaws"          %    "aws-java-sdk-sns"                        %     "1.11.2",
    "commons-codec"          %    "commons-codec"                           %     "1.10",
    "org.bitbucket.b_c"      %    "jose4j"                                  %     "0.5.1",
    "ch.qos.logback"         %    "logback-classic"                         %     "1.1.7",
    "com.amazonaws"          %    "aws-java-sdk-dynamodb"                   %     "1.11.3"

  )
}