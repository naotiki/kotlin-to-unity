import com.google.devtools.ksp.symbol.KSType

val list= mapOf("String" to "string",
    "Int" to "int",
    "Boolean" to "bool",
    "Float" to "float",
    "Double" to "double",
    "Unit" to "void")
fun KSType.Change():String{

    return list[ this.toString()] ?:this.toString()
}


