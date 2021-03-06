package es.upm.fi.oeg.morph.stream.evaluate
import java.sql.ResultSet
import java.sql.Date
import java.sql.Timestamp
import java.sql.Time
import java.io.InputStream
import java.io.Reader
import java.sql.SQLWarning
import java.sql.Blob
import java.sql.Ref
import java.sql.Statement
import java.sql.Clob
import java.util.Calendar
import java.net.URL
import java.sql.RowId
import java.sql.NClob
import java.sql.SQLXML
import es.upm.fi.oeg.morph.stream.algebra.xpr.Xpr
import java.sql.ResultSetMetaData
import javax.sql.RowSetMetaData
import javax.sql.rowset.RowSetMetaDataImpl
import java.sql.Types
import org.slf4j.LoggerFactory
import es.upm.fi.oeg.morph.stream.algebra.xpr.ReplaceXpr
import es.upm.fi.oeg.morph.stream.algebra.xpr.VarXpr
import es.upm.fi.oeg.morph.stream.algebra.xpr.NullValueXpr
import es.upm.fi.oeg.morph.stream.algebra.xpr.ConstantXpr

trait StreamResultSet extends ResultSet{
  private val logger = LoggerFactory.getLogger(this.getClass)
  val records:Stream[Array[Object]]=Stream()
  val metadata: Map[String, Xpr]=Map()
  val queryVars:Seq[String]=Seq()
  
  private val it = records.iterator
  protected var current: Seq[Object] = _
  
  override def next:Boolean = {
    if (it.hasNext) {
      current = it.next
      logger.trace(current.mkString("----"))
      true
    } else false
  }

  protected def createMetadata={
    val md: RowSetMetaData = new RowSetMetaDataImpl
    md.setColumnCount(metadata.size)
    var i=1
    metadata.foreach{e=>
      logger.trace("metadata "+e._1+"--"+e._2)
      md.setColumnLabel(i, e._1)
      md.setColumnType(i, Types.VARCHAR)
      i+=1
    }
    md
  }
  protected val metaData: ResultSetMetaData = createMetadata
  
  protected lazy val internalLabels=queryVars.zipWithIndex.toMap
/*
  private val compoundLabels={
    val spk=internalLabels.keys.toArray.filter(k=>k.contains('_')).map{k=>
      val sp=k.split('_')
      (sp(0),sp(1))      
    }
    val grouped=spk.groupBy(_._1).map(v=>(v._1,v._2.map(v2=>v2._2)))
    grouped
  }*/
  
  private val labelPos=
    (1 to metaData.getColumnCount).map(i=>metaData.getColumnLabel(i)->i).toMap
    
  override def findColumn(columnLabel:String):Int = labelPos(columnLabel)
  override def getMetaData:ResultSetMetaData=metaData
  override def getObject(columnIndex:Int):Object=current(columnIndex-1)
  
  override def getObject(columnLabel:String):Object={ 
    logger.debug("labels "+internalLabels.mkString)

    metadata(columnLabel) match{
      case rep:ReplaceXpr=>
        val repl=internalLabels.map(l=>l._1.replace(columnLabel+"_","")->current(l._2))
        rep.evaluate(repl)
      case v:VarXpr=>
        //current(internalLabels(columnLabel))
        current(internalLabels(columnLabel))
      case NullValueXpr=>null
      case const:ConstantXpr=>const.evaluate
      case _=>current(internalLabels(columnLabel))
    }
  }

  override def getString(columnLabel:String):String = 
    getObject(columnLabel:String).toString

  
  
  override def unwrap[T](iface:Class[T]): T = null.asInstanceOf[T]
  override def isWrapperFor(iface:Class[_]): Boolean = false
  
  override def close {}

  override def getHoldability(): Int = 0
  override def isClosed(): Boolean = false
  override def wasNull(): Boolean = false  

  override def getString(columnIndex: Int): String = null  
  override def getBoolean(columnIndex: Int): Boolean = false
  override def getByte(columnIndex: Int): Byte = 0
  override def getShort(columnIndex: Int): Short = 0
  override def getInt(columnIndex: Int): Int = 0
  override def getLong(columnIndex: Int): Long = 0
  override def getFloat(columnIndex: Int): Float = 0
  override def getDouble(columnIndex: Int): Double = 0
  override def getBigDecimal(columnIndex: Int, scale: Int): java.math.BigDecimal = null
  override def getBytes(columnIndex: Int): Array[Byte] = null
  override def getDate(columnIndex: Int): Date = null
  override def getTime(columnIndex: Int): Time = null
  override def getTimestamp(columnIndex: Int): Timestamp = null
  override def getAsciiStream(columnIndex: Int): InputStream = null
  override def getUnicodeStream(columnIndex: Int): InputStream = null
  override def getBinaryStream(columnIndex: Int): InputStream = null

  override def getBoolean(columnLabel: String): Boolean = false
  override def getByte(columnLabel: String): Byte = 0
  override def getShort(columnLabel: String): Short = 0
  override def getInt(columnLabel: String): Int = 0
  override def getLong(columnLabel: String): Long = 0
  override def getFloat(columnLabel: String): Float = 0
  override def getDouble(columnLabel: String): Double = 0
  override def getBigDecimal(columnLabel: String, scale: Int): java.math.BigDecimal = null
  override def getBytes(columnLabel: String): Array[Byte] = null
  override def getDate(columnLabel: String): Date = null
  override def getTime(columnLabel: String): Time = null
  override def getTimestamp(columnLabel: String): Timestamp = null
  override def getAsciiStream(columnLabel: String): InputStream = null
  override def getUnicodeStream(columnLabel: String): InputStream = null
  override def getBinaryStream(columnLabel: String): InputStream = null


  override def getWarnings(): SQLWarning = null
  override def clearWarnings() {}

  override def getCursorName(): String = null

  override def getCharacterStream(columnIndex: Int): Reader = null
  override def getCharacterStream(columnLabel: String): Reader = null
  override def getBigDecimal(columnIndex: Int): java.math.BigDecimal = null
  override def getBigDecimal(columnLabel: String): java.math.BigDecimal = null


  override def isBeforeFirst(): Boolean = false
  override def isAfterLast(): Boolean = false
  override def isFirst(): Boolean = false
  override def isLast(): Boolean = false
  override def beforeFirst() {}
  override def afterLast() {}
  override def first(): Boolean = false
  override def last(): Boolean = false
  override def getRow(): Int = 0
  override def absolute(row: Int): Boolean = false
  override def relative(rows: Int): Boolean = false
  override def previous(): Boolean = false
  override def setFetchDirection(direction: Int) {}
  override def getFetchDirection(): Int = 0
  override def setFetchSize(rows: Int) {}
  override def getFetchSize(): Int = 0
  override def getType(): Int = 0
  override def getConcurrency(): Int = 0
  override def rowUpdated(): Boolean = false
  override def rowInserted(): Boolean = false
  override def rowDeleted(): Boolean = false

  override def updateNull(columnIndex: Int) {}
  override def updateBoolean(columnIndex: Int, x: Boolean) {}
  override def updateByte(columnIndex: Int, x: Byte) {}
  override def updateShort(columnIndex: Int, x: Short) {}
  override def updateInt(columnIndex: Int, x: Int) {}
  override def updateLong(columnIndex: Int, x: Long) {}
  override def updateFloat(columnIndex: Int, x: Float) {}
  override def updateDouble(columnIndex: Int, x: Double) {}
  override def updateBigDecimal(columnIndex: Int, x: java.math.BigDecimal) {}
  override def updateString(columnIndex: Int, x: String) {}
  override def updateBytes(columnIndex: Int, x: Array[Byte]) {}
  override def updateDate(columnIndex: Int, x: Date) {}
  override def updateTime(columnIndex: Int, x: Time) {}
  override def updateTimestamp(columnIndex: Int, x: Timestamp) {}
  override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Int) {}
  override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Int) {}
  override def updateCharacterStream(columnIndex: Int, x: Reader, length: Int) {}
  override def updateObject(columnIndex: Int, x: Object, scaleOrLength: Int) {}
  override def updateObject(columnIndex: Int, x: Object) {}
  
  override def updateNull(columnLabel: String) {}
  override def updateBoolean(columnLabel: String, x: Boolean) {}
  override def updateByte(columnLabel: String, x: Byte) {}
  override def updateShort(columnLabel: String, x: Short) {}
  override def updateInt(columnLabel: String, x: Int) {}
  override def updateLong(columnLabel: String, x: Long) {}
  override def updateFloat(columnLabel: String, x: Float) {}
  override def updateDouble(columnLabel: String, x: Double) {}
  override def updateBigDecimal(columnLabel: String, x: java.math.BigDecimal) {}
  override def updateString(columnLabel: String, x: String) {}
  override def updateBytes(columnLabel: String, x: Array[Byte]) {}
  override def updateDate(columnLabel: String, x: Date) {}
  override def updateTime(columnLabel: String, x: Time) {}
  override def updateTimestamp(columnLabel: String, x: Timestamp) {}
  override def updateAsciiStream(columnLabel: String, x: InputStream, length: Int) {}
  override def updateBinaryStream(columnLabel: String, x: InputStream, length: Int) {}
  override def updateCharacterStream(columnLabel: String, reader: Reader,  length: Int) {}
  override def updateObject(columnLabel: String, x: Object, scaleOrLength: Int) {}
  override def updateObject(columnLabel: String, x: Object) {}

  override def insertRow() {}
  override def updateRow() {}
  override def deleteRow() {}
  override def refreshRow() {}
  override def cancelRowUpdates() {}
  override def moveToInsertRow() {}
  override def moveToCurrentRow() {}
  override def getStatement(): Statement = null

  
  override def getObject(columnIndex: Int, map: java.util.Map[String, java.lang.Class[_]]): Object = null
  override def getObject[T](columnIndex: Int, cl:java.lang.Class[T]): T = null.asInstanceOf[T]
  override def getRef(columnIndex: Int): Ref = null
  override def getBlob(columnIndex: Int): Blob = null
  override def getClob(columnIndex: Int): Clob = null
  override def getArray(columnIndex: Int): java.sql.Array = null
  override def getObject(columnLabel: String, map: java.util.Map[String, java.lang.Class[_]]): Object = null
  override def getObject[T](columnLabel: String, cl: java.lang.Class[T]): T = null.asInstanceOf[T]
  override def getRef(columnLabel: String): Ref = null
  override def getBlob(columnLabel: String): Blob = null
  override def getClob(columnLabel: String): Clob = null

  override def getArray(columnLabel: String): java.sql.Array = null

  override def getDate(columnIndex: Int, cal: Calendar): Date = null
  override def getDate(columnLabel: String, cal: Calendar): Date = null

  override def getTime(columnIndex: Int, cal: Calendar): Time = null
  override def getTime(columnLabel: String, cal: Calendar): Time = null

  override def getTimestamp(columnIndex: Int, cal: Calendar): Timestamp = null
  override def getTimestamp(columnLabel: String, cal: Calendar): Timestamp = null

  override def getURL(columnIndex: Int): URL = null
  override def getURL(columnLabel: String): URL = null

  override def updateRef(columnIndex: Int, x: Ref) {}
  override def updateRef(columnLabel: String, x: Ref) {}

  override def updateBlob(columnIndex: Int, x: Blob) {}
  override def updateBlob(columnLabel: String, x: Blob) {}

  override def updateClob(columnIndex: Int, x: Clob) {}
  override def updateClob(columnLabel: String, x: Clob) {}

  override def updateArray(columnIndex: Int, x: java.sql.Array) {}
  override def updateArray(columnLabel: String, x: java.sql.Array) {}

  override def getRowId(columnIndex: Int): RowId = null
  override def getRowId(columnLabel: String): RowId = null
  override def updateRowId(columnIndex: Int, x: RowId) {}
  override def updateRowId(columnLabel: String, x: RowId) {}

  
  override def updateNString(columnIndex: Int, nString: String) {}
  override def updateNString(columnLabel: String, nString: String) {}
  
  override def updateNClob(columnIndex: Int, nClob: NClob) {}
  override def updateNClob(columnLabel: String, nClob: NClob) {}
  override def getNClob(columnIndex: Int): NClob = null
  override def getNClob(columnLabel: String): NClob = null

  override def getSQLXML(columnIndex: Int): SQLXML = null
  override def getSQLXML(columnLabel: String): SQLXML = null
  override def updateSQLXML(columnIndex: Int, xmlObject: SQLXML) {}
  override def updateSQLXML(columnLabel: String, xmlObject: SQLXML) {}

  override def getNString(columnIndex: Int): String = null
  override def getNString(columnLabel: String): String = null
  override def getNCharacterStream(columnIndex: Int): Reader = null
  override def getNCharacterStream(columnLabel: String): Reader = null
  override def updateNCharacterStream(columnIndex: Int, x: Reader, length: Long) {}
  override def updateNCharacterStream(columnLabel: String, reader: Reader,length: Long) {}
  
  override def updateAsciiStream(columnIndex: Int, x: InputStream, length: Long) {}
  override def updateBinaryStream(columnIndex: Int, x: InputStream, length: Long) {}
  override def updateCharacterStream(columnIndex: Int, x: Reader, length: Long) {}
  override def updateAsciiStream(columnLabel: String, x: InputStream, length: Long) {}
  override def updateBinaryStream(columnLabel: String, x: InputStream,length: Long) {}
  override def updateCharacterStream(columnLabel: String, reader: Reader,length: Long) {}
  override def updateBlob(columnIndex: Int, inputStream: InputStream, length: Long) {}
  override def updateBlob(columnLabel: String, inputStream: InputStream,length: Long) {}
  override def updateClob(columnIndex: Int, reader: Reader, length: Long) {}
  override def updateClob(columnLabel: String, reader: Reader, length: Long) {}
  override def updateNClob(columnIndex: Int, reader: Reader, length: Long) {}
  override def updateNClob(columnLabel: String, reader: Reader, length: Long) {}
  override def updateNCharacterStream(columnIndex: Int, x: Reader) {}
  override def updateNCharacterStream(columnLabel: String, reader: Reader) {}
  override def updateAsciiStream(columnIndex: Int, x: InputStream) {}
  override def updateBinaryStream(columnIndex: Int, x: InputStream) {}
  override def updateCharacterStream(columnIndex: Int, x: Reader) {}
  override def updateAsciiStream(columnLabel: String, x: InputStream) {}
  override def updateBinaryStream(columnLabel: String, x: InputStream) {}
  override def updateCharacterStream(columnLabel: String, reader: Reader) {}
  override def updateBlob(columnIndex: Int, inputStream: InputStream) {}
  override def updateBlob(columnLabel: String, inputStream: InputStream) {}
  override def updateClob(columnIndex: Int, reader: Reader) {}
  override def updateClob(columnLabel: String, reader: Reader) {}
  override def updateNClob(columnIndex: Int, reader: Reader) {}
  override def updateNClob(columnLabel: String, reader: Reader) {}

}


class ComposedResultSet(resultsets:Seq[StreamResultSet]) 
extends {override val metadata=resultsets.head.metadata } with StreamResultSet{
  private val iter=resultsets.iterator
  private var currentRs:StreamResultSet=_
  override def next:Boolean={
    if (currentRs!=null && currentRs.next)
      true
    else if (currentRs==null || iter.hasNext){ 
      currentRs=iter.next
      currentRs.next
    }
    else false
  }
  /*override def next:Boolean = {
    if (current==null) current=it.next
    val nxt=current.next
    if (nxt) nxt
    else if (it.hasNext){
      current=it.next
      current.next
    }
    else false       
  }*/
  
  override def findColumn(columnLabel:String):Int =resultsets.head.findColumn(columnLabel)    
  override def getMetaData:ResultSetMetaData=resultsets.head.getMetaData
 //  override def getMetaData=if (currentRs!=null)currentRs.getMetaData
 //                           else super.getMetaData
  override def getObject(columnIndex:Int):Object=currentRs.getObject(columnIndex)
  override def getObject(columnLabel:String):Object=currentRs.getObject(columnLabel)  
  override def getString(columnLabel:String):String = currentRs.getString(columnLabel)
}