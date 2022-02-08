
scalaVersion := "3.1.1"

Compile/scalaSource := baseDirectory.value / "src"

Test/scalaSource := baseDirectory.value / "test"

libraryDependencies ++= Seq(
  "org.apache.pdfbox" % "pdfbox" % "2.0.25"
)

