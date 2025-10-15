val scala3Version = "3.7.3"
val commonSettings = Seq(
  scalaVersion           := scala3Version,
  semanticdbEnabled      := true,
  semanticdbVersion      := scalafixSemanticdb.revision,
  Compile / doc / target := file("docs"),
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
  .enablePlugins(JavaAppPackaging)
  .settings(
    name    := "Client",
    version := "0.1.0-SNAPSHOT",
    commonSettings,
    libraryDependencies += "org.typelevel"      %% "cats-effect"    % "3.6.3",
    libraryDependencies += "com.lihaoyi"        %% "requests"       % "0.9.0",
    libraryDependencies += "com.typesafe.slick" %% "slick"          % "3.6.1",
    libraryDependencies += "com.typesafe.slick" %% "slick-hikaricp" % "3.6.1",
    libraryDependencies += "org.postgresql"      % "postgresql"     % "42.6.0"
  )
