
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.HBox
import tornadofx.*

class DemoTreeViews : View() {
    override val root = HBox()

    var tree: TreeView<Page> by inject()
    var treeItem: TreeItem<Page>? = null
    var selectedPage: Page? = null
    init {
        with(root) {
            addClass(Styles.wrapper)
            vbox {
                this += label("Based on parent-child relationships")
                tree = treeview<Page> {
                    root = TreeItem(definition)
                    root.isExpanded = true
                    cellFormat { text = it.title }
                    onUserSelect {
                       selectedPage = it
                    }
                    populate {
                        it.value.subpages
                    }
                }
            }
            vbox {
                label("Page title");
                textfield("?")
                label("Markdown");
                textarea("?")
                vbox{
                    button("Add subpage") {
                        isDefaultButton = true

                        setOnAction {
                            if (selectedPage != null) {

                                definition.add(Page("New page", ""))
                                println(definition)
                                tree.root.children.add(TreeItem(Page("","")))
                            }
                        }
                    }
                }
            }
            vbox {
                webview()
            }

        }
    }
}