import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.options.MutableDataSet
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlin.system.exitProcess


class Screen : Application() {

    //val service = Service("service name")
    //var rootItem:TreeItem<TreeThing> = TreeItem(service)
//    var tree: TreeView? = null
    var globalCount = 0
    var selectedItem:TreeThing? = null
//    var title = TextField("Title")
//    var markdown = TextArea("Markdown")
//    var browser = WebView()


    override fun start(stage: Stage){

        val root = FXMLLoader.load<Parent>(Screen::class.java.getResource("scene.fxml"))
        scene = Scene(root, 640.0, 375.0)
        Screen.stage = stage
        stage.setTitle("Service Description Tool")
        stage.setScene(scene)
        stage.show()

        lookupComponents()
        addListeners()

    }

    private fun addListeners() {
        tree!!.selectionModel.select(tree!!.root)
        tree!!.getSelectionModel()
                .selectedItemProperty()
                .addListener({ observable, oldValue, newValue -> updateViews(newValue.value as TreeThing) })


        title!!.textProperty().addListener { observable, oldValue, newValue -> updateSelectedThingName(newValue) }
        markdown!!.textProperty().addListener { observable, oldValue, newValue -> updateSelectedThingMarkdown(newValue) }


        scene!!.widthProperty().addListener { observableValue, oldSceneWidth, newSceneWidth ->

            markdown!!.autosize()

        }
    }

    private fun lookupComponents() {
        tree = scene!!.lookup("#tree") as TreeView<TreeThing>
        browser = scene!!.lookup("#browser") as WebView
        title = scene!!.lookup("#name") as TextField
        markdown = scene!!.lookup("#markdown") as TextArea
    }

    @FXML
    fun newSubPage(){
        if(tree!!.selectionModel.selectedItems.size == 1) {
            val selected = tree!!.selectionModel.selectedItems.first()
            var newPage = Page("Page" + globalCount++, "")
            selected.value.add(newPage)

            val item = TreeItem(newPage)

            selected.children.add(item as TreeItem<TreeThing>)
            selected.isExpanded = true
        }
    }


    @FXML
    fun quit() {
        exitProcess(0)
    }

    @FXML
    fun open(){
        val fileChooser = FileChooser()
        fileChooser.title = "Open Resource File"
        var file = fileChooser.showOpenDialog(stage)
        var service = ParseJson().parse(file.readText())
        var rootItem: TreeItem<TreeThing> = TreeItem(service)


        rootItem.value = service
        for(page in service.subpages){
            var item = TreeItem<Page>(page)
            rootItem.children.add(item as TreeItem<TreeThing>)
            rootItem.isExpanded = true
            for(subpage in page.subpages) addParsedPages(item as TreeItem<TreeThing>, subpage)

        }
        tree!!.root = rootItem
        tree!!.refresh()
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
            title!!.text = thing.name
            markdown!!.text = thing.output()
        }
        if(thing is Page){
            title!!.text = thing.name
            markdown!!.text = thing.markdown
            updateSelectedThingMarkdown(thing.markdown)
        }
    }

    fun updateSelectedThingMarkdown(newMarkdown:String){
        if(selectedItem != null && selectedItem is Page) {
            var page = selectedItem as Page
            page.markdown = newMarkdown


            var html = getHTML(getPageData(page, indent = 1))
            println(html)

            browser!!.engine.loadContent(html)
        }
    }


    fun updateSelectedThingName(newName:String){
        if(selectedItem != null && selectedItem is TreeThing) {
            selectedItem!!.name = newName
            tree!!.refresh()
        }
    }


    private fun getPageData(page: Page, indent:Int): String {
        var rawContent = ""
        rawContent += pageWithHeading(page, indent)
        if (page.subpages != null) for (subpage in page.subpages) {
            rawContent += getPageData(subpage, indent + 1)
        }
        return rawContent
    }

    private fun pageWithHeading(page:Page, indent:Int): String {
        var output = "\n<a name=\"${page.name}\"></a>\n\n"
        output += "${"#".repeat(indent)} ${page.name}\n\n${page.output()}\n\n"
        return output
    }

    private fun getHTML(md:String):String {
        val options = MutableDataSet()
        val parser = com.vladsch.flexmark.parser.Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        // You can re-use parser and renderer instances
        val document = parser.parse(md.trimMargin())
        val html = renderer.render(document)  // "<p>This is <em>Sparta</em></p>\n"
        return html
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Screen::class.java)
        }

        @JvmStatic
        var scene:Scene? = null

        @JvmStatic
        var stage:Stage? = null

        @JvmStatic
        var tree: TreeView<TreeThing>? = null

        @JvmStatic
        var browser: WebView? = null

        @JvmStatic
        var title: TextField? = null

        @JvmStatic
        var markdown: TextArea? = null
    }

}

