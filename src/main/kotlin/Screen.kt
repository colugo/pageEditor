import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javafx.stage.Stage

class Screen : Application() {

    val service = Service("service name")
    var rootItem:TreeItem<TreeThing> = TreeItem(service)
    var tree = TreeView(rootItem)
    var globalCount = 0
    var selectedItem:TreeThing? = null
    var title = TextField("Title")
    var markdown = TextArea("Markdown")
    var browser = WebView()


    override fun start(primaryStage: Stage) {
        primaryStage.title = "Tree View Sample"

        tree.selectionModel.select(rootItem)
        tree.getSelectionModel()
                .selectedItemProperty()
                .addListener({ observable, oldValue, newValue -> updateViews(newValue.value as TreeThing) })

        title.textProperty().addListener { observable, oldValue, newValue ->  updateSelectedThingName(newValue) }
        markdown.textProperty().addListener { observable, oldValue, newValue ->  updateSelectedThingMarkdown(newValue) }


        var mainHBox = HBox()
        mainHBox.padding = Insets(15.0, 12.0, 15.0, 12.0)
        mainHBox.setSpacing(10.0)   // Gap between nodes
        var treeVBox = VBox()

        treeVBox.children.add(tree)

        var add = Button("Add new page")
        add.setOnAction {
            if(tree.selectionModel.selectedItems.size == 1) {
                val selected = tree.selectionModel.selectedItems.first()
                var newPage = Page("Page" + globalCount++, "")
                selected.value.add(newPage)

                val item = TreeItem(newPage)

                selected.children.add(item as TreeItem<TreeThing>)
                selected.isExpanded = true
            }
        }
        var parse = Button("parse")
        parse.setOnAction {
           parse()
        }
        treeVBox.children.add(add)
        treeVBox.children.add(parse)

        var edit = VBox()


        edit.children.add(title)
        edit.children.add(markdown)

        mainHBox.children.add(treeVBox)
        mainHBox.children.add(edit)
        mainHBox.children.add(browser)


        primaryStage.scene = Scene(mainHBox)
        primaryStage.show()

    }

    fun updateSelectedThingName(newName:String){
        if(selectedItem != null && selectedItem is TreeThing) {
            selectedItem!!.name = newName
            tree.refresh()
        }
    }

    fun updateSelectedThingMarkdown(newMarkdown:String){
        if(selectedItem != null && selectedItem is Page) {
            var page = selectedItem as Page
            page.markdown = newMarkdown
            browser.engine.loadContent(selectedItem!!.output())
        }
    }

    fun parse(){
        println("Parsing")
        var service = ParseJson().parse(markdown.text)
        rootItem.value = service
        for(page in service.subpages){
            var item = TreeItem<Page>(page)
            rootItem.children.add(item as TreeItem<TreeThing>)
            rootItem.isExpanded = true
            for(subpage in page.subpages) addParsedPages(item as TreeItem<TreeThing>, subpage)

        }
        tree.refresh()
    }

    fun addParsedPages(item:TreeItem<TreeThing>, subpage:Page){
        var newItem = TreeItem<Page>(subpage)
        newItem.isExpanded = true
        item.children.add(newItem as TreeItem<TreeThing>)
        for(subsubpage in subpage.subpages) addParsedPages(newItem,subsubpage)
    }

    fun updateViews(thing:TreeThing){
        selectedItem = thing
        if(thing is Service){
            title.text = thing.name
            markdown.text = thing.output()
        }
        if(thing is Page){
            title.text = thing.name
            markdown.text = thing.markdown
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Screen::class.java)
        }
    }

}

