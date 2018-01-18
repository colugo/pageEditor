import javafx.collections.ObservableList
import tornadofx.observable

data class Group(val name: String, val children: List<Group>? = null)

val group = Group("Parent",
        listOf(
                Group("Child 1"),
                Group("Child 2"),
                Group("Child 3", listOf(
                        Group("Grand child 3.1",
                                listOf(
                                        Group("Great grandchild 3.1.1"),
                                        Group("Great grandchild 3.1.2"))))
                ),
                Group("Child 4"))
)

val definition = Page("Overview","",
        listOf(
            Page("Base URL",""),
            Page("Open API Specification","")
        ).observable()
)



class Page(val theTitle: String, val theMarkdown: String, var theSubpages: ObservableList<Page> = listOf<Page>().observable()){

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
        subpages = subpages.plus(page).observable()
    }
}
