package com.cooper.moviemadness

import _root_.android.app.Activity
import _root_.android.os.{Bundle, Handler}
import _root_.android.app.ListActivity
import _root_.android.widget._
import _root_.android.view._
import _root_.android.view.Menu._
import _root_.android.content.Context
import scala.actors.Actor

import com.moviemadness.html._

class FilmAdapter(films: List[Film], context: Context) 
extends BaseAdapter {
  def getCount = films.length
  def getItem(position: Int) = films(position)
  def getItemId(id: Int) = id
  def remove(id: Int) = ()

  def getView(position: Int, convert_view: View, parent: ViewGroup) = {
    var row_layout: RelativeLayout = null 
    val film = getItem(position)

    if (convert_view == null) {
      row_layout = 
        (LayoutInflater.from(context)
                       .inflate(R.layout.row, parent, false)
                       .asInstanceOf[RelativeLayout])

      val title_view = row_layout.findViewById(R.id.movie_title).asInstanceOf[TextView]
      title_view.setText(film.title)

      val year_view = row_layout.findViewById(R.id.year).asInstanceOf[TextView]
      year_view.setText(film.year match {case None => "Unknown"; case Some(yr) => yr})

      val director_view = row_layout.findViewById(R.id.director).asInstanceOf[TextView]
      director_view.setText(film.director match {case None => "Unknown"; case Some(dir) => dir})

      val section_view = row_layout.findViewById(R.id.section).asInstanceOf[TextView]
      section_view.setText(film.section)
    } else {
      row_layout = convert_view.asInstanceOf[RelativeLayout]
    }
    
    row_layout
  }
}

class MainActivity extends Activity {
    import ConverterHelper._
    private[this] var searchValue: EditText = null
    private[this] var field: Spinner = null
    private[this] var statusMessage: TextView = null
    private[this] var searchButton: Button = null
    private[this] var resultValues: ListView = null

    private var is_client_loaded = false

    val handler = new Handler
    def post(block: => Unit) {
      handler.post(new Runnable {
        def run { block }
      })
    }

    case class SearchRequest(field: String, search_text: String)  

    class MovieMadnessActor(context: Context) extends Actor {
      import Actor._

      def initClient = {
        val temp_client = new MovieMadnessClient
        post { statusMessage.setText("Client loaded.") }
        temp_client
      }

      val client = initClient
      
      def act() = loop {
        react {
          case SearchRequest(field, search_text) => 
            post { statusMessage.setText("Searching...") }
          client.searchFor(field, search_text) match {
            case None => post { statusMessage.setText("No results.") }
            case Some(movies) =>
              post { resultValues.setAdapter(new FilmAdapter(movies.toList, context))
                    statusMessage.setText("Results:") }
          }
        }
      }
    }

    val client_actor = new MovieMadnessActor(this)

    override def onCreate(savedInstanceState:Bundle) {
      client_actor.start()
      super.onCreate(savedInstanceState)
      setContentView(R.layout.main)
      field = findViewById(R.id.field_value).asInstanceOf[Spinner]

      val search_kinds = 
        new ArrayAdapter[String](
          this, R.layout.spinner_view, 
          getResources.getStringArray(R.array.search_kinds))
      field.setAdapter(search_kinds)

      searchValue = findViewById(R.id.search).asInstanceOf[EditText]
      searchButton = findViewById(R.id.search_button).asInstanceOf[Button]
      resultValues = findViewById(R.id.result_values).asInstanceOf[ListView]
      statusMessage = findViewById(R.id.status_message).asInstanceOf[TextView]
      
      searchButton.setOnClickListener( () => {
          val field_value = field.getSelectedItem.asInstanceOf[String]
          val search = searchValue.getText.toString
          client_actor ! SearchRequest(field_value, search)
      })
    }
}
