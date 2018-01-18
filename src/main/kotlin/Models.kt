
val definition = Page("Overview","",
        mutableListOf(
            Page("Base URL",""),
            Page("Open API Specification","")
        )
)


open class TreeThing{}

class Service(val theName:String, var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){

    var name = theName
    var subpages = theSubpages

    override fun toString():String = name

    fun toJson():String{
        var output = "{\"name\":\"${name}\","
        output += ",\"subpages\":["
        for(subpage in subpages) output += subpage
        output += "]"
        return output
    }
}

class Page(val theTitle: String, val theMarkdown: String, var theSubpages: MutableList<Page> = mutableListOf<Page>()):TreeThing(){

    var title = theTitle
    var markdown = theMarkdown
    var subpages = theSubpages

    override fun toString():String = title

    fun toJson():String{
        var output = "{\"title\":\"${title}\","
        output += "\"markdown\":\"${markdown}\""
        if(subpages.isNotEmpty()) {
            output += ",\"subpages\":["
            for(subpage in subpages) output += subpage
            output += "]"
        }

        output += "}"
        return output
    }


    fun add(page:Page){
        subpages.add(page)
    }
}
