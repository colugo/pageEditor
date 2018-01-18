
val definition = Page("Overview","",
        mutableListOf(
            Page("Base URL",""),
            Page("Open API Specification","")
        )
)



class Page(val theTitle: String, val theMarkdown: String, var theSubpages: MutableList<Page> = mutableListOf<Page>()){

    var title = theTitle
    var markdown = theMarkdown
    var subpages = theSubpages

    override fun toString():String{
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
