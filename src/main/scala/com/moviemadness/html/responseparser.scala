/** Movie Madness Client.
 *
 * Notes: seems the good html has 4 tables, number 2 contains the good stuff.
 *
 * Author: AgentCoops
 */
package com.moviemadness.html

import scala.collection.mutable
import dispatch._

class ValidationIDsMissing 
extends Exception("Could not extract the proper validation ids.")

case class Film(
  title: String, format: String, rating: String, 
  section: String, location: Option[String], year: Option[String],
  time: String, director: Option[String], actors: Seq[String])
 
object Film {
  implicit def stringToOption(str: String) = {
    val clean_str = str.trim
    if (clean_str == "") None else Some(clean_str)
  }

  def fromRow(elem: xml.Node) = {
    val columns = (elem \\ "td").map(_.text)

    Film(
      columns(0), columns(1), columns(2), columns(3),
      columns(4), columns(5), columns(6), columns(7),
      columns(8).split("\n").map(_.trim).filter(_ != ""))
  }
}

class MovieMadnessClient {
  lazy val html_parser_factory = new TagSoupFactoryAdapter
  //lazy val http_requestor = new Http with Threads
  implicit def inputStreamToXML(stream: java.io.InputStream) = 
    html_parser_factory.loadXML(stream)

  val host = :/("www.pdxmoviemadness.com")
  val search = host / "search.aspx"  
  // should this be lazy or preloaded? I think preloaded for now...
  val auth_map = getValidationIDs match {
    case (validation, state) => 
      mutable.Map("__EVENTVALIDATION" -> validation, "__VIEWSTATE" -> state)
  }
  
  // generate the parameter map for the indicated query.
  def getParamMap(field: String, by: Int, query: String) =
    (auth_map ++ Map("cmdSearch" -> "Search", "cboMatchTitleBy" -> by.toString,
                     "search" -> ("opt"+field.capitalize), "txtSearch" -> query))

  // get the validation ids from the movie madness page.
  def getValidationIDs = 
    Http(search >> { r => extractValidationIDs(r) }) //http_requestor.future

  // check whether attribute name exists in node with value value.
  def attributeValueEquals(name: String, value: String)(node: xml.Node) = 
    node \\ ("@"+name) exists(_.text == value)

  def searchFor(field: String, query: String) = 
    Http(search <<? getParamMap(field, 2, query) >> { r => extractResults(r) })
 
  def getValue(elem: Option[xml.Node]) =
    elem match {
      case Some(node) => Some((node \ "@value").text)
      case None => None
    }
  
  def extractValidationIDs(response: scala.xml.Node): (String, String) = {
    val inputs = response \\ "input"
    
     (getValue(inputs find attributeValueEquals("name", "__EVENTVALIDATION")),
      getValue(inputs find attributeValueEquals("name", "__VIEWSTATE"))) match {
        case (None, _) | (_, None) => throw new ValidationIDsMissing
        case (Some(validation), Some(state)) => (validation, state)
    }
  }

  def extractResults(response: scala.xml.Node) = {
    val tables = (response \\ "table")
      
    if (tables.length == 4) 
      Some( (tables(2) \\ "tr").drop(1).map(Film.fromRow(_)) )
    else None
  }
}
