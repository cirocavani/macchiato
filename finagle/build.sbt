name := "finagle"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"com.twitter" %% "finagle-http" % "6.12.1",
	"com.twitter" %% "finagle-redis" % "6.12.1"
)

EclipseKeys.withSource := true