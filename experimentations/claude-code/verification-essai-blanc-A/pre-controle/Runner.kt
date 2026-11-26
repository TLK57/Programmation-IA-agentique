import java.io.File
fun main(args: Array<String>) {
    var ok = 0; var ko = 0
    for (name in File(args[0]).readLines().filter { it.isNotBlank() }) {
        val cls = try { Class.forName(name) } catch (e: Throwable) { continue }
        val tests = cls.declaredMethods.filter { m -> m.annotations.any { it.annotationClass.qualifiedName == "kotlin.test.Test" } }.sortedBy { it.name }
        for (m in tests) {
            val inst = cls.getDeclaredConstructor().newInstance()
            cls.declaredMethods.filter { mm -> mm.annotations.any { it.annotationClass.qualifiedName == "kotlin.test.BeforeTest" } }.forEach { it.invoke(inst) }
            try { m.invoke(inst); ok++; println("OK   ${cls.name}.${m.name}") }
            catch (e: java.lang.reflect.InvocationTargetException) { ko++; println("FAIL ${cls.name}.${m.name} : ${e.targetException}") }
        }
    }
    println("TOTAL ok=$ok echec=$ko")
}
