name := "rules-service"

scalaVersion in ThisBuild := "2.11.8"

version in ThisBuild := "1.0-SNAPSHOT"

organization in ThisBuild := "com.foo"

scalacOptions in ThisBuild   := List(
  "-encoding",
  "UTF8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xlint",
  "-language:postfixOps",
  "-language:higherKinds")

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging"   %% "scala-logging"      % Versions.ScalaLogging,
  "ch.qos.logback"                % "logback-classic"    % Versions.Logback,
  "com.typesafe.akka"    %% "akka-actor"                 % Versions.Akka,
  "com.pathfinder"        % "wire-read_2.11"          % "SNAPSHOT",
  "com.pathfinder"        % "wire-common_2.11"        % "SNAPSHOT",
  "com.foo"               % "rules-spi_2.11"          % "1.0-SNAPSHOT"
)

fork in run := true

assemblyMergeStrategy in assembly := {
  case "application.conf" => MergeStrategy.concat
  case x => MergeStrategy.defaultMergeStrategy(x)
}

