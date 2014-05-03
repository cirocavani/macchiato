name := "finagle"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"com.twitter" %% "finagle-http" % "6.15.0",
	"com.twitter" %% "finagle-redis" % "6.15.0"
)

EclipseKeys.withSource := true