import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class Screen : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Tree View Sample"


        var service = Service("service name")

        val rootItem:TreeItem<TreeThing> = TreeItem(service)
        rootItem.isExpanded = true
        for (i in 1..5) {
            val item = TreeItem(Page("Message" + i,"") as TreeThing)
            rootItem.children.add(item)
        }
        val tree = TreeView(rootItem)

        var hbox = HBox()
        hbox.padding = Insets(15.0, 12.0, 15.0, 12.0)
        hbox.setSpacing(10.0)   // Gap between nodes
        hbox.children.add(tree)

        var edit = VBox()
        var textField = TextField("Title")
        var textArea = TextArea("Markdown")
        var add = Button("Add")
        add.setOnAction {
            val item = TreeItem(Page("Message" + 8,"") as TreeThing)
            rootItem.children.last().children.add(item)
            rootItem.children.last().isExpanded = true
        }
        edit.children.add(textField)
        edit.children.add(textArea)
        edit.children.add(add)
        hbox.children.add(edit)


        primaryStage.scene = Scene(hbox)
        primaryStage.show()

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Screen::class.java)
        }
    }

}

