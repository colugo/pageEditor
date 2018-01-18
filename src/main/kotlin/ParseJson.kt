import com.beust.klaxon.JsonObject
import com.beust.klaxon.array
import com.beust.klaxon.string
import com.beust.klaxon.Parser

data class ServiceDescriptionPage(val title: String, val content: String, val subpages: List<ServiceDescriptionPage>?)
data class ServiceDescription(val name: String, val id: String, val subpages: List<ServiceDescriptionPage>)


class ParseJson {


    fun parse(json: String): Service {
        var serviceJson: JsonObject = Parser().parse(StringBuilder().append(json)) as JsonObject


        var name = serviceJson.string("name")
        var id = serviceJson.string("id")

        var pagesList = getSubPages(serviceJson)
        var serviceDescription = ServiceDescription(name!!, id!!, pagesList.toList())

        var service = Service(serviceDescription.name)
        for(pageDescription in serviceDescription.subpages){
            var page = Page(pageDescription.title,pageDescription.content)
            service.add(page)
            addSubpages(page, pageDescription)
        }

        return service
    }

    fun addSubpages(page :Page, description: ServiceDescriptionPage){
        if(description.subpages == null){
            return
        }
        for(descriptionPage in description.subpages!!){
            var subPage = Page(descriptionPage.title, descriptionPage.content)
            page.add(subPage)
            addSubpages(subPage, descriptionPage)
        }
    }

    private fun getSubPages(thingWithPages: JsonObject): List<ServiceDescriptionPage> {
        var pages = thingWithPages.array<JsonObject>("subpages")
        var pagesList = mutableListOf<ServiceDescriptionPage>()
        for (page in pages!!) {
            var title = page.string("title")
            var markdown = page.string("markdown")
            if (page.containsKey("subpages")) {
                pagesList.add(ServiceDescriptionPage(title!!, markdown!!, getSubPages(page)))
            } else {
                pagesList.add(ServiceDescriptionPage(title!!, markdown!!, null))
            }
        }
        return pagesList.toList<ServiceDescriptionPage>()
    }


    private fun _parse(name: String): Any? {
        val cls = Parser::class.java
        return cls.getResourceAsStream(name)?.let { inputStream ->
            try {
                return Parser().parse(inputStream)
            } catch (e: Exception) {
                println("!!!!!!!!!!!!!!\n${e}")
                val position = e.message!!.replace("Unexpected character at position ", "").split(":")[0].toInt()
                val inputAsString = cls.getResourceAsStream(name).bufferedReader().use { it.readText() }
                println(inputAsString.subSequence(position - 50, position + 50))
            }
        }
    }

}