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
        page.parent = this
        subpages.add(page)
    }

    abstract fun removeFromParent()
    fun remove(page:Page){
        subpages.remove(page)
    }


    fun addAt(index:Int, page:Page){
        subpages.add(index,page)
    }
}

class Service(val theName:String, private var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){

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

    override fun removeFromParent(){
        throw NotImplementedError("Can't remove service from parent")
    }
}

class Page(val theTitle: String, val theMarkdown: String, var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){


    var parent:TreeThing? = null
    init{
        name = theTitle
        subpages = theSubpages
    }

    fun descendants():MutableList<Page>{
        var desc = mutableListOf<Page>()
        if(subpages == null) return desc
        for(subpage in subpages){
            desc.add(subpage)
            desc.addAll(subpage.descendants())
        }
        return desc
    }

    var markdown = theMarkdown.replace("\\n","\n").replace("\\\"","\"")

    override fun output():String{
        return markdown
    }

    override fun removeFromParent(){

        if(parent != null) parent!!.remove(this)
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
}


