import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.options.MutableDataSet
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.DataFormat
import javafx.scene.input.TransferMode
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import kotlin.system.exitProcess


class Screen : Application() {

    var globalCount = 0
    var selectedItem: TreeThing? = null


    override fun start(stage: Stage) {

        val root = FXMLLoader.load<Parent>(Screen::class.java.getResource("scene.fxml"))
        scene = Scene(root, 640.0, 375.0)
        Screen.stage = stage
        stage.setTitle("Service Description Tool")
        stage.setScene(scene)

        lookupComponents()
        addListeners()

        var emptyService = Service("service_name",  mutableListOf<Page>())
        var rootItem: TreeItem<TreeThing> = newTreeItem(emptyService)
        tree!!.root = rootItem
        tree!!.selectionModel.select(tree!!.root)
        tree!!.refresh()

        stage.show()

    }

    private fun newTreeItem(thing: TreeThing): TreeItem<TreeThing> {
        var treeItem = TreeItem<TreeThing>(thing)

        return treeItem
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

        tree!!.setCellFactory {
            object : TreeCell<TreeThing>() {
                init {

                    setOnDragDetected { event ->

                        val db = this.startDragAndDrop(TransferMode.MOVE)
                        val treeItem = this.treeItem
                        db.setContent(mutableMapOf<DataFormat, Any>(DataFormat.PLAIN_TEXT to tree!!.getRow(treeItem).toString()))

                        event.consume()
                    }

                    setOnDragDropped { event ->
                        val db = event.dragboard
                        val index = db.getContent(DataFormat.PLAIN_TEXT).toString().toInt()

                        val source: TreeItem<TreeThing> = tree!!.getTreeItem(index)

                        val sourcePage = source.value as Page
                        val destPage = item as Page

                        if (destPage !in sourcePage.descendants() && source.value != treeItem.value) {


                            source.parent.children.remove(source)
                            val indexInParent = treeItem.parent.children.indexOf(treeItem)
                            treeItem.parent.children.add(indexInParent + 1, source)


                            val sourcePageParent = sourcePage.parent!!

                            sourcePage.removeFromParent()
                            destPage.parent!!.addAt(indexInParent, sourcePage)
                        }
                    }

                    setOnMouseDragReleased { println("mouse drag") }

                    setOnDragEntered {
                        this.isUnderline = true

                    }
                    setOnDragExited {
                        this.isUnderline = false
                    }

                    setOnDragOver { event ->
                        event.acceptTransferModes(TransferMode.MOVE)
                        event.consume()
                    }

                }

                override fun updateItem(item: TreeThing?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        text = null
                    } else {
                        text = if (getItem() == null) "" else getItem().toString()
                    }
                }
            }
        }
    }

    private fun lookupComponents() {
        tree = scene!!.lookup("#tree") as TreeView<TreeThing>
        browser = scene!!.lookup("#browser") as WebView
        title = scene!!.lookup("#name") as TextField
        markdown = scene!!.lookup("#markdown") as TextArea
        contentLabel = scene!!.lookup("#content_label") as Label
    }

    @FXML
    fun delete(){
        if(tree!!.selectionModel.selectedItems.size == 1) {
            val selected = tree!!.selectionModel.selectedItems.first()
            selected.parent.children.remove(selected)
            selected.value.removeFromParent()
        }
    }

    @FXML
    fun newSubPage(){
        if(tree!!.selectionModel.selectedItems.size == 1) {
            val selected = tree!!.selectionModel.selectedItems.first()
            var newPage = Page("Page" + globalCount++, "")
            selected.value.add(newPage)

            val item = newTreeItem(newPage)

            selected.children.add(item as TreeItem<TreeThing>)
            selected.isExpanded = true
        }
    }


    @FXML
    fun quit() {
        if(!fileChanged) exitProcess(0)
        println("Save first!")
    }

    @FXML
    fun save() {
        if(definitionFile == null){
            val fileChooser = FileChooser()
            fileChooser.title = "Save as"
            definitionFile = fileChooser.showSaveDialog(stage)
        }
        if(definitionFile != null && definitionFile is File && fileChanged) {
            val newConent = tree!!.root.value.output()
            definitionFile!!.writeText(newConent)
            contentOnDisk = newConent
            fileChanged = false
            stage!!.title = definitionFile!!.name
        }
    }


    @FXML
    fun open(){
        val fileChooser = FileChooser()
        fileChooser.title = "Open Resource File"
        definitionFile = fileChooser.showOpenDialog(stage)
        contentOnDisk = definitionFile!!.readText()
        var service = ParseJson().parse(contentOnDisk)
        var rootItem: TreeItem<TreeThing> = newTreeItem(service)


        rootItem.value = service
        for(page in service.subpages){
            var item = TreeItem<Page>(page)
            rootItem.children.add(item as TreeItem<TreeThing>)
            rootItem.isExpanded = true
            for(subpage in page.subpages) addParsedPages(item as TreeItem<TreeThing>, subpage)

        }

        tree!!.root = rootItem
        tree!!.selectionModel.select(tree!!.root)
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
            val service = thing as Service
            title!!.text = service.name
            markdown!!.text = service.configuration
            contentLabel!!.text = "  Service configuration - JSON"
        }
        if(thing is Page){
            title!!.text = thing.name
            markdown!!.text = thing.markdown
            updateSelectedThingMarkdown(thing.markdown)
            contentLabel!!.text = "  Page content - MarkDown"
        }
    }

    fun updateThingsIfFileChanged(){
        val content = tree!!.root.value.output()
        if(contentOnDisk.equals(content)){
            fileChanged = false
            stage!!.title = definitionFile!!.name
            return
        }
        fileChanged = true
        if(definitionFile == null){
            stage!!.title = "Unsaved defintion"
        }else {
            stage!!.title = definitionFile!!.name + "*"
        }
    }

    fun updateSelectedThingMarkdown(newMarkdown:String){

        if(selectedItem != null && selectedItem is Page) {
            var page = selectedItem as Page
            page.markdown = newMarkdown

            var html = getHTML(getPageData(page, indent = 1))
            browser!!.engine.loadContent(html)
        }
        if(selectedItem != null && selectedItem is Service) {
            var service = selectedItem as Service
            service.configuration = newMarkdown

            browser!!.engine.loadContent("")
        }
        updateThingsIfFileChanged()
    }


    fun updateSelectedThingName(newName:String){
        if(selectedItem != null && selectedItem is TreeThing) {
            selectedItem!!.name = newName
            tree!!.refresh()
            if(selectedItem is Page){
                val page = selectedItem as Page
                var html = getHTML(getPageData(page, indent = 1))
                browser!!.engine.loadContent(html)
            }
        }

        updateThingsIfFileChanged()
    }


    private fun getPageData(page: Page, indent:Int): String {
        if(indent > 10) return ""
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

        val document = parser.parse(md.trimMargin())
        val html = renderer.render(document)
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

        @JvmStatic
        var contentLabel: Label? = null

        @JvmStatic
        var definitionFile: File? = null

        @JvmStatic
        var fileChanged: Boolean = false

        @JvmStatic
        var contentOnDisk:String = ""

    }

}

