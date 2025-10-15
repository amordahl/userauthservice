val scala3Version = "3.7.3"
val commonSettings = Seq(
  scalaVersion      := scala3Version,
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  scalacOptions ++= Seq(
    "-Wunused:all",
    "-Wnonunit-statement",
    "-Wvalue-discard",
    "-deprecation",
    "-feature",
    "-source:future"
  ),

  // Test Dependencies
  libraryDependencies += "org.scalameta" %% "munit"            % "1.2.1" % Test,
  libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "1.2.0" % Test,
  libraryDependencies += "org.scalamock" %% "scalamock"        % "7.5.0" % Test,
  libraryDependencies += "com.lihaoyi"   %% "utest"            % "0.9.1" % Test,
  testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val root = project
  .in(file("."))
  .aggregate(emailAuth, client)
  .settings(
    name                := "todo-app-demo",
    publish / skip      := true,
    publishLocal / skip := true,
    // Disable all tasks that would build the root project
    Compile / packageBin / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false
  )

lazy val client = project
  .in(file("Client"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name                   := "Client",
    version                := "0.1.0-SNAPSHOT",
    Compile / doc / target := file("Client/docs"),
    commonSettings,
    libraryDependencies += "org.typelevel"      %% "cats-effect"    % "3.6.3",
    libraryDependencies += "com.lihaoyi"        %% "requests"       % "0.9.0",
    libraryDependencies += "com.typesafe.slick" %% "slick"          % "3.6.1",
    libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.6.1",
    libraryDependencies += "com.lihaoyi"        %% "ujson"          % "4.3.2",
    libraryDependencies += "org.postgresql"      % "postgresql"     % "42.6.0"
  )

lazy val emailAuth = project
  .in(file("EmailAuthenticationService"))
  .enablePlugins(JavaServerAppPackaging)
  .settings(
    name                   := "EmailAuthenticationService",
    version                := "0.1.0-SNAPSHOT",
    Compile / doc / target := file("EmailAuthenticationService/docs"),
    commonSettings,
    libraryDependencies += "com.lihaoyi" %% "cask" % "0.9.7"
  )
