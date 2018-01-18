import org.junit.Assert
import org.junit.Test

class PageTest {

    @Test
    fun pageAsString(){
        Assert.assertEquals("""{"title":"Overview","markdown":""}""",Page("Overview","", mutableListOf<Page>()).toString())
    }

    @Test
    fun canAddSubpage(){
        var page = Page("Overview","")
        var subpage = Page("Base URL","")
        page.add(subpage)
        Assert.assertEquals("""{"title":"Overview","markdown":"","subpages":[{"title":"Base URL","markdown":""}]}""",page.toString())
    }
}