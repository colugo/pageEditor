import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.MutableDataSet

val definition = Page("Overview","",
        mutableListOf(
            Page("Base URL",""),
            Page("Open API Specification","")
        )
)


open abstract class TreeThing{
    var name = ""
    var subpages = mutableListOf<Page>()

    override fun toString():String = name
    abstract fun output():String

    fun add(page:Page){
        subpages.add(page)
    }

}

class Service(val theName:String, var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){

    init {
        name = theName
        subpages = theSubpages
    }

    override fun output():String{
        return toJson()
    }

    fun toJson():String{
        var output = "{\"name\":\"${name}\","
        output += "\"subpages\":["
        for(subpage in subpages) output += subpage.toJson() + ","
        output = output.removeSuffix(",")
        output += "]}"
        return output
    }
}

class Page(val theTitle: String, val theMarkdown: String, var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){

    init{
        name = theTitle
        subpages = theSubpages
    }

    var markdown = theMarkdown.replace("\\n","\n").replace("\\\"","\"")

    override fun output():String{
        return getMarkdown(markdown)
    }

    fun toJson():String{
        var output = "{\"title\":\"${name}\","
        output += "\"markdown\":\"${sanatise()}\""
        if(subpages.isNotEmpty()) {
            output += ",\"subpages\":["
            for(subpage in subpages) output += subpage.toJson() + ","
            output = output.removeSuffix(",")
            output += "]"
        }

        output += "}"
        return output
    }

    fun sanatise():String{
        var output = markdown
        output = output.replace("\n","\\n")
        output = output.replace("\"","\\\"")
        return output
    }


    private fun getMarkdown(md:String):String{
        val options = MutableDataSet()
        val parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        // You can re-use parser and renderer instances
        val document = parser.parse(md.trimMargin())
        val html = renderer.render(document)  // "<p>This is <em>Sparta</em></p>\n"
        return html
    }
}


