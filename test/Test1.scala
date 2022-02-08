
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import scala.jdk.CollectionConverters.*
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot
import org.apache.pdfbox.cos.COSBase
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.cos.COSObject
import org.apache.pdfbox.cos.COSInteger
import org.apache.pdfbox.cos.COSDictionary
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.pdfparser.PDFStreamParser
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.contentstream.operator.Operator
import org.apache.pdfbox.cos.COSFloat
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.text.PDFMarkedContentExtractor

@main def Test1() =
  val doc = PDDocument.load(new File("test1.pdf"))
  try
    val catalog = doc.getDocumentCatalog
    val structureRoot = catalog.getStructureTreeRoot
    
    def process(indent: Int, elem: AnyRef): Unit =
      val descr = elem match {
        case struct: PDStructureElement =>
          s"${struct.getStructureType} [Pg=${struct.getCOSObject.getCOSObject(COSName.PG)}; K=${struct.getCOSObject.getCOSArray(COSName.K).asScala map {
            case i: COSInteger => i.intValue.toString
            case obj: COSObject => s"${obj}"
            case other => other.getClass.getSimpleName
          } mkString ","}]"
          
        case root: PDStructureTreeRoot => "(root)"
        case i: Integer => s"$i"
        case other => s"$other (${other.getClass})"
      }
      println("  " * indent + s" * $descr")
      elem match {
        case struct: PDStructureElement =>
          val kids = struct.getKids.asScala
          kids foreach { kid => process(indent + 1, kid) }
          
        case root: PDStructureTreeRoot =>
          val kids = root.getKids.asScala
          kids foreach { kid => process(indent + 1, kid) }

        case _ =>
      }
      
    process(0, structureRoot)
    
    def cosDump(indent: Int, obj: COSBase): Unit =
      if indent < 20 then
        obj match {
          case dict: COSDictionary =>
            println("  " * indent + "dict")
            dict.entrySet.asScala foreach { e =>
              println("  " * indent + s"  ${e.getKey.getName}:")
              cosDump(indent + 2, e.getValue)
            }
            
          case array: COSArray =>
            println("  " * indent + "array")
            0 until array.size foreach { i =>
              println("  " * indent + s"  $i:")
              cosDump(indent + 2, array.getObject(i))
            }
            
          case obj: COSObject =>
            println("  " * indent + s"obj")
            cosDump(indent + 1, obj.getObject)
            
          case i: COSInteger =>
            println("  " * indent + s"${i.intValue}")
            
          case s: COSString =>
            println("  " * indent + s"'${s.getString}'")
            
          case other =>
            println("  " * indent + s"${other.getClass.getSimpleName}")
        }
    
    doc.getPages.asScala foreach { page =>
      println("page")
      val extractor = new PDFMarkedContentExtractor
      extractor.processPage(page)
      extractor.getMarkedContents.asScala foreach { c =>
        println(c)
      }
    }
    
  finally
    doc.close

