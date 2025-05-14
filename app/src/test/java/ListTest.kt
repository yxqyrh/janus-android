import org.junit.Test

class ListTest {

    @Test
    fun testList() {
        val list = ArrayList<String>()
        list.add("s")
        for (str in list) {
            println(str)
        }
    }
}