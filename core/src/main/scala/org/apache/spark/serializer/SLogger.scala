package org.apache.spark.serializer

private[spark] object SLogger {

  def log(serializer: String, serializationType: String): Unit = {
    if ("true".equalsIgnoreCase(System.getenv("traceOn"))) {
      val stacktrace = Thread.currentThread.getStackTrace
      println(s"start: $serializer $serializationType serializer")
      println(stacktrace.mkString("\n"))
      println(s"end: $serializer $serializationType serializer")
    }
  }
}