package com.moviemadness.html

import org.xml.sax.InputSource
import javax.xml.parsers.SAXParser
import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
import java.io.StringReader
import scala.xml.parsing.FactoryAdapter
import scala.xml._

class TagSoupFactoryAdapter extends FactoryAdapter {

  val parserFactory = new SAXFactoryImpl
  parserFactory.setNamespaceAware(false)

  val emptyElements = Set("area", "base", "br", "col", "hr", "img",
                          "input", "link", "meta", "param")

  /** Tests if an XML element contains text.
   * @return true if element named <code>localName</code> contains text.
   */
  def nodeContainsText(localName: String) = !(emptyElements contains localName)
 
  /** creates a node.
  */
  def createNode(pre:String, label: String, attrs: MetaData,
                 scpe: NamespaceBinding, children: List[Node] ): Elem = {
    Elem( pre, label, attrs, scpe, children:_* );
  }

  /** creates a text node
  */
  def createText(text: String) =
    Text( text );
 
  /** Ignore Processing Instructions
  */
  def createProcInstr(target: String, data: String) = Nil

  /** load XML document
   * @param source
   * @return a new XML document object
   */
  override def loadXML(source: InputSource): xml.Node = {
    val parser: SAXParser = parserFactory.newSAXParser()
     
    scopeStack.push(TopScope)
    parser.parse(source, this)
    scopeStack.pop
    rootElem
  }
  
  def loadXML(source: java.io.InputStream): xml.Node = loadXML(new InputSource(source))

  def loadXML(source: String): xml.Node = loadXML(new InputSource( new StringReader(source) ))

  override def loadFile(loc: String) = loadXML(new InputSource(loc))
} 
