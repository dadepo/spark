/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.serializer

import java.io._
import java.nio.ByteBuffer

import ibis.io.{BufferedArrayInputStream, BufferedArrayOutputStream, IbisSerializationInputStream, IbisSerializationOutputStream}
import org.apache.spark.SparkConf
import org.apache.spark.util.Utils

import scala.reflect.ClassTag

private[spark] class IbisSerializationStream(out: OutputStream,
                                             counterReset: Int,
                                             extraDebugInfo: Boolean) extends SerializationStream {

  private val objOut = new IbisSerializationOutputStream(
    new BufferedArrayOutputStream(out)
  )

  private var counter = 0


  /** The most general-purpose method to write an object. */
  override def writeObject[T: ClassTag](t: T): SerializationStream = {
    SLogger.log("ibis","write")
    try {
      objOut.writeObject(t)
      flush()
    } catch {
      case e: NotSerializableException if extraDebugInfo =>
        throw SerializationDebugger.improveException(t, e)
    }
    counter += 1
    if (counterReset > 0 && counter >= counterReset) {
      objOut.reset()
      counter = 0
    }
    this
  }

  def flush(): Unit = { objOut.flush() }
  def close(): Unit = {
    objOut.realClose()
  }
}


private[spark] class IbisSerializerInstance(counterReset: Int,
                                            extraDebugInfo: Boolean,
                                            defaultClassLoader: ClassLoader)
  extends SerializerInstance {

  override def serialize[T: ClassTag](t: T): ByteBuffer = {
    val bos = new ByteArrayOutputStream()
    val out = serializeStream(bos)
    out.writeObject(t)
    out.close()
    ByteBuffer.wrap(bos.toByteArray)
  }

  override def deserialize[T: ClassTag](bytes: ByteBuffer): T = {
    val bis = new ByteArrayInputStream(byteBufferToByteArray(bytes))
    val in = deserializeStream(bis)
    in.readObject()
  }

  override def deserialize[T: ClassTag](bytes: ByteBuffer, loader: ClassLoader): T = {
    val bis = new ByteArrayInputStream(byteBufferToByteArray(bytes))
    val in = deserializeStream(bis, loader)
    in.readObject()
  }

  override def serializeStream(s: OutputStream): SerializationStream = {
    new IbisSerializationStream(s, counterReset, extraDebugInfo)
  }

  override def deserializeStream(s: InputStream): DeserializationStream = {
    new IbisDeserializationStream(s, defaultClassLoader)
  }

  def deserializeStream(s: InputStream, loader: ClassLoader): DeserializationStream = {
    new IbisDeserializationStream(s, loader)
  }

  private[this] def byteBufferToByteArray(bytes: ByteBuffer):Array[Byte] = {
    if (bytes.hasArray) {
      bytes.array()
    } else {
      val bytesArray= Array.fill[Byte](bytes.remaining())(0)
      bytes.get(bytesArray, 0, bytesArray.length)
      bytesArray
    }
  }
}


private[spark] class IbisDeserializationStream(in: InputStream, loader: ClassLoader)
  extends DeserializationStream {

//  private val objIn = new ObjectInputStream(in)
  private val objIn = new IbisSerializationInputStream(new BufferedArrayInputStream(in)) {
      def resolveClass(desc: ObjectStreamClass): Class[_] =
      try {
        // scalastyle:off classforname
        Class.forName(desc.getName, false, loader)
        // scalastyle:on classforname
      } catch {
        case e: ClassNotFoundException =>
          JavaDeserializationStream.primitiveMappings.getOrElse(desc.getName, throw e)
      }
    }


  /** The most general-purpose method to read an object. */
  override def readObject[T: ClassTag](): T = {
    SLogger.log("ibis","read")
    objIn.readObject().asInstanceOf[T]
  }

  override def close(): Unit = {
    objIn.close()
  }
}


class IbisSerializer(conf: SparkConf) extends Serializer with Externalizable {

  private var counterReset = conf.getInt("spark.serializer.objectStreamReset", 100)
  private var extraDebugInfo = conf.getBoolean("spark.serializer.extraDebugInfo", true)

  protected def this() = this(new SparkConf())  // For deserialization only

  override def newInstance(): SerializerInstance = {
    val classLoader = defaultClassLoader.getOrElse(Thread.currentThread.getContextClassLoader)
    new IbisSerializerInstance(counterReset, extraDebugInfo, classLoader)
  }

  override def writeExternal(out: ObjectOutput): Unit = Utils.tryOrIOException {
    out.writeInt(counterReset)
    out.writeBoolean(extraDebugInfo)
  }

  override def readExternal(in: ObjectInput): Unit = Utils.tryOrIOException {
    counterReset = in.readInt()
    extraDebugInfo = in.readBoolean()
  }
}
